package com.forward.core.tcpReverseProxy;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import com.alibaba.fastjson.JSON;
import com.forward.core.constant.Constants;
import com.forward.core.sftp.utils.StringUtil;
import com.forward.core.tcpReverseProxy.entity.TcpProxyMapping;
import com.forward.core.tcpReverseProxy.handler.*;
import com.forward.core.tcpReverseProxy.interfaces.FourConsumer;
import com.forward.core.tcpReverseProxy.redis.RedisService;
import com.forward.core.tcpReverseProxy.utils.LockUtils;
import com.forward.core.tcpReverseProxy.utils.SingletonBeanFactory;
import com.forward.core.tcpReverseProxy.utils.SnowFlake;
import com.forward.core.tcpReverseProxy.utils.balance.Balance;
import com.forward.core.tcpReverseProxy.utils.balance.QueueBalance;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
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
    private Map<String, Map<String, TcpProxyMapping>> hosts;
    // 存储端口与对应的Channel对象
    private Map<String, Channel> serverChannels;
    /**
     * 每个代理地址对应的目标处理器
     */
    private Map<String, ConcurrentLinkedQueue<ProxyHandler>> targetProxyHandlerForHosts;

    public ReverseProxyServer() {
        if (CollectionUtil.isEmpty(this.hosts)) {
            this.hosts = new ConcurrentHashMap<>();
        }
        this.serverChannels = new ConcurrentHashMap<>();
        this.targetProxyHandlerForHosts = new ConcurrentHashMap<>();
    }

    public ReverseProxyServer(String channel, Map<String, TcpProxyMapping> hostMap) {
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

    public List<String> start(ReverseProxyServer server, Map<String, TcpProxyMapping> hosts) {
        log.info("start server channel :{}", JSON.toJSONString(hosts));
        List<String> errorServerPorts = new ArrayList<>();
        for (Map.Entry<String, TcpProxyMapping> host : hosts.entrySet()) {
            ConcurrentLinkedQueue<ProxyHandler> targetProxyHandler = new ConcurrentLinkedQueue<>();
            if (server.getServerChannels().keySet().contains(host.getKey()) && server.getServerChannels().get(host.getKey()).isActive()) {
                log.info("ports:{} has been Started", host.getKey());
                continue;
            }
            try {
                server.bootstrap(getTargetFunction(), targetProxyHandler, putCustomizeChannelHandler(), getNowEnv()).bind(Integer.valueOf(host.getKey())).addListener((ChannelFuture future) -> {
                    if (future.isSuccess()) {
                        final Channel channel = future.channel();
                        log.info("---Server is started and listening at---{}----proxy target :{}", channel.localAddress(), JSON.toJSONString(host.getValue()));
                        // 将Channel对象存储到serverChannels中
                        server.getServerChannels().put(host.getKey(), channel);
                    }
                }).sync().await();
            } catch (Exception e) {
                errorServerPorts.add(host.getKey());
                log.info("port:{} start failed, error msg:{}", host.getKey(), e.getMessage());
                log.error("full error info", e);
            }
            server.getTargetProxyHandlerForHosts().put(host.getKey(), targetProxyHandler);
        }
        return errorServerPorts;
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
                if (!serverChannel.isActive()) {
                    serverChannels.remove(portToStop); // 从Map中移除该端口的Channel对象
                    return;
                }
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
            List<String[]> targetHost = this.hosts.values().stream().flatMap(map -> map.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getTargetConnections(), (existingValue, newValue) -> newValue)).get(port);

            log.info("--------------服务端口：{},监听服务为：{}------------------", port, JSON.toJSONString(targetHost));
            return targetHost;
        };
    }

    private Function<String, String> getNowEnv() {
        return (port) -> this.hosts.values().stream().flatMap(map -> map.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getEnv(), (existingValue, newValue) -> newValue)).get(port);
    }

    private final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9_/\\\\.-]+$");

    public SslContext createServerSslContext(String env, String channel) throws Exception {
        String pemPath = redisService.getStrValueByEnvAndChannelAndKey(env, channel, Constants.PATH_SSL_TSL_PEM_PATH);
        String keyPath = redisService.getStrValueByEnvAndChannelAndKey(env, channel, Constants.PATH_SSL_TSL_KEY_PATH);
        if (ReUtil.isMatch(PATTERN, pemPath) && ReUtil.isMatch(PATTERN, keyPath)) {
            return SslContextBuilder.forServer(new File(pemPath), new File(keyPath)).build();
        }
        throw new Exception("Path illegal, pemPath:" + pemPath + " keyPath:" + keyPath);
    }


    public SslContext createClientSslContext(String env, String channel) throws Exception {
        String certPath = redisService.getStrValueByEnvAndChannelAndKey(env, channel, Constants.PATH_SSL_TSL_CERT_PATH);
        if (ReUtil.isMatch(PATTERN, certPath)) {
            return SslContextBuilder.forClient().trustManager(new File(certPath)).build();
        }
        throw new Exception("Path illegal, certPath:" + certPath);
    }

    /**
     * 存放自定义channelHandler
     * @return
     */
    private FourConsumer<String, String, String, SocketChannel> putCustomizeChannelHandler() {
        return (env, cOrS, port, channel) -> {
            //与渠道无关，所以排除渠道的影响，进行合并处理
            Map<String, List<String>> result = this.hosts.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList())));
            if (Constants.SERVER.equals(cOrS)) {
                // 在 result 中查找包含 'port' 的列表并输出对应的键
                for (Map.Entry<String, List<String>> entry : result.entrySet()) {
                    String channelName = entry.getKey();
                    List<String> mappingKeys = entry.getValue();
                    if (mappingKeys.contains(port)) {
                        channel.pipeline().addFirst("headTraceIdHandler", new HeadTraceIdHandler());
                        String openssl = redisService.getStrValueByEnvAndChannelAndKey(env, channelName, Constants.PROXY_SERVER_OPEN_SSL);
                        log.info("Get channel {} ssl config :{}", channelName, openssl);
                        if (ObjectUtil.isNotEmpty(openssl) && Constants.TRUE_STR.equals(openssl)) {
                            try {
                                channel.pipeline().addFirst("ssl/tls", new CustomizeSslHandler(createServerSslContext(env, channelName).newEngine(channel.alloc())));
                            } catch (Exception e) {
                                log.info("server add ssl handler failed", e);
                            }
                        }
                        String fieldLengthObj = redisService.getStrValueByEnvAndChannelAndKey(env, channelName, Constants.DEFAULT_FIELD_LENGTH_KEY);
                        log.info("Get channel {} length field config :{}", channelName, fieldLengthObj);
                        if (ObjectUtil.isNotEmpty(fieldLengthObj)) {
                            String[] fields = fieldLengthObj.toString().split(",");
                            channel.pipeline().addLast("customizeLengthFieldBasedFrameDecoder", new CustomizeLengthFieldBasedFrameDecoder(Integer.valueOf(fields[0]), Integer.valueOf(fields[1]), Integer.valueOf(fields[2]), Integer.valueOf(fields[3]), Integer.valueOf(fields[4])));
                        }
                        String idleConfig = redisService.getStrValueByEnvAndChannelAndKey(env, channelName, Constants.PROXY_SERVER_IDLE_CONFIG);
                        if (ObjectUtil.isNotEmpty(idleConfig)) {
                            try {
                                String[] split = idleConfig.split(",");
                                channel.pipeline().addLast("idleStateHandler", new IdleStateHandler(Integer.valueOf(split[0]), Integer.valueOf(split[1]), Integer.valueOf(split[2])));
                                channel.pipeline().addLast("nettyClientIdleEventHandler", new NettyClientIdleEventHandler());
                            } catch (Exception e) {
                                log.info("server add idle handler failed", e);
                            }
                        }
                    }
                }
            } else if (Constants.CLIENT.equals(cOrS)) {
                // 在 result 中查找包含 'port' 的列表并输出对应的键
                for (Map.Entry<String, List<String>> entry : result.entrySet()) {
                    String channelName = entry.getKey();
                    List<String> mappingKeys = entry.getValue();
                    if (mappingKeys.contains(port)) {
                        channel.pipeline().addFirst("headTraceIdHandler", new HeadTraceIdHandler());
                        String fieldLengthObj = redisService.getStrValueByEnvAndChannelAndKey(env, channelName, Constants.DEFAULT_FIELD_LENGTH_KEY);
                        log.info("Get channel {} config :{}", channelName, fieldLengthObj);
                        if (StringUtil.isNotEmpty(fieldLengthObj)) {
                            String[] fields = fieldLengthObj.toString().split(",");
                            channel.pipeline().addLast("customizeLengthFieldBasedFrameDecoder", new CustomizeLengthFieldBasedFrameDecoder(Integer.valueOf(fields[0]), Integer.valueOf(fields[1]), Integer.valueOf(fields[2]), Integer.valueOf(fields[3]), Integer.valueOf(fields[4])));
                        }
                        String openssl = redisService.getStrValueByEnvAndChannelAndKey(env, channelName, Constants.PROXY_CLIENT_OPEN_SSL);
                        if (ObjectUtil.isNotEmpty(openssl) && Constants.TRUE_STR.equals(openssl)) {
                            try {
                                channel.pipeline().addFirst("ssl/tls", new CustomizeSslHandler(createClientSslContext(env, channelName).newEngine(channel.alloc())));
                            } catch (Exception e) {
                                log.info("client add ssl handler failed", e);
                            }
                        }
                        String idleConfig = redisService.getStrValueByEnvAndChannelAndKey(env, channelName, Constants.PROXY_CLIENT_IDLE_CONFIG);
                        if (ObjectUtil.isNotEmpty(idleConfig)) {
                            try {
                                String[] split = idleConfig.split(",");
                                channel.pipeline().addLast("idleStateHandler", new IdleStateHandler(Integer.valueOf(split[0]), Integer.valueOf(split[1]), Integer.valueOf(split[2])));
                                channel.pipeline().addLast("nettyClientIdleEventHandler", new NettyClientIdleEventHandler());
                            } catch (Exception e) {
                                log.info("client add idle handler failed", e);
                            }
                        }
                    }
                }
            }


        };
    }

    private RedisService redisService = SingletonBeanFactory.getSpringBeanInstance(RedisService.class).getSingleton();


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


    private ServerBootstrap bootstrap(Function<String, List<String[]>> getConnections, ConcurrentLinkedQueue<ProxyHandler> targetProxyHandlers, FourConsumer<String, String, String, SocketChannel> putCustomizeChannelHandler, Function<String, String> getNowEnv) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 128) //设置线程队列中等待连接的个数
                .childOption(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 50000) // 设置连接超时时间为50秒
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    private String targetHost;
                    private int targetPort;
                    private String remoteClientPort = "";
                    private String localClientPort = "";
                    private volatile Map<String, Integer> connectionCounts = new ConcurrentHashMap<>();
                    private volatile List<String[]> targetConnections = new ArrayList<>();
                    private volatile Balance<String[]> targetConnectionQueue = new QueueBalance<>();

                    /**
                     * din
                     * @return
                     */
                    private Consumer<SocketChannel> putCustomizeTargetChannelHandler() {
                        return (socketChannel) -> putCustomizeChannelHandler.accept(getNowEnv.apply(remoteClientPort), Constants.CLIENT, remoteClientPort, socketChannel);
                    }

                    @Override
                    protected synchronized void initChannel(SocketChannel ch) throws Exception {
                        String traceId = SnowFlake.getTraceId();
                        MDC.put(Constants.TRACE_ID, traceId);
                        ch.attr(Constants.TRACE_ID_KEY).set(traceId);
                        remoteClientPort = String.valueOf(ch.localAddress().getPort());
                        boolean connectionFlag = setTarget();
                        if (!connectionFlag) {
                            ch.close();
                        }
//                        if (CollectionUtil.isNotEmpty(customizeHandlerMap)) {
//                            customizeHandlerMap.forEach((key, value) -> ch.pipeline().addLast(key, value));
//                        }
                        putCustomizeChannelHandler.accept(getNowEnv.apply(remoteClientPort), Constants.SERVER, remoteClientPort, ch);
                        log.info("当前代理服务器:{},\n已连接信息：{},\n远程客户端地址：{},\n此次连接转发目标地址：{}:{}", ch.localAddress().toString().replace("/", ""), JSON.toJSONString(connectionCounts), ch.remoteAddress().toString().replace("/", ""), targetHost, targetPort);
                        LockUtils.executeWithLock(getTargetServerAddress(), LockUtils.defaultExpireTime, (v) -> {
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
                            ProxyHandler proxyHandler = new ProxyHandler(localClientPort, targetHost, targetPort, connectionCounts, handlerWorkerGroup, executorGroup, getNewTarget(), getReconnectTime(), putCustomizeTargetChannelHandler());
                            proxyHandler.setProxyHandlers(targetProxyHandlers);
                            targetProxyHandlers.add(proxyHandler);
                            ch.pipeline().addLast(proxyHandler);
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG)).fireChannelReadComplete();
                        }, 5, 1500);

                    }


                    private Function<String[], String[]> getNewTarget() {
                        return (target) -> {
                            log.info("getNewTarget");
                            setTarget();
                            if (CollectionUtils.isEmpty(targetConnections)) {
                                return target;
                            }
                            if (targetConnections.size() == 1) {
                                return targetConnections.get(0);
                            }
//                            List<String[]> otherTarget = targetConnections.stream().filter(a -> !a[1].equals(target[1])).collect(Collectors.toList());
//                            Random random = new Random();
//                            int index = random.nextInt(otherTarget.size());
//                            String[] result = otherTarget.get(index);
                            String[] result = targetConnectionQueue.chooseOne(targetConnections);
                            return result;
                        };
                    }

                    private Supplier<Integer> getReconnectTime() {
                        targetConnections = getConnections.apply(remoteClientPort);
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
//                        for (String[] targetStr : targetConnections) {
//                            String[] target = targetStr[1].split(":");
//                            String targetHost = target[0];
//                            String targetPort = target[1];
//                            String targetServerId = targetHost + ":" + targetPort;
//                            int connectionCount = connectionCounts.getOrDefault(targetServerId, 0);
//                            if (connectionCount < minConnectionCount) {
//                                selectedTarget = targetServerId;
//                                selectedClientPort = targetStr[0];
//                                minConnectionCount = connectionCount;
//                            }
//                        }
                        String[] strings = targetConnectionQueue.chooseOne(targetConnections);
                        selectedClientPort = strings[0];
                        selectedTarget = strings[1];
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
        Set<String> keySet = hosts.values().stream().flatMap(map -> map.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (existingValue, newValue) -> newValue)).keySet();
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

    public Map<String, Map<String, TcpProxyMapping>> getHosts() {
        return hosts;
    }

    public void setHosts(Map<String, Map<String, TcpProxyMapping>> hosts) {
        this.hosts = hosts;
    }

    public void putChannelHosts(String channel, Map<String, TcpProxyMapping> channelHosts) {
        this.hosts.put(channel, channelHosts);
    }

    public Map<String, Channel> getServerChannels() {
        return serverChannels;
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
