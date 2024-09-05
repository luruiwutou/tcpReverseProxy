package com.forward.core.netty.handler;

import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author
 * @description 消息解析错误处理类
 * @date
 */
@ChannelHandler.Sharable
@Slf4j
public class ClientExceptionHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            log.error("远程服务端中断了一个已有的连接，稍后会自动重连！");
        } else {
            log.error("消息处理异常！异常：" + cause.getMessage(), cause);
            log.info("channel 处理异常，关闭连接");
            ChannelFuture future = ctx.channel().close();
            future.addListener((ChannelFutureListener) f -> {
                if (f.isSuccess())
                    log.info("客户端出现异常主动关闭,Exception:", cause);
            });
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.info("服务端关闭连接");
    }
}
