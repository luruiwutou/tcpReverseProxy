package com.forward.core.tcpReverseProxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ProxyHandler_release1 extends ChannelInboundHandlerAdapter {
    private String targetHost;
    private int targetPort;
    private Channel targetChannel;
    private Channel clientChannel;
    private EventLoopGroup workerGroup;

    public ProxyHandler_release1(String targetHost, int targetPort) {
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.workerGroup = new NioEventLoopGroup();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        clientChannel = ctx.channel();
        connectToTarget();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (targetChannel != null && targetChannel.isActive()) {
            targetChannel.writeAndFlush(msg);
        } else {
            // todo Target channel is not active, handle accordingly (e.g., buffer the message)
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (targetChannel != null && targetChannel.isActive()) {
            targetChannel.close();
        }
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
                                clientChannel.writeAndFlush(msg);
                            }

                            @Override
                            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                reconnectToTarget();
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                cause.printStackTrace();
                                clientChannel.close();
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
        if (targetChannel != null && targetChannel.isActive()) {
            targetChannel.close();
        }
        targetChannel = null;
        connectToTarget();
    }
}
