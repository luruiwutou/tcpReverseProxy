package com.forward.core.tcpReverseProxy;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
public class ReverseProxyServer {
    // 创建并启动代理服务器
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    private Map<String, List<String>> hosts;
    private Map<String, Boolean> hostStates;
    private Map<String, Map<String, ProxyHandler>> targetProxyHandlerForHosts;
    //这一台代理服务需要转发的目的服务器ip、端口
//    private String[][] targetConnections = {{"localhost", "8881"}, {"localhost", "8882"}, {"localhost", "8883"}};


    public ReverseProxyServer() {
        this.hostStates = new ConcurrentHashMap<>();
        this.targetProxyHandlerForHosts = new ConcurrentHashMap<>();
    }

    public ReverseProxyServer(Map<String, List<String>> hosts) {
        this.hosts = hosts;
        this.hostStates = new ConcurrentHashMap();
        this.targetProxyHandlerForHosts = new ConcurrentHashMap<>();
    }

    public static ReverseProxyServer start(Map<String, List<String>> hosts) throws Exception {
        ReverseProxyServer server = new ReverseProxyServer(hosts);
        server.start(server, hosts);
        return server;
    }


    public void start(ReverseProxyServer server, Map<String, List<String>> hosts) throws Exception {
        for (Map.Entry<String, List<String>> host : hosts.entrySet()) {
            Map<String, ProxyHandler> targetProxyHandler = new HashMap<>();
            server.bootstrap(getTargetFunction(), targetProxyHandler).bind(Integer.valueOf(host.getKey())).addListener((ChannelFuture future) -> {
                final Channel channel = future.channel();
                hostStates.put(host.getKey(), true);
                log.info("---Server is started and listening at---" + channel.localAddress());
            }).sync().await();
            targetProxyHandlerForHosts.put(host.getKey(), targetProxyHandler);
        }
    }

    /**
     * 获取当前服务端口配置的代理目标
     * @return
     */
   private Function<String, List<String>> getTargetFunction() {
        log.info("--------------取targetHost{}------------------",JSON.toJSONString(hosts));
        return (port) -> hosts.get(port);
    }

    /**
     * 关闭对转发目标客户端的连接
     *
     * @param hostPort
     * @param targetHost
     * @throws Exception
     */
    public void stopTargetServer(String hostPort, String targetHost) throws Exception {
        Map<String, ProxyHandler> stringProxyHandlerMap = targetProxyHandlerForHosts.get(hostPort);
        if (CollectionUtils.isEmpty(stringProxyHandlerMap)) {
            return;
        }
        ProxyHandler proxyHandler = stringProxyHandlerMap.get(targetHost);
        if (proxyHandler == null) {
            return;
        }
        proxyHandler.shutdown();
        stringProxyHandlerMap.remove(targetHost);
    }

    /**
     * 关闭对转发目标客户端的连接
     *
     * @param hostPort
     * @param targetHosts
     * @throws Exception
     */
    public void stopTargetServer(String hostPort, List<String> targetHosts) throws Exception {
        Map<String, ProxyHandler> stringProxyHandlerMap = targetProxyHandlerForHosts.get(hostPort);
        if (CollectionUtils.isEmpty(stringProxyHandlerMap)) {
            return;
        }
        for (String targetHost : targetHosts) {
            ProxyHandler proxyHandler = stringProxyHandlerMap.get(targetHost);
            if (proxyHandler != null) {
                proxyHandler.shutdown();
                stringProxyHandlerMap.remove(targetHost);
            }
        }
    }

    /**
     * 切换对转发目标客户端的连接
     *
     * @param hostPort
     * @param targetHost
     * @throws Exception
     */
    public void switchTargetServer(String hostPort, String shutDownTargetHost, String targetHost) throws Exception {
        ProxyHandler proxyHandler = targetProxyHandlerForHosts.get(hostPort).get(shutDownTargetHost);
        proxyHandler.switchTargetServer(targetHost);
        targetProxyHandlerForHosts.get(hostPort).remove(shutDownTargetHost);
        targetProxyHandlerForHosts.get(hostPort).put(targetHost, proxyHandler);

    }

    private ServerBootstrap bootstrap(Function<String, List<String>> getConnections, Map<String, ProxyHandler> targetProxyHandler) throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    private String targetHost;
                    private int targetPort;
                    private Map<String, Integer> connectionCounts = new ConcurrentHashMap<>();
                    private List<String> targetConnections = new ArrayList<>();

                    @Override
                    protected void initChannel(SocketChannel ch) {
                        boolean connectionFlag = setTarget(ch.localAddress().getPort());
                        if (!connectionFlag) {
                            ch.close();
                        }
                        log.info("当前代理服务器:{},\n已连接信息：{},\n远程客户端地址：{},\n此次连接转发目标地址：{}:{}", ch.localAddress().toString().replace("/", ""), JSON.toJSONString(connectionCounts), ch.remoteAddress().toString().replace("/", ""), targetHost, targetPort);
                        ProxyHandler proxyHandler = new ProxyHandler(targetHost, targetPort, connectionCounts, targetProxyHandler, getNewTarget());
                        ch.pipeline().addLast(proxyHandler);
                    }

                    private Function<String, String> getNewTarget() {
                        return (target) -> CollectionUtils.isEmpty(targetConnections) || targetConnections.size() < 2 ? target : targetConnections.stream().filter(a -> !a.equals(target)).findAny().get();
                    }

                    //判断是否能够建立有效连接
                    private boolean setTarget(int port) {
                        targetConnections = getConnections.apply(String.valueOf(port));
                        if (CollectionUtils.isEmpty(targetConnections)) {
                            return false;
                        }
                        // 寻找连接数最少的目标服务器
                        // 选择使用计数最少的目标服务器
                        String selectedTarget = null;
                        int minConnectionCount = Integer.MAX_VALUE;
                        for (String targetStr : targetConnections) {
                            String[] target = targetStr.split(":");
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
                            String[] split = targetConnections.get(0).split(":");
                            targetHost = split[0];
                            targetPort = Integer.valueOf(split[1]);
                        }
                        return true;
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


    public void shutDown() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public Map<String, List<String>> getHosts() {
        return hosts;
    }

    public void setHosts(Map<String, List<String>> hosts) {
        this.hosts = hosts;
    }

    public static void main(String[] args) throws Exception {
        try {
            // 本地服务器的端口号
            String[] localPorts = {"8888", "8889"};
            // 目标服务器的主机名或IP地址,目标服务器的端口号
            List<String> connections = Arrays.asList("localhost:8881", "localhost:8882", "localhost:8883");
            Map<String, List<String>> hashMap = new HashMap<>();
            for (int i = 0; i < localPorts.length; i++) {
                hashMap.put(localPorts[i], connections);
            }
            ReverseProxyServer server = start(hashMap);
            server.stopTargetServer("8888", "localhost:8881");

        } finally {
//            server.shutDown();
        }
    }
}
