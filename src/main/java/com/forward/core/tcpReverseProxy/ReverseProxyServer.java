package com.forward.core.tcpReverseProxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ReverseProxyServer {
    private int localPort;
    private String targetHost;
    private int targetPort;

    public ReverseProxyServer(int localPort, String targetHost, int targetPort) {
        this.localPort = localPort;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
    }

    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                     .channel(NioServerSocketChannel.class)
                     .childHandler(new ChannelInitializer<SocketChannel>() {
                         @Override
                         protected void initChannel(SocketChannel ch) throws Exception {
                             ch.pipeline().addLast(new ProxyHandler(targetHost, targetPort));
                         }
                     });

            ChannelFuture future = bootstrap.bind(localPort).sync();
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
    private static void startProxyServer(String host, int port, ChannelHandler handler) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(handler);

            ChannelFuture future = bootstrap.bind(host, port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int localPort = 8888;             // 本地服务器的端口号
        String targetHost = "localhost";  // 目标服务器的主机名或IP地址
        int targetPort = 8889;            // 目标服务器的端口号

        ReverseProxyServer server = new ReverseProxyServer(localPort, targetHost, targetPort);

    }
}
