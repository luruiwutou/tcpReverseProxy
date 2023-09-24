package com.forward.core.tcpReverseProxy.handler;

import cn.hutool.core.collection.CollectionUtil;
import com.forward.core.constant.Constants;
import com.forward.core.sftp.utils.StringUtil;
import com.forward.core.tcpReverseProxy.redis.RedisService;
import com.forward.core.tcpReverseProxy.utils.LockUtils;
import com.forward.core.tcpReverseProxy.utils.SingletonBeanFactory;
import com.forward.core.tcpReverseProxy.utils.SnowFlake;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFutureListener;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@ChannelHandler.Sharable
public class ProxyHandler extends ChannelInboundHandlerAdapter {
    private String targetHost;
    private int targetPort;
    private String clientPort;
    private volatile Channel targetChannel;
    private ChannelGroup clientChannels;
    private EventLoopGroup workerGroup;
    private EventExecutorGroup executorGroup;
    //每一个目标服务器开了多少个channel的计数
    private Map<String, Integer> connectionCounts;
    private Function<String[], String[]> getNewTarget;
    private Supplier<Integer> getReconnectTime;
    private ConcurrentLinkedQueue<ProxyHandler> proxyHandlers;
    private Consumer<SocketChannel> customizeHandlerMapCon;
    private volatile boolean isShutDown = false;
    private volatile boolean shouldReconnect = true;
    private volatile boolean connecting = false;

    public ProxyHandler(String clientPort, String targetHost, int targetPort, Map<String, Integer> connectionCounts, EventLoopGroup workerGroup, EventExecutorGroup executorGroup, ConcurrentLinkedQueue targetProxyHandler, Function<String[], String[]> getNewTarget, Supplier<Integer> getReconnectTime, Consumer<SocketChannel> putCustomizeTargetChannelHandler) {
        this.clientPort = clientPort;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.customizeHandlerMapCon = putCustomizeTargetChannelHandler;
        this.connectionCounts = connectionCounts; // 使用线程安全的 ConcurrentHashMap
        this.workerGroup = workerGroup;
        this.executorGroup = executorGroup;
        this.clientChannels = new DefaultChannelGroup(this.workerGroup.next());
        this.getNewTarget = getNewTarget;
        this.getReconnectTime = getReconnectTime;
        if (Objects.isNull(targetProxyHandler)) {
            targetProxyHandler = new ConcurrentLinkedQueue<>();
        }
        targetProxyHandler.add(this);
        proxyHandlers = targetProxyHandler;
    }


    RedisService getRedisService() {
        return SingletonBeanFactory.getSpringBeanInstance(RedisService.class).getSingleton();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        if (StringUtil.isNotEmpty(ctx.channel().attr(Constants.TRACE_ID_KEY).get())) {
            MDC.put(Constants.TRACE_ID, ctx.channel().attr(Constants.TRACE_ID_KEY).get());
        }
        if (StringUtil.isEmpty(MDC.get(Constants.TRACE_ID))) {
            String traceId = SnowFlake.getTraceId();
            MDC.put(Constants.TRACE_ID, traceId);
        }
        shouldReconnect = true;
        clientChannels.add(ctx.channel());
        log.info("targetChannelActive: {}", targetChannel);
        if (targetChannel != null && targetChannel.isActive()) {
            readMsgCache(getHostStr(ctx.channel().localAddress()), targetChannel);
            log.info("Received from remote client {} send to {}", getHostStr(ctx.channel().remoteAddress()), getTargetServerAddress());
        } else {
            // Target channel is not active, handle accordingly (e.g., buffer the message)3
            log.info("active count connectToTarget");
            if (!connecting) {
                connectToTarget();
                connecting=true;
            }
        }
        MDC.remove(Constants.TRACE_ID);
    }

