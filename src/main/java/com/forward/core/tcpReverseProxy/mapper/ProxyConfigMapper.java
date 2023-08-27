package com.forward.core.tcpReverseProxy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forward.core.tcpReverseProxy.entity.ChannelProxyConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ProxyConfigMapper extends BaseMapper<ChannelProxyConfig> {
    // 自定义方法

    Optional<ChannelProxyConfig> findByConfKey(@Param("channel") String channel, @Param("confKey") String confKey);

    List<ChannelProxyConfig> distinctChannelConfig(@Param("confKey") String confKey);

    Integer updChannelEnvConf(@Param("channel") String channel, @Param("confKey") String confKey, @Param("confVal") String confVal);
}
