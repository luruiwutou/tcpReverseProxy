package com.forward.core.proxy.handler;


import com.forward.core.proxy.myClient.MyClient;
import com.forward.core.proxy.util.SimpleClassUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.SocketAddress;

@Component
public class SendCupyHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(SendCupyHandler.class);

//    @Autowired
    private MyClient myClient;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (myClient == null) {
            MyClient myClient = SimpleClassUtil.getMyClientInstance();
            if (myClient != null) {
                Channel channel = myClient.getChannel();
                if (channel.isWritable() && channel.isOpen()){
                    channel.writeAndFlush((ByteBuf)msg);
                }else {
                 logger.info("通道不可读或未打开");
                }
            }
        }
    }
}
