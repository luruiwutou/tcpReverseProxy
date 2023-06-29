package com.forward.core.proxy.myClient;

import com.forward.core.proxy.channel.ClientChannel;
import com.forward.core.proxy.listener.ClientListener;
import com.forward.core.proxy.listener.ClientListener2;
import com.forward.core.proxy.listener.ClientListener3;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

public class MyClient extends AbstractClientAndServer{
    NioEventLoopGroup eventExecutors = null;
    Bootstrap bootstrap = null;
    List<InetSocketAddress> socketAddressList = null;
    public MyClient(){

    }

    public MyClient(List<InetSocketAddress> socketAddressList) throws InterruptedException {
        this.socketAddressList = socketAddressList;
    }

    @Override
    public void init() {
        eventExecutors = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventExecutors)
                //设置客户端的通道实现类型
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                //使用匿名内部类初始化通道
                .handler(new ClientChannel());
        setBootstrap(bootstrap);
        setEventExecutors(eventExecutors);
    }

    @Override
    public void connect() {
        try {
            //连接服务端
                InetSocketAddress address = socketAddressList.get(0);
                if (address!=null){
                    ChannelFuture channelFuture = getBootstrap().connect(address);
  //                  对通道关闭进行监听
                    channelFuture.addListener(new ClientListener(this,address));
                    Channel channel = channelFuture.channel();
                    setChannel(channel);
                    channel.closeFuture();
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connect2() {
        InetSocketAddress address = socketAddressList.get(1);
        if (address != null){
            ChannelFuture channelFuture = getBootstrap().connect(address);
            //对通道关闭进行监听
            channelFuture.addListener(new ClientListener2(this,address));
            Channel channel = channelFuture.channel();
            setChannel(channel);
            channel.closeFuture();
        }

    }

    @Override
    public void connect3() {
        InetSocketAddress address = socketAddressList.get(2);
        if (address != null){
            ChannelFuture channelFuture = getBootstrap().connect(address);
            //对通道关闭进行监听
            channelFuture.addListener(new ClientListener3(this,address));
            Channel channel = channelFuture.channel();
            setChannel(channel);
            channel.closeFuture();
        }
    }
}