    private static String getHostStr(SocketAddress socketAddress) {
        return socketAddress.toString().split("/")[1];
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (StringUtil.isNotEmpty(ctx.channel().attr(Constants.TRACE_ID_KEY).get())) {
            MDC.put(Constants.TRACE_ID, ctx.channel().attr(Constants.TRACE_ID_KEY).get());
        }
        if (StringUtil.isEmpty(MDC.get(Constants.TRACE_ID))) {
            String traceId = SnowFlake.getTraceId();
            MDC.put(Constants.TRACE_ID, traceId);
        }
        shouldReconnect = true;
        String hostStr = getHostStr(ctx.channel().localAddress());
//        log.info("targetChannel:{}",targetChannel);
        if (targetChannel != null && targetChannel.isActive()) {
            log.info("Received from {} send to {}", getHostStr(ctx.channel().remoteAddress()), getTargetServerAddress());
            getReadConsumer(targetChannel).accept(msg);
//            executorGroup.submit(() -> targetChannel.writeAndFlush(msg));
            readMsgCache(hostStr, targetChannel);
        } else {
            log.info("target channel is not active {}", targetChannel);
            // Target channel is not active, handle accordingly (e.g., buffer the message)
            Long listSize = getListSize(hostStr);
            if (listSize != null && listSize < 1000) writeMsgCache(hostStr, msg);
            log.info("Connecting to target ,connecting:{}", connecting);
            if (!connecting) {
                connectToTarget();
                connecting=true;
            }
        }
        MDC.remove(Constants.TRACE_ID);
        ctx.channel().attr(Constants.TRACE_ID_KEY).set(null);
    }

    private Long getListSize(String hostStr) throws Exception {
        try {
            return LockUtils.executeWithLock(clientChannels.stream().findAny().get().id().asLongText(), () -> getRedisService().getListSize(hostStr));
        } catch (Exception e) {
            throw new Exception(e);
        }
    }


    private void writeMsgCache(String hostStr, Object msg) throws Exception {
        LockUtils.executeWithLock(clientChannels.stream().findAny().get().id().asLongText(), (v) -> {
            getRedisService().writeMsgCache(hostStr, msg);
        });
    }

    private void readMsgCache(String hostStr, Channel channel) {
        try {
            LockUtils.executeWithLock(clientChannels.stream().findAny().get().id().asLongText(), (v) -> {
                Long listSize = getRedisService().getListSize(hostStr);
                if (null == listSize || listSize == 0) return;
                log.info("read msg cache from redis ,send to {}", getTargetServerAddress());
                getRedisService().readMsgCache(hostStr, getReadConsumer(channel));
            });
        } catch (Exception e) {
            log.error("read cache error：", e);
        }
    }

    private Consumer getReadConsumer(Channel channel) {
        return msg -> {
            if (msg instanceof ByteBuf) {
                log.info("{} send to target :{},Hex msg:{}", getHostStr(channel.localAddress()), getTargetServerAddress(), ByteBufUtil.hexDump((ByteBuf) msg));
            }
            executorGroup.submit(() -> channel.writeAndFlush(msg));
        };
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        whenExceptionOrClose(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isWritable()) {
            // 当缓冲区可写时，再次尝试写入数据
//            writeMessage(channel);
            readMsgCache(getTargetServerAddress(), ctx.channel());
        }
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        whenExceptionOrClose(ctx);
    }

    private void whenExceptionOrClose(ChannelHandlerContext ctx) {
        if (StringUtil.isNotEmpty(ctx.channel().attr(Constants.TRACE_ID_KEY).get())) {
            MDC.put(Constants.TRACE_ID, ctx.channel().attr(Constants.TRACE_ID_KEY).get());
        }
        if (StringUtil.isEmpty(MDC.get(Constants.TRACE_ID))) {
            String traceId = SnowFlake.getTraceId();
            MDC.put(Constants.TRACE_ID, traceId);
        }
        if (clientChannels.contains(this)) {
            clientChannels.remove(this);
        }
        ctx.close();
        if (!clientChannels.isEmpty()) {
            return;
        }
        if (proxyHandlers.contains(this)) {
            proxyHandlers.remove(this);
        }
        shutdown();
        MDC.get(Constants.TRACE_ID);
    }

    public void connectToTarget() {
        try {
            log.info("Lock connecting to target,lock key：{}", this.hashCode() + clientChannels.name());
            String traceId = MDC.get(Constants.TRACE_ID);
            LockUtils.executeWithLock(this.hashCode() + clientChannels.name(), (v) -> {
                if (StringUtil.isEmpty(MDC.get(Constants.TRACE_ID))) {
                    MDC.put(Constants.TRACE_ID, traceId);
                }
                if (targetChannel != null && !targetChannel.isActive()) {
                    targetChannel = null;
                }
                //非连接池方式
                if (shouldReconnect) initClientBootstrap();
            });
        } catch (Exception e) {
            log.info("加锁执行失败，原因：{}", e);
        }


    }

    final static Random random = new Random();

