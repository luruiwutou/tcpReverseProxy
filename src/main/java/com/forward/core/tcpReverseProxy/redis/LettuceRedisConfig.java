package com.forward.core.tcpReverseProxy.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.Serializable;

@Configuration
public class LettuceRedisConfig {

    @Bean
    public RedisTemplate<String, Serializable> redisTemplateSer(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, Serializable> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());//StringRedisSerializer：序列化为String
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());//GenericJackson2JsonRedisSerializer：序列化为JSON,同时在json中加入@class属性，类的全路径包名，方便反系列化
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);//设置连接工厂
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplateObj(LettuceConnectionFactory lettuceConnectionFactory) {
        lettuceConnectionFactory.setShareNativeConnection(false);
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        // 序列化
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // String serialization of keys
        redisTemplate.setKeySerializer(stringRedisSerializer);
        // String serialization of hash keys
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        // Jackson serialization of values
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        // Jackson serialization of hash values
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }


    @Bean
    public RedisTemplate<String, byte[]> redisTemplateByteArray(LettuceConnectionFactory lettuceConnectionFactory) {
        lettuceConnectionFactory.setShareNativeConnection(false);
        RedisTemplate<String, byte[]> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);

        // 使用自定义的ByteArrayRedisSerializer来处理byte[]的存储和读取
        ByteArrayRedisSerializer byteArrayRedisSerializer = new ByteArrayRedisSerializer();

        // String serialization of keys
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // String serialization of hash keys
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        // Custom serialization of values (byte[])
        redisTemplate.setValueSerializer(byteArrayRedisSerializer);
        // Custom serialization of hash values (byte[])
        redisTemplate.setHashValueSerializer(byteArrayRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}