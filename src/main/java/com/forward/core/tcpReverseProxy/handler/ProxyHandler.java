package com.forward.core.tcpReverseProxy.handler;

import cn.hutool.core.util.HexUtil;
import com.forward.core.tcpReverseProxy.redis.RedisService;
import com.forward.core.tcpReverseProxy.utils.LockUtils;
import com.forward.core.tcpReverseProxy.utils.SingletonBeanFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFutureListener;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class ProxyHandler extends ChannelInboundHandlerAdapter {
    private String targetHost;
    private int targetPort;
    private Channel targetChannel;
    private ChannelGroup clientChannels;
    private EventLoopGroup workerGroup;
    private EventExecutorGroup executorGroup;
    //每一个目标服务器开了多少个channel的计数
    private Map<String, Integer> connectionCounts;
    private Function<String, String> getNewTarget;
    private Supplier<Integer> getReconnectTime;
    private ConcurrentLinkedQueue<ProxyHandler> proxyHandlers;
    private volatile boolean isShutDown = false;
    private volatile boolean shouldReconnect = true;

    public ProxyHandler(String targetHost, int targetPort, Map<String, Integer> connectionCounts, ConcurrentLinkedQueue targetProxyHandler, Function<String, String> getNewTarget, Supplier<Integer> getReconnectTime) {
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.connectionCounts = connectionCounts; // 使用线程安全的 ConcurrentHashMap
        this.workerGroup = new NioEventLoopGroup();
        this.executorGroup = new DefaultEventExecutorGroup(16);
        this.clientChannels = new DefaultChannelGroup(workerGroup.next());
        this.getNewTarget = getNewTarget;
        this.getReconnectTime = getReconnectTime;
        if (Objects.isNull(targetProxyHandler)) {
            targetProxyHandler = new ConcurrentLinkedQueue<>();
        }
        targetProxyHandler.add(this);
        proxyHandlers = targetProxyHandler;
    }


    RedisService getRedisService() {
        return SingletonBeanFactory.getBeanInstance(RedisService.class).getSingleton();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        clientChannels.add(ctx.channel());
        if (targetChannel != null && targetChannel.isActive()) {
            readMsgCache(getHostStr(ctx.channel().localAddress()));
            log.info("Received from {} send to {}:{}", getHostStr(ctx.channel().remoteAddress()), targetHost, targetPort);
        } else {
            // Target channel is not active, handle accordingly (e.g., buffer the message)
            connectToTarget();
        }
    }

    private static String getHostStr(SocketAddress socketAddress) {
        return socketAddress.toString().replace("/", "");
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        shouldReconnect = true;
        String hostStr = getHostStr(ctx.channel().localAddress());
        if (targetChannel != null && targetChannel.isActive()) {
            getReadConsumer().accept(msg);
//            executorGroup.submit(() -> targetChannel.writeAndFlush(msg));
            log.info("Received from {} send to {}", getHostStr(ctx.channel().remoteAddress()), getTargetServerAddress());
            readMsgCache(hostStr);
        } else {
            // Target channel is not active, handle accordingly (e.g., buffer the message)
            Long listSize = getListSize(hostStr);
            if (listSize != null && listSize < 1000) writeMsgCache(hostStr, msg);
            connectToTarget();
        }
    }

    private Long getListSize(String hostStr) throws Exception {
        try {
            return LockUtils.executeWithLock(clientChannels.stream().findAny().get().id().asLongText(), () -> getRedisService().getListSize(hostStr));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void writeMsgCache(String hostStr, Object msg) throws Exception {
        LockUtils.executeWithLock(clientChannels.stream().findAny().get().id().asLongText(), (v) -> getRedisService().writeMsgCache(hostStr, msg));
    }

    private void readMsgCache(String hostStr) {
        try {
            log.info("read msg cache from redis ,send to {}", getTargetServerAddress());
            LockUtils.executeWithLock(clientChannels.stream().findAny().get().id().asLongText(), (v) -> getRedisService().readMsgCache(hostStr, getReadConsumer()));
        } catch (Exception e) {
            log.error("read cache error：", e);
        }
    }

    private Consumer getReadConsumer() {
        return msg -> {
            if (msg instanceof ByteBuf) {
                byte[] bytes = new byte[((ByteBuf) msg).readableBytes()];
                ((ByteBuf) msg).readBytes(bytes);
                log.info("Hex msg:{}", HexUtil.encodeHexStr(bytes));
                ((ByteBuf) msg).resetReaderIndex();
            }
            executorGroup.submit(() -> targetChannel.writeAndFlush(msg));
        };
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (proxyHandlers.contains(this)) {
            proxyHandlers.remove(this);
        }
        ctx.close();
        shutdown();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
        if (proxyHandlers.contains(this)) {
            proxyHandlers.remove(this);
        }
        shutdown();
    }

    public void connectToTarget() {
        if (targetChannel != null && !targetChannel.isActive()) {
            shouldReconnect = false;
            targetChannel.close();
            targetChannel = null;
        }
        //非连接池方式
        initClientBootstrap();
    }


    public Bootstrap initClientBootstrap() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
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
                        clientChannels.forEach(ch -> readMsgCache(getHostStr(ch.localAddress())));
                    }

                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        // 将耗时操作委托给 executorGroup 处理
//                                executorGroup.submit(() -> clientChannels.writeAndFlush(msg));
//                                clientChannels.writeAndFlush(msg);
                        // 转发消息到客户端
                        // 将耗时操作委托给 executorGroup 处理
                        if (clientChannels.isEmpty()) {
                            ctx.close();
                            return;
                        }
                        executorGroup.submit(() -> {
                            // 转发消息到客户端
                            log.info("received from target server {}:{},return to {}", targetHost, targetPort, clientChannels.stream().findAny().get().remoteAddress());
                            clientChannels.writeAndFlush(msg);
                        });
                    }

                    @Override
                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                        // 更新连接数
                        String targetServerId = getTargetServerAddress();
                        int connectionCount = connectionCounts.getOrDefault(targetServerId, 0);
                        if (connectionCount > 0) {
                            connectionCounts.put(targetServerId, connectionCount - 1);
                        }
                        if (clientChannels.isEmpty()) {
                            return;
                        }
                        log.info("目标不可用：{}，尝试重连！", targetServerId);
                        ctx.close();
                        if (shouldReconnect) reconnectToTarget();
                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                        cause.printStackTrace();
                        ctx.close();
                        log.info("目标异常：{}，异常信息：{}，尝试重连！", getTargetServerAddress(), cause);
                        reconnectToTarget();
                    }
                });
            }
        });
        ChannelFuture future = bootstrap.connect(targetHost, targetPort);

        future.addListener((ChannelFutureListener) future1 -> {
            if (future1.isSuccess()) {
                reconnectTimeCount.set(1);
                log.info("Connected to target: {} success", getTargetServerAddress());
            } else {
                log.info("client :{} Failed to connect to target: {} ,try times: {}", getHostStr(clientChannels.stream().findAny().get().remoteAddress()), getTargetServerAddress(), reconnectTimeCount);
                reconnectToTarget();
            }
        });
        return bootstrap;
    }

    public String getTargetServerAddress() {
        return targetHost + ":" + targetPort;
    }

    private AtomicInteger reconnectTimeCount = new AtomicInteger(1);

    private void reconnectToTarget() {
        int increment = reconnectTimeCount.incrementAndGet();
        //只重连6次
        if (increment <= getReconnectTime.get()) {
            String newTarget = getNewTarget.apply(getTargetServerAddress());
            String[] split = newTarget.split(":");
            targetHost = split[0];
            targetPort = Integer.valueOf(split[1]);
            workerGroup.schedule(() -> {
                connectToTarget();
            }, 2, TimeUnit.SECONDS);
        } else {
            reconnectTimeCount.set(1);
        }
    }

    public void shutdown() {
        if (isShutDown) return;
        clientChannels.close().addListener((ChannelGroupFutureListener) future -> {
            if (targetChannel != null) {
                shouldReconnect = false;
                targetChannel.close();
            }
            if (!workerGroup.isShuttingDown()) {
                workerGroup.shutdownGracefully();
            }
            if (!executorGroup.isShuttingDown()) {
                executorGroup.shutdownGracefully();
            }
            isShutDown = true;
        });
    }

}