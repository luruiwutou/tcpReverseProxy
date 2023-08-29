package com.forward.core.tcpReverseProxy;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.forward.core.sftp.utils.StringUtil;
import com.forward.core.constant.Constants;
import com.forward.core.tcpReverseProxy.handler.CustomizeLengthFieldBasedFrameDecoder;
import com.forward.core.tcpReverseProxy.handler.ProxyHandler;
import com.forward.core.tcpReverseProxy.utils.SnowFlake;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * TCP反向代理服务
 */
@Slf4j
@Component
public class ReverseProxyServer {
    // 创建并启动代理服务器
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    private EventLoopGroup handlerWorkerGroup = new NioEventLoopGroup();
    private EventExecutorGroup executorGroup = new DefaultEventExecutorGroup(64);
    /**
     * 渠道对应的代理集合
     */
    private Map<String, Map<String, List<String[]>>> hosts;
    // 存储端口与对应的Channel对象
    private Map<String, Channel> serverChannels;
    /**
     * 每个代理地址对应的目标处理器
     */
    private Map<String, ConcurrentLinkedQueue<ProxyHandler>> targetProxyHandlerForHosts;
    /**
     * 端口对应的拆包长度
     */
    private Map<String, Integer> fieldLengthMap = new HashMap<>();

    public ReverseProxyServer() {
        if (CollectionUtil.isEmpty(this.hosts)) {
            this.hosts = new ConcurrentHashMap<>();
        }
        this.serverChannels = new ConcurrentHashMap<>();
        this.targetProxyHandlerForHosts = new ConcurrentHashMap<>();
    }

    public ReverseProxyServer(String channel, Map<String, List<String[]>> hostMap) {
        if (CollectionUtil.isEmpty(this.hosts)) {
            this.hosts = new ConcurrentHashMap<>();
        }
        if (CollectionUtil.isEmpty(this.serverChannels)) {
            this.hosts = new ConcurrentHashMap<>();
        }
        if (CollectionUtil.isEmpty(this.targetProxyHandlerForHosts)) {
            this.hosts = new ConcurrentHashMap<>();
        }
        this.hosts.put(channel, hostMap);
    }

    /**
     * @return
     */
    public void start() {
        if (hosts.isEmpty()) {
            return;
        }
        this.hosts.forEach((key, value) -> {
            start(this, value);
        });
    }

