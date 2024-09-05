package com.forward.core.netty.handler;

import cn.hutool.core.util.HexUtil;
import com.forward.core.netty.DataBusConstant;
import com.forward.core.utils.NettyUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class NettyClientIdleEventHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof String) {
            String message = (String) msg;
            log.info("服务端：{}，消息:{}", NettyUtils.getRemoteAddress(ctx.channel()), message);
        } else if (msg instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf) msg;
            log.info("服务端：{}，消息:{}", NettyUtils.getRemoteAddress(ctx.channel()), ByteBufUtil.hexDump(byteBuf));
            if (byteBuf.readableBytes() == 4 && HexUtil.encodeHexStr(DataBusConstant.HEART_BEAT).equals(ByteBufUtil.hexDump(byteBuf))) {
                log.info("msg from {} heart beat msg ：0000", ctx.channel().remoteAddress());
                ReferenceCountUtil.release(msg);
                return;
            }
        } else {
            if (ctx.pipeline().last() == this) {
                // 如果当前处理器是最后一个处理器，则说明它是消息处理链的最后一个处理器
                // 可以在这里对消息进行最终处理
                ReferenceCountUtil.release(msg);
                return;
            }
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        boolean active = ctx.channel().isActive();
        log.debug("[此时通道状态] {}", active);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {

            IdleStateEvent e = (IdleStateEvent) evt;

            if (e.state() == IdleState.READER_IDLE || e.state() == IdleState.ALL_IDLE) {

                ctx.flush();

            }

        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}