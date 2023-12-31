package com.forward.core.tcpReverseProxy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forward.core.tcpReverseProxy.entity.TcpProxyMapping;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TcpProxyMappingMapper extends BaseMapper<TcpProxyMapping> {
    // 自定义方法
//    TcpProxyMapping selectMappingWithTargets(@Param("localHost") String localHost, @Param("localPort") String localPort);

    List<TcpProxyMapping> selectMappingWithTargets(@Param("channel") String channel, @Param("env") String env, @Param("localIp") String localIp, @Param("regex") String regex);
}
