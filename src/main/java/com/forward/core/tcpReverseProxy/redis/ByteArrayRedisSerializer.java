package com.forward.core.tcpReverseProxy.redis;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.StandardCharsets;

public class ByteArrayRedisSerializer implements RedisSerializer<byte[]> {

    @Override
    public byte[] serialize(byte[] bytes) throws SerializationException {
        return bytes;
    }

    @Override
    public byte[] deserialize(byte[] bytes) throws SerializationException {
        return bytes;
    }


}
