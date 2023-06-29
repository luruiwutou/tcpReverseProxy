package com.forward.core.proxy.myServer;

import com.forward.core.proxy.channel.ServerChannelInitializer;
import com.forward.core.proxy.myClient.AbstractClientAndServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

public class MyServer extends AbstractClientAndServer {

    private SocketAddress socketAddress;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ServerBootstrap bootstrap;

    List<InetSocketAddress> socketServerAddressList = null;

    public MyServer() {
    }

    public MyServer(List<InetSocketAddress> socketServerAddressList) {
        this.socketServerAddressList = socketServerAddressList;
    }

    /**
     * 初始化 eventgrouploop，bootstrap
     */
    @Override
    public void init() {
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
        this.bootstrap = new ServerBootstrap()
                .group(bossGroup,workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,1024)
                .childOption(ChannelOption.SO_KEEPALIVE,true)
                .childHandler(new ServerChannelInitializer<>());
        setWorkerGroup(workerGroup);
        setBossGroup(bossGroup);
        setServerBootstrap(bootstrap);
    }

    @Override
    public void connect() {
        try {
            ChannelFuture channelFuture1 = getServerBootstrap().bind(socketServerAddressList.get(0)).sync();
            Channel channel1 = channelFuture1.channel();
            setServerChannel(channel1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connect2() {
        try {
            ChannelFuture channelFuture1 = getServerBootstrap().bind(socketServerAddressList.get(1)).sync();
            Channel channel1 = channelFuture1.channel();
            setServerChannel(channel1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connect3() {
        try {
            ChannelFuture channelFuture1 = getServerBootstrap().bind(socketServerAddressList.get(2)).sync();
            Channel channel1 = channelFuture1.channel();
            setServerChannel(channel1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}