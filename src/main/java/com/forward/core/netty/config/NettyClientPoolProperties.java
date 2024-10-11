package com.forward.core.netty.config;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @description:CUPY银联渠道，配置类
 * @time: 2022/7/19
 */
//@ConfigurationProperties(prefix = "jetco.netty.client-pool")
@Data
@NoArgsConstructor
public class NettyClientPoolProperties {


    /**
     * server 配置
     */
    private List<String> servers;
    /**
     * remote_server - local_port 键值对
     */
    private Map<String,String> remoteServerAndLocalPort;
    /**
     * server 配置
     */
    private String port = null;
/**
 * port : List
 */
    /**
     * 连接池大小
     */
    private int poolSize = 10;

    /**
     * 重连次数
     */
    private int retryTimes = 10;

    public NettyClientPoolProperties(List<String> servers, String port) {
        this.servers = servers;
        this.port = port;
    }
    public NettyClientPoolProperties(Map<String,String> servers ) {
        this.remoteServerAndLocalPort = servers;
    }
}
