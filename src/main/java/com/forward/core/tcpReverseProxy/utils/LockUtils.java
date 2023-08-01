package com.forward.core.tcpReverseProxy.utils;

import cn.hutool.core.lang.UUID;
import lombok.extern.slf4j.Slf4j;
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

    public static long defaultExpireTime = 60;

    static {
        redisTemplate = SpringUtils.getBean("stringRedisTemplate");
    }

    public static <T> T executeWithLock(String lockKey, long expireTime, Supplier<T> method) throws Exception {
        // 获取锁的过期时间
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
        // 获取锁的过期时间
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
