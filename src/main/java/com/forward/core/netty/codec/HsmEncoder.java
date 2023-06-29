package com.forward.core.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class HsmEncoder extends MessageToByteEncoder<byte[]> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, byte[] bytes, ByteBuf byteBuf) throws Exception {
        log.info("HsmDecoder begin");
        byteBuf.writeBytes(bytes);
        log.info("HsmDecoder end");
    }
}
