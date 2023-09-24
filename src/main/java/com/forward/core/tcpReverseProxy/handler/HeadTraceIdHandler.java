package com.forward.core.tcpReverseProxy.handler;

import com.forward.core.constant.Constants;
import com.forward.core.sftp.utils.StringUtil;
import com.forward.core.tcpReverseProxy.utils.SnowFlake;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public class HeadTraceIdHandler extends ChannelInboundHandlerAdapter {
    // 定义一个Channel属性来存储traceId


    // 将traceId设置到Channel属性中
    public static void setTraceId(Channel channel) {
        channel.attr(Constants.TRACE_ID_KEY).set(SnowFlake.getTraceId());
    }

    public static void setTraceId(Channel channel, String traceId) {
        channel.attr(Constants.TRACE_ID_KEY).set(traceId);
    }

    // 从Channel属性中获取traceId
    public static String getTraceId(Channel channel) {
        return channel.attr(Constants.TRACE_ID_KEY).get();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (StringUtil.isEmpty(ctx.channel().attr(Constants.TRACE_ID_KEY).get())){
            if (StringUtil.isEmpty(MDC.get(Constants.TRACE_ID))) {
                setTraceId(ctx.channel());
            } else {
                setTraceId(ctx.channel(), MDC.get(Constants.TRACE_ID));
            }
        }
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (StringUtil.isEmpty(MDC.get(Constants.TRACE_ID))) {
            setTraceId(ctx.channel());
        }else{
            setTraceId(ctx.channel(), MDC.get(Constants.TRACE_ID));
        }
        super.channelRead(ctx, msg);
    }
}