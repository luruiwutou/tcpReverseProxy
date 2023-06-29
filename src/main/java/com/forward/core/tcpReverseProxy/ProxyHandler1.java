package com.forward.core.tcpReverseProxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ProxyHandler1 extends ChannelInboundHandlerAdapter {
    private String targetHost;
    private int targetPort;
    private ChannelHandlerContext targetContext;
    private ChannelHandlerContext clientContext;

    public ProxyHandler1(String targetHost, int targetPort) {
        this.targetHost = targetHost;
        this.targetPort = targetPort;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        clientContext = ctx;

        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                 .channel(NioSocketChannel.class)
                 .handler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     protected void initChannel(SocketChannel ch) throws Exception {
                         ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                             @Override
                             public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                 targetContext = ctx;
                                 clientContext.read();
                             }

                             @Override
                             public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                 clientContext.writeAndFlush(msg);
                             }

                             @Override
                             public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                 clientContext.channel().close();
                             }

                             @Override
                             public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                 cause.printStackTrace();
                                 clientContext.close();
                             }
                         });
                     }
                 });

        ChannelFuture future = bootstrap.connect(targetHost, targetPort).sync();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        targetContext.writeAndFlush(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (targetContext != null) {
            targetContext.channel().close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
