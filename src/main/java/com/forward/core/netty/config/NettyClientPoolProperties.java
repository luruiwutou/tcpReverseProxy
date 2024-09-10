package com.forward.core.netty.config;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
     * server 配置
     */
    private String port = null;
    /**
     * 读超时处理时间：四分半
     */
    private int readIdleTimeout = 0;
    /**
     * 写超时处理时间
     */
    private int writeIdleTimeout = 0;

    /**
     * 空闲时间（MINUTES）达到idleTimeout处理 用于发送心跳消息
     */
    private int allIdleTimeout = 5;
    /**
     * 连接池大小
     */
    private int poolSize = 10;
    /**
     * 银联报文头长度
     */
    private int cupyHeadLength = 6;

    /**
     * 工作线程数
     */
    private int bossThreadsCount = 100;
    /**
     * 工作线程数
     */
    private int workerThreadsCount = 100;
    /**
     * 重连次数
     */
    private int retryTimes = 3000;

    public NettyClientPoolProperties(List<String> servers, String port) {
        this.servers = servers;
        this.port = port;
    }
}
