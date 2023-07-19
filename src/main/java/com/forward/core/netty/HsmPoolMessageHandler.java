package com.forward.core.netty;

import com.forward.core.tcpReverseProxy.utils.HsmUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ZhaoLing
 * @description Hsm消息处理类
 * <p>
 * <p>
 * 因为还需要通过该类来获取放回的message用于业务处理
 * @date
 */
@Slf4j
@ChannelHandler.Sharable
public class HsmPoolMessageHandler extends SimpleChannelInboundHandler<byte[]> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, byte[] bytes) throws Exception {
        log.info("HsmPoolMessageHandler channelRead0 begin:");
        HsmRequestContext requestContext = channelHandlerContext.channel().attr(HsmSendClient.CURRENT_REQ_BOUND_WITH_THE_CHANNEL).get();
        log.info("HsmPoolMessageHandler req, params:{},resp: {}",
                HsmUtils.byteToHex(requestContext.getMessage()),
                HsmUtils.byteToHex(bytes));
        Promise<byte[]> promise = requestContext.getDefaultPromise();
        promise.setSuccess(bytes);
        log.info("HsmPoolMessageHandler channelRead0 end:");
    }
}
