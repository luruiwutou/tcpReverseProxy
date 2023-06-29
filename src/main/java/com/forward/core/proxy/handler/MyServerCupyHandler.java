package com.forward.core.proxy.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 自定义的Handler需要继承Netty规定好的HandlerAdapter
 * 才能被Netty框架所关联，有点类似SpringMVC的适配器模式
 **/
@Component
public class MyServerCupyHandler extends ByteToMessageDecoder {

    private static Logger logger = LoggerFactory.getLogger(MyServerCupyHandler.class);

    @Override
    protected void handlerRemoved0(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved0(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("服务器MyServer已经被SocketTool的客户端连接:{}",ctx.channel().remoteAddress());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //发送消息给客户端
        //ctx.writeAndFlush(Unpooled.copiedBuffer("服务端接受消息后返回客户端应答消息"+String.valueOf(i), CharsetUtil.UTF_8));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        if (!byteBuf.isReadable()) {
            return;
        }
        byteBuf.retain();
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        this.logger.info("TCP ACCEPT MESSAGE: {}", new String(bytes, "utf-8"));
        logger.info("接收到的数据为：{}",byteBuf);
        ByteBuf buffer = ctx.alloc().buffer(bytes.length);
        buffer.writeBytes(bytes);
        out.add(buffer);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx,cause);
        Channel channel = ctx.channel();
        if (channel.isActive())ctx.close();
    }
}