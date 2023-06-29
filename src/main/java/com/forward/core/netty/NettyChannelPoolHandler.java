package com.forward.core.netty;

import com.forward.core.netty.codec.HsmDecoder;
import com.forward.core.netty.codec.HsmEncoder;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyChannelPoolHandler implements ChannelPoolHandler {

    private final HsmClientProperties hsmClientProperties;

    public NettyChannelPoolHandler(HsmClientProperties hsmClientProperties) {
        this.hsmClientProperties = hsmClientProperties;
    }

    @Override
    public void channelReleased(Channel channel) throws Exception {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER);
        log.info("|-->Released HSM Channel. Channel ID: " + channel.id());
    }

    @Override
    public void channelAcquired(Channel channel) throws Exception {
        log.info("|-->Acquired HSM Channel. Channel ID: " + channel.id());
    }

    @Override
    public void channelCreated(Channel channel) throws Exception {
        log.info("|-->Create HSM Channel. Channel ID: " + channel.id());
        SocketChannel ch = (SocketChannel) channel;
        ch.config().setKeepAlive(true);
        ch.config().setTcpNoDelay(true);
        ChannelPipeline pipeline = ch.pipeline();
        //管道添加发送和接受的解码器
        pipeline.addLast("lengthFieldFameDecoder", new LengthFieldBasedFrameDecoder(
                        hsmClientProperties.getMaxFrameLength(),
                        hsmClientProperties.getLengthFieldOffset(),
                        hsmClientProperties.getFrameLengthFieldLength(),
                        hsmClientProperties.getLengthAdjustment(),
                        hsmClientProperties.getInitialBytesToStrip()));

        pipeline.addLast("hsmDecoder", createDecoder());
        pipeline.addLast("hsmEncoder", createEncoder());
        pipeline.addLast("idleState", new IdleStateHandler(hsmClientProperties.getReadIdleTimeout(),
                hsmClientProperties.getWriteIdleTimeout(), 0));
        pipeline.addLast("parseExceptionHandler", createParseExceptionHandler());
        pipeline.addLast("createHandler", createHandler());
    }

    private HsmPoolMessageHandler createHandler() {
        return new HsmPoolMessageHandler();
    }
    protected ChannelHandler createParseExceptionHandler() {
        return  new HsmParseExceptionHandler();
    }
    protected HsmEncoder createEncoder() {
        return new HsmEncoder();
    }
    protected HsmDecoder createDecoder() {
        return new HsmDecoder();
    }

}
