package com.forward.core.tcpReverseProxy.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

@Slf4j
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {
    //mysql "/* ping */ SELECT 1"
    //redis "PING\n\n"
    private final String pingPayload;

    public HeartbeatHandler(String pingPayload) {
        this.pingPayload = pingPayload;
    }

    private ByteBuf getWriteByteBuf() {
        return Unpooled.unreleasableBuffer(Unpooled.copiedBuffer(getPingPayload().replace("\\n", "\n"), CharsetUtil.UTF_8));
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                if (StringUtils.isBlank(getPingPayload())) return;
                // 发送心跳包
                ctx.writeAndFlush(getWriteByteBuf().duplicate());
            }
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

    public String getPingPayload() {
        return pingPayload;
    }

//    public static void main(String[] args) {
//        String x = "PING\\n".replace("\\n", "\n");
//        System.out.println(x);
//    }
}
