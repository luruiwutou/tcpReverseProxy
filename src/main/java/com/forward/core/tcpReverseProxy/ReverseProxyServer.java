package com.forward.core.tcpReverseProxy;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ReverseProxyServer {
    // 创建并启动代理服务器
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    private int localPort;
    //这一台代理服务需要转发的目的服务器ip、端口
//    private String[][] targetConnections = {{"localhost", "8881"}, {"localhost", "8882"}, {"localhost", "8883"}};


    public ReverseProxyServer() {
    }

    public ServerBootstrap start(String[][] targetConnections) throws Exception {

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    private String targetHost;
                    private int targetPort;
                    private Map<String, Integer> connectionCounts = new ConcurrentHashMap<>();

                    @Override
                    protected void initChannel(SocketChannel ch) {
                        setTarget();
                        log.info("当前代理服务器:{},\n已连接信息：{},\n远程客户端地址：{},\n此次连接转发目标地址：{}:{}", ch.localAddress().toString().replace("/", ""), JSON.toJSONString(connectionCounts), ch.remoteAddress().toString().replace("/", ""), targetHost, targetPort);

                        ch.pipeline().addLast(new ProxyHandler(targetHost, targetPort, connectionCounts));
                    }

                    private void setTarget() {
                        // 寻找连接数最少的目标服务器
                        // 选择使用计数最少的目标服务器
                        String selectedTarget = null;
                        int minConnectionCount = Integer.MAX_VALUE;
                        for (String[] target : targetConnections) {
                            String targetHost = target[0];
                            String targetPort = target[1];
                            String targetServerId = targetHost + ":" + targetPort;
                            int connectionCount = connectionCounts.getOrDefault(targetServerId, 0);
                            if (connectionCount < minConnectionCount) {
                                selectedTarget = targetServerId;
                                minConnectionCount = connectionCount;
                            }
                        }

                        if (selectedTarget != null) {
                            // 执行相关操作，如使用 selectedTarget 进行转发或其他处理
                            log.info("Selected target: {}", selectedTarget);
                            String[] split = selectedTarget.split(":");
                            targetHost = split[0];
                            targetPort = Integer.valueOf(split[1]);
                        } else {
                            // 处理未找到可用目标服务器的情况
                            targetHost = targetConnections[0][0];
                            targetPort = Integer.valueOf(targetConnections[0][1]);
                        }
                    }
                });

//            ChannelFuture future = bootstrap.bind(localHost,localPort);
//              future.addListener((ChannelFuture future1) -> {
//                  final Channel channel = future1.channel();
//                  log.info("Server is started and listening at {}", channel.localAddress());
//              });
//              future.channel().closeFuture().sync().await();
//            future.channel().closeFuture().sync();
        return bootstrap;

    }

    public static void startProxyServer(String host, int port, ChannelHandler handler) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(handler);

            ChannelFuture future = bootstrap.bind(host, port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void shutDown() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public static void main(String[] args) throws Exception {
        ReverseProxyServer server = new ReverseProxyServer();
        try {
            // 本地服务器的端口号
            int[] localPorts = {8888, 8889};
            // 目标服务器的主机名或IP地址,目标服务器的端口号
            String[][] connections = {{"localhost", "8881"}, {"localhost", "8882"}, {"localhost", "8883"}};

            for (int i = 0; i < localPorts.length; i++) {
                server.start(connections).bind(localPorts[i]).addListener((ChannelFuture future) -> {
                    final Channel channel = future.channel();
                    log.info("---Server is started and listening at---" + channel.localAddress());
                }).sync().await();
            }
        } finally {
//            server.shutDown();
        }
    }
}
