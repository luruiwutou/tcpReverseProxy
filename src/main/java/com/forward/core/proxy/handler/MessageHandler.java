package com.forward.core.proxy.handler;

import com.forward.core.proxy.myClient.AbstractClientAndServer;
import com.forward.core.proxy.myClient.MyClient;
import com.forward.core.proxy.util.SimpleClassUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class MessageHandler extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    @Autowired(required = false)
    private MyClient myClient;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("MyClient客户端连接Jetco的服务端 {} 成功",ctx.channel().remoteAddress());
        AbstractClientAndServer.connectChannelList.add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().eventLoop().schedule(new Runnable() {
            @Override
            public void run() {
                logger.info("MyClient客户端的服务器端remote:{},local:{}断开了，正尝试重新连接。。。",ctx.channel().remoteAddress(),ctx.channel().localAddress());
                AbstractClientAndServer.connectChannelList.remove(ctx.channel());
                try {
                    String port = ctx.channel().remoteAddress().toString().split(":")[1];
                    if (port.equals("20115")){
                        SimpleClassUtil.getMyClientInstance().connect();
                    }
//                    else if (port.equals("20112")){
//                        SimpleClassUtil.getMyClientInstance().connect2();
//                    }else if (port.equals("20113")){
//                        SimpleClassUtil.getMyClientInstance().connect3();
//                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },3,TimeUnit.SECONDS);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.info("有异常：{}",cause.getMessage());
    }
}
