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

import java.util.concurrent.TimeUnit;

public class ProxyHandler extends ChannelInboundHandlerAdapter {
    private String targetHost;
    private int targetPort;
    private Channel targetChannel;
    private ChannelGroup clientChannels;
    private EventLoopGroup workerGroup;
    private EventExecutorGroup executorGroup;

    public ProxyHandler(String targetHost, int targetPort) {
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.workerGroup = new NioEventLoopGroup();
        this.executorGroup = new DefaultEventExecutorGroup(16);
        this.clientChannels = new DefaultChannelGroup(workerGroup.next());
        connectToTarget();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        clientChannels.add(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (targetChannel != null && targetChannel.isActive()) {
//            executorGroup.submit(() -> targetChannel.writeAndFlush(msg));
            targetChannel.writeAndFlush(msg);
        } else {
            // Target channel is not active, handle accordingly (e.g., buffer the message)
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        clientChannels.remove(ctx.channel());
        reconnectToTarget();
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
                                targetChannel = ctx.channel();
                            }

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                // 将耗时操作委托给 executorGroup 处理
//                                executorGroup.submit(() -> clientChannels.writeAndFlush(msg));
                                clientChannels.writeAndFlush(msg);
                            }

                            @Override
                            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
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

    public void shutdown() {
        clientChannels.close().addListener((ChannelGroupFutureListener) future -> {
            workerGroup.shutdownGracefully();
            executorGroup.shutdownGracefully();
        });
    }
}
