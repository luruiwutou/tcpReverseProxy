package com.forward.core.tcpReverseProxy.utils;

import cn.hutool.core.lang.UUID;
import com.forward.core.constant.Constants;
import com.forward.core.sftp.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 分布式锁工具类
 */
@Slf4j
public class LockUtils {
    private final static RedisTemplate<String, String> redisTemplate;

    public static long defaultExpireTime = 30;

    static {
        redisTemplate = SpringUtils.getBean("stringRedisTemplate");
    }

    public static <T> T executeWithLock(String lockKey, long expireTime, Supplier<T> method) throws Exception {
        addTraceId();
        String requestId = UUID.randomUUID().toString();
        // 加锁逻辑
        boolean lockAcquired = acquireLock(lockKey, requestId, expireTime);
        try {
            log.info("Lock acquired :{}", lockAcquired);
            if (lockAcquired) {
                // 获取锁成功，执行业务逻辑
                return method.get();
            } else {
                // 获取锁失败，可以根据需要处理
                log.info("=====Failed to acquire lock=====");
                throw new Exception("Failed to acquire lock ");
            }
        } finally {
            // 释放锁
            if (lockAcquired)
                releaseLock(lockKey, requestId);
        }
    }

    /**
     * @param lockKey
     * @param expireTime seconds
     * @param method
     * @throws Exception
     */
    public static void executeWithLock(String lockKey, long expireTime, Consumer<Void> method) throws Exception {
        addTraceId();
        String requestId = UUID.randomUUID().toString();
        // 加锁逻辑
        boolean lockAcquired = acquireLock(lockKey, requestId, expireTime);
        try {
            log.info("=====Lock {} acquired :{}=====", lockKey, lockAcquired ? "success" : "failure");
            if (lockAcquired) {
                // 获取锁成功，执行业务逻辑
                method.accept(null);
            } else {
                // 获取锁失败，可以根据需要处理
                log.info("=====Failed to acquire lock, not allow execute=====");
                throw new Exception("Failed to acquire lock");
            }
        } finally {
            if (lockAcquired)
                // 释放锁
                releaseLock(lockKey, requestId);
        }
    }

    public static <T> T executeWithLock(String lockKey, long expireTime, Supplier<T> method, int maxRetryAttempts, long retryInterval) throws Exception {
        addTraceId();
        // 获取锁的过期时间
        String requestId = UUID.randomUUID().toString();
        int retryCount = 0;

        while (retryCount < maxRetryAttempts) {
            // 尝试获取锁
            boolean lockAcquired = acquireLock(lockKey, requestId, expireTime);

            if (lockAcquired) {
                try {
                    log.info("Lock acquired :{}", lockAcquired);
                    // 获取锁成功，执行业务逻辑
                    return method.get();
                } finally {
                    // 释放锁
                    releaseLock(lockKey, requestId);
                }
            } else {
                // 获取锁失败，等待一段时间后重试
                log.info("Failed to acquire lock. Retrying...");
                Thread.sleep(retryInterval); // 休眠一段时间后重试
                retryCount++;
            }
        }

        // 超过重试次数仍然无法获取锁，抛出异常或者执行其他处理
        log.info("Exceeded maximum retry attempts. Failed to acquire lock.");
        throw new Exception("Failed to acquire lock after maximum retry attempts.");
    }

    public static void executeWithLock(String lockKey, long expireTime, Consumer<Void> method, int maxRetryAttempts, long retryInterval) throws Exception {
        addTraceId();
        // 获取锁的过期时间
        String requestId = UUID.randomUUID().toString();
        int retryCount = 0;

        while (retryCount < maxRetryAttempts) {
            // 尝试获取锁
            boolean lockAcquired = acquireLock(lockKey, requestId, expireTime);
            if (lockAcquired) {
                try {
                    log.info("Lock key:{}, Lock acquired :{}",lockKey, lockAcquired);
                    // 获取锁成功，执行业务逻辑
                    method.accept(null);
                } finally {
                    if (lockAcquired)
                        // 释放锁
                        releaseLock(lockKey, requestId);
                    break;
                }
            } else {
                // 获取锁失败，等待一段时间后重试
                log.info("Failed to acquire lock. Retrying after {}ms...",retryInterval);
                Thread.sleep(retryInterval); // 休眠一段时间后重试
                retryCount++;
            }
        }
    }

    private static void addTraceId() {
        String traceId = MDC.get(Constants.TRACE_ID);
        if (StringUtil.isEmpty(traceId)) {
            traceId = SnowFlake.getTraceId();
            MDC.put(Constants.TRACE_ID, traceId);
        }
    }


    public static <T> T executeWithLock(String lockKey, Supplier<T> method) throws Exception {
        return executeWithLock(lockKey, defaultExpireTime, method);
    }

    public static <T> T executeWithLock(Supplier<T> method) throws Exception {
        return executeWithLock(UUID.randomUUID().toString(), defaultExpireTime, method);
    }

    public static void executeWithLock(String lockKey, Consumer<Void> method) throws Exception {
        executeWithLock(lockKey, defaultExpireTime, method);
    }

    public static void executeWithLock(Consumer<Void> method) throws Exception {
        executeWithLock(UUID.randomUUID().toString(), defaultExpireTime, method);
    }

    public static boolean acquireLock(String lockKey, String requestId, long seconds) {
        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
            // 尝试获取锁
            // 执行 SETNX 指令，如果锁不存在则设置成功，返回 1；如果锁已存在则设置失败，返回 0
            boolean lockAcquired = connection.setNX(lockKey.getBytes(), requestId.getBytes());

            if (lockAcquired) {
                // 设置锁的过期时间
                connection.expire(lockKey.getBytes(), seconds);
            }

            return lockAcquired;
        } catch (Exception e) {
            // 异常处理...
            log.info("acquire lock failed,exception is {}", e);
            return false;
        }
    }


    public static void releaseLock(String lockKey, String requestId) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText("if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end");
        script.setResultType(Long.class);
        Long execute = redisTemplate.execute(script, Collections.singletonList(lockKey), requestId);
        if (execute == 1)
            log.info("=====锁：{} 已释放!=====", lockKey);
        else
            log.info("=====锁：{} 未主动释放，可能超时释放了!=====", lockKey);
    }

}
