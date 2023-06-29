package com.forward.core.tcpReverseProxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ReverseProxyClient1 {
    private String remoteHost;
    private int remotePort;
    private String targetHost;
    private int targetPort;

    public ReverseProxyClient1(String remoteHost, int remotePort, String targetHost, int targetPort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                     .channel(NioSocketChannel.class)
                     .handler(new ChannelInitializer<SocketChannel>() {
                         @Override
                         protected void initChannel(SocketChannel ch) throws Exception {
                             ch.pipeline().addLast(new ProxyHandler1(targetHost, targetPort));
                         }
                     });

            ChannelFuture future = bootstrap.connect(remoteHost, remotePort).sync();
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        String remoteHost = "localhost";  // 代理服务器的主机名或IP地址
        int remotePort = 8887;            // 代理服务器的端口号
        String targetHost = "localhost";  // 目标服务器的主机名或IP地址
        int targetPort = 8888;            // 目标服务器的端口号

        ReverseProxyClient1 client = new ReverseProxyClient1(remoteHost, remotePort, targetHost, targetPort);
        client.start();
    }
}
