package com.forward.core.tcpReverseProxy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forward.core.tcpReverseProxy.entity.ProxyConfig;
import com.forward.core.tcpReverseProxy.entity.TcpProxyMapping;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ProxyConfigMapper extends BaseMapper<ProxyConfig> {
    // 自定义方法

    Optional<ProxyConfig> findByConfKey(@Param("confKey") String confKey);
}
