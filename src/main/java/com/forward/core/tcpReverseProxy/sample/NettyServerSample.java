package com.forward.core.tcpReverseProxy.sample;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServerSample {

    private int port;

    public NettyServerSample(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                     .channel(NioServerSocketChannel.class)
                     .childHandler(new ChannelInitializer<SocketChannel>() {
                         @Override
                         public void initChannel(SocketChannel ch) throws Exception {
                             ch.pipeline().addLast(new ChannelInitializer<SocketChannel>() {
                                 @Override
                                 protected void initChannel(SocketChannel socketChannel) throws Exception {
                                     
                                 }
                             }); // 自定义处理器
                         }
                     });

            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 9991; // 服务器监听端口
        NettyServerSample server = new NettyServerSample(port);
        server.run();
        int port1 = 9992; // 服务器监听端口
        NettyServerSample server1 = new NettyServerSample(port1);
        server.run();
    }
}
