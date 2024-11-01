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
import com.forward.core.tcpReverseProxy.utils.SecureFileAccess;
import com.forward.core.tcpReverseProxy.utils.SingletonBeanFactory;
import com.forward.core.tcpReverseProxy.utils.SnowFlake;
import com.forward.core.utils.NettyUtils;
import com.forward.core.utils.balance.Balance;
import com.forward.core.utils.balance.QueueBalance;
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

import javax.annotation.PreDestroy;
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
    private EventLoopGroup clientWorkGroup = new NioEventLoopGroup();
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

    private final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9_,/\\\\.-]+$");

    public SslContext createServerSslContext(String env, String channel) throws Exception {
        String pemFileName = SecureFileAccess.getSafePath(redisService.getStrValueByEnvAndChannelAndKey(env, channel, Constants.PATH_SSL_TSL_PEM_FILENAME));
        String keyFileName = SecureFileAccess.getSafePath(redisService.getStrValueByEnvAndChannelAndKey(env, channel, Constants.PATH_SSL_TSL_KEY_FILENAME));
        return SslContextBuilder.forServer(new File(pemFileName), new File(keyFileName)).build();
    }


    public SslContext createClientSslContext(String env, String channel) throws Exception {
        String certFileName = SecureFileAccess.getSafePath(redisService.getStrValueByEnvAndChannelAndKey(env, channel, Constants.PATH_SSL_TSL_CERT_FILENAME));
        if (ReUtil.isMatch(PATTERN, certFileName)) {
            return SslContextBuilder.forClient().trustManager(new File(certFileName)).build();
        }
        throw new Exception("Path illegal, certFileName:" + certFileName);
    }

    /**
     * 存放自定义channelHandler
     *
     * @return
     */
    private FourConsumer<String, String, String, Channel> putCustomizeChannelHandler() {
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
                                channel.pipeline().addLast("nettyClientIdleEventHandler", new HeartbeatHandler(split.length == 4 ? split[3] : null));
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
                                channel.pipeline().addLast("nettyClientIdleEventHandler", new HeartbeatHandler(split.length == 4 ? split[3] : null));
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


    private ServerBootstrap bootstrap(Function<String, List<String[]>> getConnections, ConcurrentLinkedQueue<ProxyHandler> targetProxyHandlers, FourConsumer<String, String, String, Channel> putCustomizeChannelHandler, Function<String, String> getNowEnv) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 256)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 50000) // 设置连接超时时间为50秒
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_RCVBUF, 1048576)
                .childOption(ChannelOption.SO_SNDBUF, 1048576)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    private String remoteClientPort = "";
                    private volatile Map<String, Integer> connectionCounts = new ConcurrentHashMap<>();
                    private volatile List<String[]> targetConnections = new ArrayList<>();

                    /**
                     * din
                     * @return
                     */
                    private Consumer<Channel> putCustomizeTargetChannelHandler() {
                        return (channel) -> {
                            putCustomizeChannelHandler.accept(getNowEnv.apply(remoteClientPort), Constants.CLIENT, remoteClientPort, channel);
                        };
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
                        putCustomizeChannelHandler.accept(getNowEnv.apply(remoteClientPort), Constants.SERVER, remoteClientPort, ch);
                        log.info("当前代理服务器:{},\n已连接信息：{},\n远程客户端地址：{},", NettyUtils.getLocalAddress(ch), JSON.toJSONString(connectionCounts), NettyUtils.getRemoteAddress(ch));
                        LockUtils.executeWithLock(NettyUtils.getLocalAddress(ch) + remoteClientPort, LockUtils.defaultExpireTime, (v) -> {
                            if (!CollectionUtils.isEmpty(targetProxyHandlers)) {
                                Optional<ProxyHandler> optionalProxyHandler = targetProxyHandlers.stream().filter(handler -> remoteClientPort.equals(handler.getRemoteClientPort())).findAny();
                                if (optionalProxyHandler.isPresent()) {
                                    ch.pipeline().addLast(optionalProxyHandler.get());
                                    return;
                                }
                            }
                            ProxyHandler proxyHandler = new ProxyHandler(remoteClientPort, handlerWorkerGroup, executorGroup, getNewTarget(), getReconnectTime(), putCustomizeTargetChannelHandler(), getTargetServers(targetConnections));
                            proxyHandler.setProxyHandlers(targetProxyHandlers);
                            targetProxyHandlers.add(proxyHandler);
                            ch.pipeline().addLast(proxyHandler);
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG)).fireChannelReadComplete();
                        }, 5, 1500);

                    }

                    Map<String, String> getTargetServers(List<String[]> targetConnections) {
                        if (CollectionUtils.isEmpty(targetConnections)) {
                            return Collections.EMPTY_MAP;
                        }
                        return targetConnections.stream().collect(Collectors.toMap(strings -> strings[1], strings -> strings[0]));
                    }

                    private Supplier<Map<String, String>> getNewTarget() {
                        return () -> {
                            setTarget();
                            return getTargetServers(targetConnections);
                        };
                    }

                    private Supplier<Integer> getReconnectTime() {
                        targetConnections = getConnections.apply(remoteClientPort);
                        return () -> 5 * targetConnections.size();
                    }

                    /**
                     *
                     * @return clientPort - servers
                     */
                    Map<String, List<String>> getTargetServers() {
                        if (CollectionUtil.isEmpty(targetConnections)) {
                            return Collections.emptyMap();
                        }
                        return targetConnections.stream()
                                .collect(Collectors.groupingBy(
                                        arr -> arr[0], // key是数组的第一个元素
                                        Collectors.mapping(arr -> arr[1], Collectors.toList()) // value是收集数组第二个元素的列表
                                ));
                    }

                    //判断是否能够建立有效连接
                    private boolean setTarget() {
                        targetConnections = getConnections.apply(remoteClientPort);
                        if (CollectionUtils.isEmpty(targetConnections)) {
                            return false;
                        }
                        return true;
                    }
                });
        return bootstrap;

    }


    @PreDestroy
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
