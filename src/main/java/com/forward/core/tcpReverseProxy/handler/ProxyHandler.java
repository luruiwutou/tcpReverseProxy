package com.forward.core.tcpReverseProxy.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFutureListener;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

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
    private ConcurrentLinkedDeque<Object> tempMsgQueue = new ConcurrentLinkedDeque<>();
    private Function<String, String> getNewTarget;
    private List<ProxyHandler> proxyHandlers;
    private volatile boolean isShutDown = false;
    private volatile boolean shouldReconnect = true;

    public ProxyHandler(String targetHost, int targetPort, Map<String, Integer> connectionCounts, Map<String, List<ProxyHandler>> targetProxyHandler, Function<String, String> getNewTarget) {
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.connectionCounts = connectionCounts; // 使用线程安全的 ConcurrentHashMap
        this.workerGroup = new NioEventLoopGroup();
        this.executorGroup = new DefaultEventExecutorGroup(16);
        this.clientChannels = new DefaultChannelGroup(workerGroup.next());
        this.getNewTarget = getNewTarget;
        if (CollectionUtils.isEmpty(targetProxyHandler.get(targetHost + ":" + targetPort))) {
            proxyHandlers = new ArrayList<>();
        } else {
            proxyHandlers = targetProxyHandler.get(targetHost + ":" + targetPort);
        }
        proxyHandlers.add(this);
        targetProxyHandler.put(targetHost + ":" + targetPort, proxyHandlers);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        clientChannels.add(ctx.channel());
        if (targetChannel != null && targetChannel.isActive()) {
            while (!tempMsgQueue.isEmpty()) {
                executorGroup.submit(() -> targetChannel.writeAndFlush(tempMsgQueue.pop()));
            }
            log.info("Received from {} send to {}:{}", ctx.channel().remoteAddress().toString().replace("/", ""), targetHost, targetPort);
        } else {
            // Target channel is not active, handle accordingly (e.g., buffer the message)
            connectToTarget();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        shouldReconnect = true;
        if (targetChannel != null && targetChannel.isActive()) {
            while (!tempMsgQueue.isEmpty()) {
                executorGroup.submit(() -> targetChannel.writeAndFlush(tempMsgQueue.pop()));
            }
            log.info("Received from {} send to {}:{}", ctx.channel().remoteAddress().toString().replace("/", ""), targetHost, targetPort);
            executorGroup.submit(() -> targetChannel.writeAndFlush(msg));
        } else {
            // Target channel is not active, handle accordingly (e.g., buffer the message)
            if (tempMsgQueue.size() < 10)
                tempMsgQueue.add(msg);
            connectToTarget();
        }
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
        if (targetChannel != null&&!targetChannel.isActive()) {
            shouldReconnect = false;
            targetChannel.close();
            targetChannel = null;
        }
        //非连接池方式
        initClientBootstrap();
    }


    public Bootstrap initClientBootstrap() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                //当与转发目标服务器进行连接的时候，把当前附表的channel赋值给targetChannel
                                targetChannel = ctx.channel();
                                // 增加连接数
                                String targetServerId = targetHost + ":" + targetPort;
                                int connectionCount = connectionCounts.getOrDefault(targetServerId, 0);
                                connectionCounts.put(targetServerId, connectionCount + 1);
                                while (!tempMsgQueue.isEmpty()) {
                                    executorGroup.submit(() -> targetChannel.writeAndFlush(tempMsgQueue.pop()));
                                }

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
                                String targetServerId = targetHost + ":" + targetPort;
                                int connectionCount = connectionCounts.getOrDefault(targetServerId, 0);
                                if (connectionCount > 0) {
                                    connectionCounts.put(targetServerId, connectionCount - 1);
                                }
                                if (clientChannels.isEmpty()) {
                                    return;
                                }
                                log.info("目标不可用：{}，尝试重连！", targetServerId);
                                ctx.close();
                                if (shouldReconnect)
                                    reconnectToTarget();
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                cause.printStackTrace();
                                ctx.close();
                                log.info("目标异常：{}，异常信息：{}，尝试重连！", targetHost + ":" + targetPort, cause);
                                reconnectToTarget();
                            }
                        });
                    }
                });
        ChannelFuture future = bootstrap.connect(targetHost, targetPort);

        future.addListener((ChannelFutureListener) future1 -> {
            if (future1.isSuccess()) {
                log.info("Connected to target: {}:{} success", targetHost, targetPort);
            } else {
                log.info("Failed to connect to target: {}:{}", targetHost, targetPort);
                reconnectToTarget();
            }
        });
        return bootstrap;
    }

    private AtomicInteger reconnectTimes = new AtomicInteger(0);

    private void reconnectToTarget() {
        workerGroup.schedule(() -> {
            int increment = reconnectTimes.getAndIncrement();
            if (increment <= 10) {
                if (increment % 2 == 1) {
                    String newTarget = getNewTarget.apply(targetHost + ":" + targetPort);
                    String[] split = newTarget.split(":");
                    targetHost = split[0];
                    targetPort = Integer.valueOf(split[1]);
                }
                connectToTarget();
            } else {
                reconnectTimes.set(0);
            }
        }, 2, TimeUnit.SECONDS);
    }

    public void shutdown() {
        if (isShutDown)
            return;
        clientChannels.close().addListener((ChannelGroupFutureListener) future -> {
            if (targetChannel != null)
                shouldReconnect = false;
                targetChannel.close();
            if (!workerGroup.isShuttingDown())
                workerGroup.shutdownGracefully();
            if (!CollectionUtils.isEmpty(tempMsgQueue))
                tempMsgQueue.forEach(msg -> ReferenceCountUtil.release(msg));
            if (!executorGroup.isShuttingDown())
                executorGroup.shutdownGracefully();
            isShutDown = true;
        });
    }

}
