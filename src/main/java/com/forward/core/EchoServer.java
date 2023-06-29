package com.forward.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.Charset;
import java.util.List;

public class EchoServer {

    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(port)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 添加字符串编码器
                            pipeline.addLast(new StringEncoder(Charset.forName("UTF-8")));
                            // 添加字符串解码器
                            pipeline.addLast(new StringDecoder(Charset.forName("UTF-8")));

                            pipeline.addLast(new EchoServerHandler());
                        }
                    });

            ChannelFuture future = bootstrap.bind().sync();
            System.out.println("EchoServer started and listening on " + future.channel().localAddress());

            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws Exception {

        new EchoServer(17777).start();
    }

    private static class EchoServerHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("接收到客户端消息：" + new String(msg.toString().getBytes(),"UTF-8"));
            String response = "服务端已经接收到你的消息：" + msg;
            // 将响应消息写入管道
            ctx.writeAndFlush(response);
        }

    }
}
