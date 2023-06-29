package com.forward.core.tcpReverseProxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFutureListener;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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
    //这一台代理服务器需要转发的目的服务器ip、端口
    private String[][] targetConnections={{"localhost","8881"},{"localhost", "8882"},{"localhost", "8883"}};

    public ProxyHandler(String targetHost, int targetPort) {
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.workerGroup = new NioEventLoopGroup();
        this.executorGroup = new DefaultEventExecutorGroup(16);
        this.clientChannels = new DefaultChannelGroup(workerGroup.next());
        this.connectionCounts = new ConcurrentHashMap<>(); // 使用线程安全的 ConcurrentHashMap
        connectToTarget();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        clientChannels.add(ctx.channel());

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (targetChannel != null && targetChannel.isActive()) {
            executorGroup.submit(() -> targetChannel.writeAndFlush(msg));
//            targetChannel.writeAndFlush(msg);
        } else {
            // Target channel is not active, handle accordingly (e.g., buffer the message)
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 减少连接数
        String targetServerId = targetHost + ":" + targetPort;
        int connectionCount = connectionCounts.getOrDefault(targetServerId, 0);
        if (connectionCount > 0) {
            connectionCounts.put(targetServerId, connectionCount - 1);
        }
        clientChannels.remove(ctx.channel());
//        reconnectToTarget();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private void connectToTarget() {
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
                            }

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                // 将耗时操作委托给 executorGroup 处理
//                                executorGroup.submit(() -> clientChannels.writeAndFlush(msg));
//                                clientChannels.writeAndFlush(msg);
                                // 转发消息到客户端
                                // 将耗时操作委托给 executorGroup 处理
                                executorGroup.submit(() -> {
                                    // 转发消息到客户端
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
                                reconnectToTarget();
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                cause.printStackTrace();
                                clientChannels.close();
                            }
                        });
                    }
                });

        ChannelFuture future = bootstrap.connect(targetHost, targetPort);
        future.addListener((ChannelFutureListener) future1 -> {
            if (future1.isSuccess()) {
                System.out.println("Connected to target: " + targetHost + ":" + targetPort);
            } else {
                System.err.println("Failed to connect to target: " + targetHost + ":" + targetPort);
                reconnectToTarget();
            }
        });
    }

    private void reconnectToTarget() {
        workerGroup.schedule(this::connectToTarget, 5, TimeUnit.SECONDS);
    }

    //    private void forwardToClients(Object msg) {
//        // 寻找连接数最少的目标服务器
//        String targetServerId = null;
//        int minConnectionCount = Integer.MAX_VALUE;
//        for (Map.Entry<String, Integer> entry : connectionCounts.entrySet()) {
//            if (entry.getValue() < minConnectionCount) {
//                targetServerId = entry.getKey();
//                minConnectionCount = entry.getValue();
//            }
//        }
//
//        // 转发消息给连接数最少的目标服务器的客户端
//        if (targetServerId != null) {
//            String finalTargetServerId = targetServerId;
//            clientChannels.writeAndFlush(msg, channel -> {
//                String channelId = channel.remoteAddress().toString();
//                return channelId.contains(finalTargetServerId);
//            }).addListener((ChannelGroupFutureListener) future -> {
//                // Handle the response from the target server (e.g., log, process, etc.)
//                log.info("转发了");
//            });
//        }
//    }
    private void setTarget() {
        // 寻找连接数最少的目标服务器
        String targetServerId = null;
        int minConnectionCount = Integer.MAX_VALUE;
        for (Map.Entry<String, Integer> entry : connectionCounts.entrySet()) {
            if (entry.getValue() < minConnectionCount) {
                targetServerId = entry.getKey();
                minConnectionCount = entry.getValue();
            }
        }
        if (targetServerId != null) {
            String[] split = targetServerId.split(":");
            targetHost = split[0];
            targetPort = Integer.valueOf(split[1]);
        }
    }


    public void shutdown() {
        clientChannels.close().addListener((ChannelGroupFutureListener) future -> {
            workerGroup.shutdownGracefully();
            executorGroup.shutdownGracefully();
        });
    }
}
