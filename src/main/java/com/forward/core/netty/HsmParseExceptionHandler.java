package com.forward.core.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author ZhaoLing
 * @description 消息解析错误处理类
 * @date
 */
@ChannelHandler.Sharable
@Slf4j
public class HsmParseExceptionHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException && "Connection reset by peer".equals(cause.getMessage())) {
            log.error("远程客户端中断了一个已有的连接，稍后会自动重连！");
        } else {
            log.error("消息处理异常！异常：" + cause.getMessage(), cause);
        }
    }
}
