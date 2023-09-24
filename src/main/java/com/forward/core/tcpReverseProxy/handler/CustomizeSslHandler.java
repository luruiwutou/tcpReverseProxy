package com.forward.core.tcpReverseProxy.handler;

import com.forward.core.constant.Constants;
import com.forward.core.sftp.utils.StringUtil;
import com.forward.core.tcpReverseProxy.utils.SnowFlake;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.SslHandler;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.util.List;
import java.util.concurrent.Executor;

@Slf4j
public class CustomizeSslHandler extends SslHandler {

    public CustomizeSslHandler(SSLEngine engine) {
        super(engine);
    }

    public CustomizeSslHandler(SSLEngine engine, boolean startTls) {
        super(engine, startTls);
    }

    public CustomizeSslHandler(SSLEngine engine, Executor delegatedTaskExecutor) {
        super(engine, delegatedTaskExecutor);
    }

    public CustomizeSslHandler(SSLEngine engine, boolean startTls, Executor delegatedTaskExecutor) {
        super(engine, startTls, delegatedTaskExecutor);
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws SSLException {
        if (StringUtil.isNotEmpty(ctx.channel().attr(Constants.TRACE_ID_KEY).get())) {
            MDC.put(Constants.TRACE_ID, ctx.channel().attr(Constants.TRACE_ID_KEY).get());
        }
        if (StringUtil.isEmpty(MDC.get(Constants.TRACE_ID))) {
            String traceId = SnowFlake.getTraceId();
            ctx.channel().attr(Constants.TRACE_ID_KEY).set(SnowFlake.getTraceId());
            MDC.put(Constants.TRACE_ID, traceId);
        }
        log.info("ssl decode data :{}", ByteBufUtil.hexDump(in));
        super.decode(ctx, in, out);
        MDC.remove(Constants.TRACE_ID);
    }
}
