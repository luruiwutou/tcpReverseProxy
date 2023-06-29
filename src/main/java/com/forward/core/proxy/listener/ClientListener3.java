package com.forward.core.proxy.listener;

import com.forward.core.proxy.myClient.MyClient;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

public class ClientListener3 implements ChannelFutureListener {

    private Logger logger = LoggerFactory.getLogger(ClientListener3.class);

    private MyClient myClient;

    private SocketAddress socketAddress;

    public ClientListener3() {
    }

    public ClientListener3(MyClient myClient, SocketAddress socketAddress) {
        this.myClient = myClient;
        this.socketAddress = socketAddress;
    }

    /**
     * ChannelFuture提供操作完成时一种异步通知的方式。
     * 一般在Socket编程中，等待响应结果都是同步阻塞的，而Netty则不会造成阻塞，因为ChannelFuture是采取类似观察者模式的形式进行获取结果
     * @param channelFuture
     * @throws Exception
     */
    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        if (!channelFuture.isSuccess()){
            final EventLoop eventLoop = channelFuture.channel().eventLoop();
            eventLoop.schedule(new Runnable() {
                @Override
                public void run() {
                    logger.info("{}--客户端重连中...",socketAddress);
                    myClient.connect3();
                }
            },3, TimeUnit.SECONDS);
        }else{
            logger.info("监听器监听到：MyClient已经和服务端:{}链接完成",socketAddress);
        }
    }
}
