package com.forward.core.tcpReverseProxy.handler;

import com.forward.core.constant.Constants;
import com.forward.core.sftp.utils.StringUtil;
import com.forward.core.tcpReverseProxy.utils.SnowFlake;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

@Slf4j
public class CustomizeLengthFieldBasedFrameDecoder extends LengthFieldBasedFrameDecoder {

    public CustomizeLengthFieldBasedFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
    }

    public CustomizeLengthFieldBasedFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected long getUnadjustedFrameLength(ByteBuf buf, int offset, int length, ByteOrder order) {
        buf = buf.order(order);
        long frameLength;
        switch (length) {
            case 1:
                frameLength = buf.getUnsignedByte(offset);
                break;
            case 2:
                frameLength = buf.getUnsignedShort(offset);
                break;
            case 3:
                frameLength = buf.getUnsignedMedium(offset);
                break;
            case 4:
            case 8:
            case 9:
                frameLength = getFrameLength(buf, offset, length);
                break;
            default:
                throw new DecoderException(
                        "unsupported lengthFieldLength: " + length + " (expected: 1, 2, 3, 4, 8, 9)");
        }
        return frameLength;
    }

    private long getFrameLength(ByteBuf buf, int offset, int length) {
        byte[] bytes = new byte[length];
        buf.getBytes(offset, bytes);
        Long result = 0L;
        try {
            result = Long.valueOf(new String(bytes));
        } catch (NumberFormatException e) {
            log.info("try IBM charset");
            try {
                result = Long.valueOf(new String(bytes, Charset.forName("IBM1047")));
                log.info("IBM length: {}", result);
            } catch (NumberFormatException ex) {
                log.info("getFrameLength error:", e);
                log.info("getFrameLength error:", ex);
            }
        }
        return result;
    }

    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        if (StringUtil.isEmpty(MDC.get(Constants.TRACE_ID))){
            String traceId = SnowFlake.getTraceId();
            MDC.put(Constants.TRACE_ID, traceId);
        }
        Object decode = super.decode(ctx, in);
        return decode;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("decoder Exception :", cause);
        super.exceptionCaught(ctx, cause);
        MDC.remove(Constants.TRACE_ID);
    }
}
