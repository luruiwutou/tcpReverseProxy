package com.forward.core.tcpReverseProxy;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.forward.core.tcpReverseProxy.handler.ProxyHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public class ReverseProxyServer {
    // 创建并启动代理服务器
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    private Map<String, List<String>> hosts;
    // 存储端口与对应的Channel对象
    private Map<String, Channel> serverChannels;
    private Map<String, ConcurrentLinkedQueue<ProxyHandler>> targetProxyHandlerForHosts;

    public ReverseProxyServer() {
        this.serverChannels = new ConcurrentHashMap<>();
        this.targetProxyHandlerForHosts = new ConcurrentHashMap<>();
    }

    public ReverseProxyServer(Map<String, List<String>> hosts) {
        this.hosts = hosts;
        this.serverChannels = new ConcurrentHashMap();
        this.targetProxyHandlerForHosts = new ConcurrentHashMap<>();
    }

    public static ReverseProxyServer start(Map<String, List<String>> hosts) throws Exception {
        ReverseProxyServer server = new ReverseProxyServer(hosts);
        server.start(server, hosts);
        return server;
    }


    public void start(ReverseProxyServer server, Map<String, List<String>> hosts) throws Exception {
        log.info("start server channel :{}",JSON.toJSONString(hosts));
        for (Map.Entry<String, List<String>> host : hosts.entrySet()) {
            ConcurrentLinkedQueue<ProxyHandler> targetProxyHandler = new ConcurrentLinkedQueue<>();
            if (serverChannels.keySet().contains(host.getKey())) {
                log.info("ports:{} has been Started", host.getKey());
                continue;
            }
            try {
                server.bootstrap(getTargetFunction(), targetProxyHandler).bind(Integer.valueOf(host.getKey())).addListener((ChannelFuture future) -> {
                    final Channel channel = future.channel();
                    log.info("---Server is started and listening at---{}----proxy target :{}", channel.localAddress(), JSON.toJSONString(host.getValue()));
                    // 将Channel对象存储到serverChannels中
                    serverChannels.put(host.getKey(), channel);
                }).sync().await();
            } catch (Exception e) {
                log.info("port:{} start failed, error msg:{}", host.getKey(),e.getMessage());
                log.error("full error info",e);
            }
            targetProxyHandlerForHosts.put(host.getKey(), targetProxyHandler);
        }
    }


    public void closeChannelConnects(String port) {
        stopServer(port);
        ConcurrentLinkedQueue<ProxyHandler> targetProxyHandler = targetProxyHandlerForHosts.get(port);
        if (CollectionUtils.isEmpty(targetProxyHandler)) {
            log.info("---No target channel found for port---" + port);
            return;
        }
        while (true) {
            Iterator<ProxyHandler> iterator = targetProxyHandler.iterator();
            if (!iterator.hasNext()) break;
            ProxyHandler next = iterator.next();
            iterator.remove();
            next.shutdown();
        }
        targetProxyHandlerForHosts.remove(port);
        log.info("---Server is stopped for port---" + port);
    }

    /**
     * 停止监听指定端口
     *
     * @param port
     */
    private void stopServer(String port) {
        Channel serverChannel = serverChannels.get(port); // 获取对应端口的Channel对象
        int portToStop = Integer.parseInt(port);
        if (serverChannel != null) {
            try {
                serverChannel.close().addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        // 关闭成功f
                        serverChannels.remove(portToStop); // 从Map中移除该端口的Channel对象
                        log.info("Server stopped listening at port: " + portToStop);
                    } else {
                        // 关闭失败
                        Throwable cause = future.cause();
                        log.error("Failed to stop server at port: " + portToStop, cause);
                    }
                }).sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.error("同步关闭服务channel异常",e);
            }
        } else {
            log.warn("No server channel found for port: " + portToStop);
        }
    }

    /**
     * 获取当前服务端口配置的代理目标
     *
     * @return
     */
    private Function<String, List<String>> getTargetFunction() {
        return (port) -> {
            List<String> targetHost = hosts.get(port);
            log.info("--------------服务端口：{},监听服务为：{}------------------", port, JSON.toJSONString(targetHost));
            return targetHost;
        };
    }

    /**
     * 关闭对转发目标客户端的连接
     *
     * @param hostPort
     * @param targetHost
     * @throws Exception
     */
    public void stopTargetServer(String hostPort, String targetHost) throws Exception {
        ConcurrentLinkedQueue<ProxyHandler> ProxyHandlers = targetProxyHandlerForHosts.get(hostPort);
        if (CollectionUtils.isEmpty(ProxyHandlers)) {
            return;
        }
        List<ProxyHandler> proxyHandlers = ProxyHandlers.stream().filter(proxyHandler -> targetHost.equals(proxyHandler.getTargetServerAddress())).collect(Collectors.toList());
        if (proxyHandlers == null) {
            return;
        }
        for (ProxyHandler proxyHandler : proxyHandlers) {
            proxyHandler.shutdown();
        }
        ProxyHandlers.removeAll(proxyHandlers);
    }


    private ServerBootstrap bootstrap(Function<String, List<String>> getConnections, ConcurrentLinkedQueue targetProxyHandlers) throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
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
                ProxyHandler proxyHandler = new ProxyHandler(targetHost, targetPort, connectionCounts, targetProxyHandlers, getNewTarget(), getReconnectTime());
                ch.pipeline().addLast(proxyHandler);
            }

            private Function<String, String> getNewTarget() {
                return (target) -> {
                    if (CollectionUtils.isEmpty(targetConnections) || targetConnections.size() < 2) {
                        return target;
                    }
                    List<String> otherTarget = targetConnections.stream().filter(a -> !a.equals(target)).collect(Collectors.toList());
                    Random random = new Random();
                    int index = random.nextInt(otherTarget.size());
                    return otherTarget.get(index);
                };
            }

            private Supplier<Integer> getReconnectTime() {
                return () -> 5 * targetConnections.size();
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
        if (CollectionUtil.isNotEmpty(hosts.keySet())) {
            for (String port : hosts.keySet()) {
                closeChannelConnects(port);
            }
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public Map<String, List<String>> getHosts() {
        return hosts;
    }

    public void setHosts(Map<String, List<String>> hosts) {
        this.hosts = hosts;
    }

    public Map<String, Channel> getServerChannels() {
        return serverChannels;
    }

    public void setServerChannels(Map<String, Channel> serverChannels) {
        this.serverChannels = serverChannels;
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
