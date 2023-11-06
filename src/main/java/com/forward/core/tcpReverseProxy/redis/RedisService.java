package com.forward.core.tcpReverseProxy.redis;

import com.forward.core.constant.Constants;
import com.forward.core.sftp.utils.StringUtil;
import com.forward.core.tcpReverseProxy.entity.ChannelProxyConfig;
import com.forward.core.tcpReverseProxy.mapper.ProxyConfigMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

@Component
@Slf4j
public class RedisService {


    @Autowired
    RedisTemplate<String, byte[]> redisTemplateByteArray;
    @Autowired
    StringRedisTemplate redisTemplateStr;
    @Autowired
    ProxyConfigMapper proxyConfigMapper;

    public String getStrValueByKey(String key) {
        return redisTemplateStr.opsForValue().get(key);
    }

    public String getStrValueByKeyAndChanel(String channel, String suffix) {
        String result = redisTemplateStr.opsForValue().get(channel + Constants.UNDERLINE + suffix);
        if (StringUtil.isNotEmpty(result)) {
            return result;
        }
        Optional<ChannelProxyConfig> byConfKey = proxyConfigMapper.findByConfKey(channel, suffix);
        if (byConfKey.isPresent()) {
            setStrValue(channel + Constants.UNDERLINE + suffix, byConfKey.get().getConfVal());
            result = byConfKey.get().getConfVal();
        }
        return result;
    }

    public String getStrValueByEnvAndChannelAndKey(String env, String channel, String suffix) {
        if (StringUtil.isEmpty(env)) {
            return getStrValueByKeyAndChanel(channel, suffix);
        } else {
            return getStrValueByKeyAndChanel(channel, env + Constants.UNDERLINE + suffix);
        }

    }

    public void setStrValue(String key, String value) {
        redisTemplateStr.opsForValue().set(key, value);
    }

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
            log.info("receive from :{} ,write Hex msg to redis :{}", key, ByteBufUtil.hexDump((ByteBuf) msg));
            byte[] bytes = changeMessage((ByteBuf) msg);
            this.push(key, bytes);
        }
    }


    public void push(String key, byte[]... objs) {
        redisTemplateByteArray.opsForList().leftPushAll(key, objs);
    }

    public byte[] changeMessage(ByteBuf byteBuf) {
        // 将ByteBuf转换为字节数组
        byte[] bytes = ByteBufUtil.getBytes(byteBuf);
        byteBuf.release();
        return bytes;

    }

}
