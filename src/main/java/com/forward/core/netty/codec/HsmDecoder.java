package com.forward.core.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author ZhaoLing
 * @description HSM返回信息解码
 * @date
 */

@Slf4j
public class HsmDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        log.info("HsmDecoder begin");
        if (!byteBuf.isReadable()) {
            log.info("HsmDecoder fail ===> byteBuf is unReadable");
            return;
        }
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        list.add(bytes);
        log.info("HsmDecoder End:");
    }
}