    public void initClientBootstrap() {
        log.info("Connecting :{}", connecting);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true) // 设置 TCP_NODELAY 选项
                .option(ChannelOption.SO_KEEPALIVE, true) // 设置 SO_KEEPALIVE 选项
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        log.info("initializing target channel");
                        if (StringUtil.isEmpty(ch.attr(Constants.TRACE_ID_KEY).get())) {
                            ch.attr(Constants.TRACE_ID_KEY).set(StringUtil.isEmpty(MDC.get(Constants.TRACE_ID)) ? SnowFlake.getTraceId() : MDC.get(Constants.TRACE_ID));
                        }
                        customizeHandlerMapCon.accept(ch);
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                //当与转发目标服务器进行连接的时候，把当前附表的channel赋值给targetChannel
                                targetChannel = ctx.channel();
                                // 增加连接数
                                String targetServerId = getTargetServerAddress();
                                int connectionCount = connectionCounts.getOrDefault(targetServerId, 0);
                                connectionCounts.put(targetServerId, connectionCount + 1);
                                if (clientChannels.isEmpty()) {
                                    return;
                                }
                                clientChannels.forEach(ch -> readMsgCache(getHostStr(ch.localAddress()), ctx.channel()));
                                MDC.remove(Constants.TRACE_ID);
                            }

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                // 转发消息到客户端
                                // 将耗时操作委托给 executorGroup 处理
                                if (StringUtil.isEmpty(MDC.get(Constants.TRACE_ID))) {
                                    String traceId = SnowFlake.getTraceId();
                                    MDC.put(Constants.TRACE_ID, traceId);
                                }
                                if (clientChannels.isEmpty()) {
                                    ctx.close();
                                    return;
                                }
                                sendByClientChannels(msg);
                                MDC.remove(Constants.TRACE_ID);
                                ctx.channel().attr(Constants.TRACE_ID_KEY).set(null);
                            }

                            private void sendByClientChannels(Object msg) {
                                List<Channel> collect = clientChannels.stream().filter(Channel::isWritable).collect(Collectors.toList());
                                if (CollectionUtils.isEmpty(collect)) {
                                    log.info("channelGroup {} is not writable", clientChannels.name());
                                    if (getRedisService().isRedisConnected()) {
                                        String longText = clientChannels.stream().findAny().get().id().asLongText();
                                        log.info("write msg to redis ,channel id :{} ", longText);
                                        getRedisService().writeMsgCache(longText, msg);
                                    }
                                }
                                if (msg instanceof ByteBuf) {
                                    ByteBuf buf = (ByteBuf) msg;
                                    if (buf.readableBytes() == 4) {
                                        log.info("receive from {} write msg: {} to every client", getTargetServerAddress(), ByteBufUtil.hexDump(buf));
                                        executorGroup.submit(() -> {
                                            clientChannels.writeAndFlush(msg);
                                        });
                                        return;
                                    }
                                }
                                int randomIndex = random.nextInt(collect.size());
                                Channel randomChannel = collect.get(randomIndex);
                                String clientAddress = getHostStr(randomChannel.remoteAddress());
                                log.info("client channel {} is writeable, {}", clientAddress, randomChannel);
                                String traceId = MDC.get(Constants.TRACE_ID);
                                executorGroup.submit(() -> {
                                    if (StringUtil.isEmpty(MDC.get(Constants.TRACE_ID))) {
                                        MDC.put(Constants.TRACE_ID, traceId);
                                    }                                    // 转发消息到客户端
                                    if (msg instanceof ByteBuf) {
                                        log.info("received from target server {},return to {},Hex msg:{}", getTargetServerAddress(), clientAddress, ByteBufUtil.hexDump((ByteBuf) msg));
                                    }
                                    log.info("client channel {} is active", clientAddress);
                                    randomChannel.writeAndFlush(msg);
                                    MDC.remove(Constants.TRACE_ID);
                                });
                            }

                            @Override
                            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                String traceId = SnowFlake.getTraceId();
                                MDC.put(Constants.TRACE_ID, traceId);
                                if (StringUtil.isNotEmpty(clientPort) && Constants.LOCAL_PORT_RULE_SINGLE.equals(clientPort)) {
                                    log.info("clientPort is {} , means not to reconnect", clientPort);
                                    shouldReconnect = false;
                                }
                                log.info("closed shouldReconnect : {}, target channel :{} inactive", shouldReconnect, getHostStr(ctx.channel().localAddress()));
                                // 更新连接数
                                String targetServerId = getTargetServerAddress();
                                int connectionCount = connectionCounts.getOrDefault(targetServerId, 0);
                                if (connectionCount > 0) {
                                    connectionCounts.put(targetServerId, connectionCount - 1);
                                }
                                log.info("本地地址：{} ，目标：{}不可用，{} 重连！", getHostStr(ctx.channel().localAddress()), targetServerId, shouldReconnect ? "尝试" : "无需");
                                ctx.close();
                                targetChannel = null;
                                if (clientChannels.isEmpty()) {
                                    return;
                                }
                                connecting=false;
                                if (shouldReconnect) reconnectToTarget();
                                MDC.remove(Constants.TRACE_ID);
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                if (StringUtil.isEmpty(MDC.get(Constants.TRACE_ID))) {
                                    String traceId = SnowFlake.getTraceId();
                                    MDC.put(Constants.TRACE_ID, traceId);
                                }
                                cause.printStackTrace();
                                ctx.close();
                                log.info("目标异常：{}，异常信息：{}，尝试重连！", getTargetServerAddress(), cause);
                                connecting=false;
                                reconnectToTarget();
                                MDC.remove(Constants.TRACE_ID);
                            }
                        });
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG)).fireChannelReadComplete();
                    }
                });
        if (!StringUtil.isEmpty(clientPort) && !Constants.LOCAL_PORT_RULE_SINGLE.equals(clientPort))
            try {
                bootstrap.localAddress(Integer.valueOf(clientPort));
            } catch (Exception e) {
                log.info("客户端绑定发送端口失败", e);
            }

        ChannelFuture future = bootstrap.connect(targetHost, targetPort);
        String traceId = MDC.get(Constants.TRACE_ID);
        future.addListener((ChannelFutureListener) future1 -> {
            if (StringUtil.isEmpty(MDC.get(Constants.TRACE_ID))) {
                MDC.put(Constants.TRACE_ID, traceId);
            }
            if (future1.isSuccess()) {
                reconnectTimeCount.set(1);
                log.info("local client:{} Connected to target: {} success", getHostStr(future1.channel().localAddress()), getTargetServerAddress());
            } else {
                log.info("remote client:{} Failed to connect to target: {} ,try times: {}\n error msg:{}", clientChannels.isEmpty() ? null : getHostStr(clientChannels.stream().findAny().get().remoteAddress()), getTargetServerAddress(), reconnectTimeCount, future1.cause().getMessage());
                log.error("local client:{} started failed cause:", clientPort, future1.cause());
                reconnectToTarget();
            }
            MDC.remove(Constants.TRACE_ID);
        });
    }

    public String getTargetServerAddress() {
        return targetHost + ":" + targetPort;
    }

    private AtomicInteger reconnectTimeCount = new AtomicInteger(1);

    private void reconnectToTarget() {
        resetTargetAddress();
        workerGroup.schedule(() -> {
            log.info("----重连");
            if(!connecting){
                connectToTarget();
                connecting=true;
            }

        }, 2, TimeUnit.SECONDS);
    }

    private void resetTargetAddress() {
        int increment = reconnectTimeCount.incrementAndGet();
        //只重连6次
        if (increment <= getReconnectTime.get()) {
            String[] newTarget = getNewTarget.apply(new String[]{clientPort, getTargetServerAddress()});
            clientPort = newTarget[0];
            String[] split = newTarget[1].split(":");
            targetHost = split[0];
            targetPort = Integer.valueOf(split[1]);
        } else {
            reconnectTimeCount.set(1);
            shouldReconnect = false;
        }
    }

    public void shutdown() {
        if (isShutDown) return;
        clientChannels.close().addListener((ChannelGroupFutureListener) future -> {
            if (targetChannel != null) {
                shouldReconnect = false;
                targetChannel.close();
            }
//            if (!workerGroup.isShuttingDown()) {
//                workerGroup.shutdownGracefully();
//            }
//            if (!executorGroup.isShuttingDown()) {
//                executorGroup.shutdownGracefully();
//            }
            isShutDown = true;
        });
    }

    public String getClientPort() {
        return clientPort;
    }

    public void initiativeReconnected() {
        //利用自动重连机制，主动关闭channel，让其重连
        if (null != targetChannel) {
            log.info("close target channel :localAddress :{},RemoteAddress:{}", getHostStr(targetChannel.localAddress()), getHostStr(targetChannel.remoteAddress()));
            targetChannel.close();
            targetChannel = null;
        }
        resetTargetAddress();
    }
}