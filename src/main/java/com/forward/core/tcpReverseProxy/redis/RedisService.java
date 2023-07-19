package com.forward.core.tcpReverseProxy.redis;

import cn.hutool.core.util.HexUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Component
@Slf4j
public class RedisService {


    @Autowired
    RedisTemplate<String, byte[]> redisTemplateByteArray;

    public boolean isRedisConnected() {
        RedisConnection connection = null;
        try {
            connection = redisTemplateByteArray.getConnectionFactory().getConnection();
            String response = connection.ping();
            return "PONG".equals(response);
        } catch (Exception e) {
            // 处理连接异常
            return false;
        } finally {
            if (connection != null) connection.close();
        }
    }

    public List<byte[]> getList(String key, Long count) {

        Long size = redisTemplateByteArray.opsForList().size(key);
        redisTemplateByteArray.opsForList();
        if (Objects.isNull(size) || size == 0) {
            return Collections.emptyList();
        }
        return redisTemplateByteArray.opsForList().leftPop(key, count > size ? size : count);

    }

    public Long getListSize(String key) {
        if (!this.isRedisConnected()) {
            return null;
        }
        return redisTemplateByteArray.opsForList().size(key);
    }

    public void readMsgCache(String key, Consumer consumer) {
        if (!this.isRedisConnected()) {
            return;
        }
        Long listSize = redisTemplateByteArray.opsForList().size(key);
        if (Objects.isNull(listSize) || listSize == 0) {
            return;
        }
        for (int i = 0; i < listSize / 10 + 1; i++) {
            List<byte[]> popList = this.getList(key, 10l);
            for (Object msg : popList) {
                consumer.accept(Unpooled.wrappedBuffer((byte[]) msg));

            }
        }
    }

    public void writeMsgCache(String key, Object msg) {
        if (!this.isRedisConnected()) {
            return;
        }
        if (msg instanceof ByteBuf) {
            byte[] bytes = changeMessage((ByteBuf) msg);
            log.info("receive from :{} ,write Hex msg to redis :{}", key, HexUtil.encodeHexStr(bytes));
            this.push(key, bytes);
        }
    }


    public void push(String key, byte[]... objs) {
        redisTemplateByteArray.opsForList().leftPushAll(key, objs);
    }

    public byte[] changeMessage(ByteBuf byteBuf) {
        // 将ByteBuf转换为字节数组
        byte[] byteArray = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(byteArray);
        byteBuf.release();
        return byteArray;

    }

}
