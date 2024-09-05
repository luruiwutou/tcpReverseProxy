package com.forward.core.netty.handler;

import com.forward.core.netty.handler.ClientExceptionHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@ChannelHandler.Sharable
public class NettyChannelPoolHandler implements ChannelPoolHandler {

    private final Map<String, ChannelHandler> customChannelHandlers;

    private final EventLoopGroup workerGroup;
    private AtomicInteger activeConnections = new AtomicInteger(0);

    public NettyChannelPoolHandler(Map<String, ChannelHandler> customChannelHandlers, EventLoopGroup workerGroup) {
        this.customChannelHandlers = customChannelHandlers;
        this.workerGroup = workerGroup;
    }

    @Override
    public void channelReleased(Channel ch) {
        ch.writeAndFlush(Unpooled.EMPTY_BUFFER);
//        log.info("|-->回收Channel. Channel ID: " + ch.id());
    }

    @Override
    public void channelAcquired(Channel ch) {
//        log.info("|-->获取Channel. Channel ID: " + ch.id());
    }

    @Override
    public void channelCreated(Channel ch) {
        activeConnections.incrementAndGet();
        log.info("创建Channle开始 " + ch.id());
        //   log.info("|-->创建Channel. Channel ID: {} -->创建Channel. Channel REAL HASH: {}", ch.id(), System.identityHashCode(ch));
        SocketChannel channel = (SocketChannel) ch;
        channel.config().setKeepAlive(true);
        channel.config().setTcpNoDelay(true);
        if (customChannelHandlers != null) {
            customChannelHandlers
                    .forEach((name, channelHandler) -> channel.pipeline().addLast(workerGroup, name, channelHandler).addLast(new LoggingHandler(LogLevel.DEBUG)));
        }
        channel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG))
                .addLast("exceptionHandler", new ClientExceptionHandler())
                .fireChannelReadComplete();
        //  log.info("|-->Channel ID: {} -->localAddress:{},remoteAddress:{}", ch.id(), channel.localAddress(), channel.remoteAddress());
        ch.closeFuture().addListener(listener -> {
            log.info("销毁Channle {}", ch.id());
            activeConnections.decrementAndGet();
        });
        log.info("创建Channel完成 " + ch.id());
    }

    public int getActiveConnections() {
        return activeConnections.get();
    }

}