    public void start(ReverseProxyServer server, Map<String, List<String[]>> hosts) {
        log.info("start server channel :{}", JSON.toJSONString(hosts));
        for (Map.Entry<String, List<String[]>> host : hosts.entrySet()) {
            ConcurrentLinkedQueue<ProxyHandler> targetProxyHandler = new ConcurrentLinkedQueue<>();
            if (server.getServerChannels().keySet().contains(host.getKey())) {
                log.info("ports:{} has been Started", host.getKey());
                continue;
            }
            try {
                server.bootstrap(getTargetFunction(), targetProxyHandler).bind(Integer.valueOf(host.getKey())).addListener((ChannelFuture future) -> {
                    final Channel channel = future.channel();
                    log.info("---Server is started and listening at---{}----proxy target :{}", channel.localAddress(), JSON.toJSONString(host.getValue()));
                    // 将Channel对象存储到serverChannels中
                    server.getServerChannels().put(host.getKey(), channel);
//                    server.getFieldLengthMap().put(host.getKey(), fieldLength);
                }).sync().await();
            } catch (Exception e) {
                log.info("port:{} start failed, error msg:{}", host.getKey(), e.getMessage());
                log.error("full error info", e);
            }
            server.getTargetProxyHandlerForHosts().put(host.getKey(), targetProxyHandler);
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
                log.error("同步关闭服务channel异常", e);
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
    private Function<String, List<String[]>> getTargetFunction() {
        return (port) -> {
            //与渠道无关，所以排除渠道的影响，进行合并处理
            List<String[]> targetHost = this.hosts.values().stream()
                    .flatMap(map -> map.entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (existingValue, newValue) -> newValue)).get(port);

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


    private ServerBootstrap bootstrap(Function<String, List<String[]>> getConnections, ConcurrentLinkedQueue<ProxyHandler> targetProxyHandlers) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
            private String targetHost;
            private int targetPort;
            private String remoteClientPort = "";
            private String localClientPort = "";
            private Map<String, Integer> connectionCounts = new ConcurrentHashMap<>();
            private List<String[]> targetConnections = new ArrayList<>();


            @Override
            protected void initChannel(SocketChannel ch) {
                String traceId = SnowFlake.getTraceId();
                MDC.put(Constants.TRACE_ID, traceId);
                remoteClientPort = String.valueOf(ch.localAddress().getPort());
                boolean connectionFlag = setTarget();
                if (!connectionFlag) {
                    ch.close();
                }
                ch.pipeline().addLast(new CustomizeLengthFieldBasedFrameDecoder(10240, 0, 4, 0, 0));
                log.info("当前代理服务器:{},\n已连接信息：{},\n远程客户端地址：{},\n此次连接转发目标地址：{}:{}", ch.localAddress().toString().replace("/", ""), JSON.toJSONString(connectionCounts), ch.remoteAddress().toString().replace("/", ""), targetHost, targetPort);
                if (!CollectionUtils.isEmpty(targetProxyHandlers)) {
                    if (StringUtil.isNotEmpty(localClientPort)) {
                        Optional<ProxyHandler> optionalProxyHandler;
                        if (Constants.LOCAL_PORT_RULE_SINGLE.equals(localClientPort)) {
                            optionalProxyHandler = targetProxyHandlers.stream().filter(handler -> getTargetServerAddress().equals(handler.getTargetServerAddress())).findAny();
                        } else {
                            optionalProxyHandler = targetProxyHandlers.stream().filter(handler -> StringUtil.isNotEmpty(handler.getClientPort()) && handler.getClientPort().equals(localClientPort)).findAny();
                        }
                        if (optionalProxyHandler.isPresent()) {
                            ch.pipeline().addLast(optionalProxyHandler.get());
                            return;
                        }
                    }

                }
                ProxyHandler proxyHandler = new ProxyHandler(localClientPort, targetHost, targetPort, connectionCounts, handlerWorkerGroup, executorGroup, targetProxyHandlers, getNewTarget(), getReconnectTime());
                ch.pipeline().addLast(proxyHandler);
            }


            private Function<String[], String[]> getNewTarget() {
                return (target) -> {
                    setTarget();
                    if (CollectionUtils.isEmpty(targetConnections)) {
                        return target;
                    }
                    if (targetConnections.size() == 1) {
                        return targetConnections.get(0);
                    }
                    List<String[]> otherTarget = targetConnections.stream().filter(a -> !a[1].equals(target[1])).collect(Collectors.toList());
                    Random random = new Random();
                    int index = random.nextInt(otherTarget.size());
                    String[] result = otherTarget.get(index);

                    return result;
                };
            }

            private Supplier<Integer> getReconnectTime() {
                setTarget();
                return () -> 5 * targetConnections.size();
            }

            //判断是否能够建立有效连接
            private boolean setTarget() {
                targetConnections = getConnections.apply(remoteClientPort);
                if (CollectionUtils.isEmpty(targetConnections)) {
                    return false;
                }
                // 寻找连接数最少的目标服务器
                // 选择使用计数最少的目标服务器
                String selectedTarget = null;
                String selectedClientPort = null;
                int minConnectionCount = Integer.MAX_VALUE;
                for (String[] targetStr : targetConnections) {
                    String[] target = targetStr[1].split(":");
                    String targetHost = target[0];
                    String targetPort = target[1];
                    String targetServerId = targetHost + ":" + targetPort;
                    int connectionCount = connectionCounts.getOrDefault(targetServerId, 0);
                    if (connectionCount < minConnectionCount) {
                        selectedTarget = targetServerId;
                        selectedClientPort = targetStr[0];
                        minConnectionCount = connectionCount;
                    }
                }

                if (selectedTarget != null) {
                    // 执行相关操作，如使用 selectedTarget 进行转发或其他处理
                    log.info("Selected target: {}", selectedTarget);
                    String[] split = selectedTarget.split(":");
                    localClientPort = selectedClientPort;
                    targetHost = split[0];
                    targetPort = Integer.valueOf(split[1]);
                } else {
                    // 处理未找到可用目标服务器的情况
                    localClientPort = targetConnections.get(0)[0];
                    String[] split = targetConnections.get(0)[1].split(":");
                    targetHost = split[0];
                    targetPort = Integer.valueOf(split[1]);
                }
                return true;
            }

            public String getTargetServerAddress() {
                return targetHost + ":" + targetPort;
            }
        });
        return bootstrap;

    }


    public void shutDown() {
        Set<String> keySet = hosts.values().stream()
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (existingValue, newValue) -> newValue)).keySet();
        if (CollectionUtil.isNotEmpty(keySet)) {
            for (String port : keySet) {
                closeChannelConnects(port);
            }
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        handlerWorkerGroup.shutdownGracefully();
        executorGroup.shutdownGracefully();
    }

    public Map<String, Map<String, List<String[]>>> getHosts() {
        return hosts;
    }

    public void setHosts(Map<String, Map<String, List<String[]>>> hosts) {
        this.hosts = hosts;
    }

    public void putChannelHosts(String channel, Map<String, List<String[]>> channelHosts) {
        this.hosts.put(channel, channelHosts);
    }

    public Map<String, Channel> getServerChannels() {
        return serverChannels;
    }

    public Map<String, Integer> getFieldLengthMap() {
        return fieldLengthMap;
    }

    public Map<String, ConcurrentLinkedQueue<ProxyHandler>> getTargetProxyHandlerForHosts() {
        return targetProxyHandlerForHosts;
    }

//    public static void main(String[] args) throws Exception {
//        try {
//            // 本地服务器的端口号
//            String[] localPorts = {"8888", "8889"};
//            // 目标服务器的主机名或IP地址,目标服务器的端口号
//            List<String[]> connections = Arrays.asList(new String[]{"", "localhost:8881"}, new String[]{"", "localhost:8881"});
//            Map<String, List<String[]>> hashMap = new HashMap<>();
//            for (int i = 0; i < localPorts.length; i++) {
//                hashMap.put(localPorts[i], connections);
//            }
//            ReverseProxyServer server = start(hashMap);
//            server.stopTargetServer("8888", "localhost:8881");
//
//        } finally {
////            server.shutDown();
//        }
//    }
}
