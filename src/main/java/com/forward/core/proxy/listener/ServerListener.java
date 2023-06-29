package com.forward.core.proxy.listener;

import com.forward.core.proxy.util.SimpleClassUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class ServerListener implements ChannelFutureListener {

    private static Logger logger = LoggerFactory.getLogger(ServerListener.class);

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
                    SimpleClassUtil.getMyClientInstance().connect();
                }
            },2, TimeUnit.SECONDS);
        }else{
            logger.info("服务端链接完成");
        }

    }
}
