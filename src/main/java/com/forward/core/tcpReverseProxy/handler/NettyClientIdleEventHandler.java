package com.forward.core.tcpReverseProxy.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class NettyClientIdleEventHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            // 触发了读空闲事件，表示对方没有发送心跳消息
            if (event.state() == IdleState.READER_IDLE) {
                // 关闭连接
                log.info("server not send idle msg ,closed connection");
                ctx.flush();
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}