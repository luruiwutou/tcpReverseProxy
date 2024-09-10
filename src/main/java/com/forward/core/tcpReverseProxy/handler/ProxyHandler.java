package com.forward.core.tcpReverseProxy.handler;

import com.forward.core.constant.Constants;
import com.forward.core.netty.config.NettyClientPoolProperties;
import com.forward.core.netty.handler.ClientReturnToServerHandler;
import com.forward.core.netty.listener.ReconnectListener;
import com.forward.core.netty.pool.CupySendClient;
import com.forward.core.sftp.utils.StringUtil;
import com.forward.core.tcpReverseProxy.utils.SnowFlake;
import com.forward.core.utils.NettyUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFutureListener;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@ChannelHandler.Sharable
public class ProxyHandler extends ChannelInboundHandlerAdapter {
    private String clientPort;
    private ChannelGroup clientChannels;
    private EventLoopGroup workerGroup;
    private EventLoopGroup clientBoosGroup = new NioEventLoopGroup();
    private EventLoopGroup clientWorkGroup = new NioEventLoopGroup();
    private EventExecutorGroup executorGroup;
    //每一个目标服务器开了多少个channel的计数
    private Supplier<List<String>> getNewTarget;
    private ConcurrentLinkedQueue<ProxyHandler> proxyHandlers;
    private Consumer<Channel> customizeHandlerMapCon;
    private CupySendClient client;
    /**
     * 控制重连的监听器
     */
    private ReconnectListener clientReconnectListener;
    private volatile boolean isShutDown = false;

    public ProxyHandler(String clientPort, Map<String, Integer> connectionCounts, EventLoopGroup workerGroup, EventExecutorGroup executorGroup, Supplier<List<String>> getNewTarget, Supplier<Integer> getReconnectTime, Consumer<Channel> putCustomizeTargetChannelHandler, List<String> targetServers) {
        this.clientPort = clientPort;
        this.customizeHandlerMapCon = putCustomizeTargetChannelHandler;
        this.workerGroup = workerGroup;
        this.executorGroup = executorGroup;
        this.clientChannels = new DefaultChannelGroup(this.workerGroup.next());
        this.getNewTarget = getNewTarget;
        this.client = new CupySendClient(putCustomizeTargetChannelHandler(), new NettyClientPoolProperties(targetServers, clientPort), clientBoosGroup, clientWorkGroup);
        this.clientReconnectListener = new ReconnectListener(() -> client.getNettyClientPool());
    }

    /**
     * 注入代理channelGroup
     *
     * @return
     */
    private Consumer<Channel> putCustomizeTargetChannelHandler() {
        return (channel) -> {
            customizeHandlerMapCon.accept(channel);
            channel.pipeline().addLast(new ClientReturnToServerHandler(() -> clientChannels, executorGroup, clientReconnectListener));
        };
    }

    public void setProxyHandlers(ConcurrentLinkedQueue<ProxyHandler> proxyHandlers) {
        this.proxyHandlers = proxyHandlers;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        if (StringUtil.isNotEmpty(ctx.channel().attr(Constants.TRACE_ID_KEY).get())) {
            MDC.put(Constants.TRACE_ID, ctx.channel().attr(Constants.TRACE_ID_KEY).get());
        }
        if (StringUtil.isEmpty(MDC.get(Constants.TRACE_ID))) {
            String traceId = SnowFlake.getTraceId();
            MDC.put(Constants.TRACE_ID, traceId);
        }

        clientChannels.add(ctx.channel());
        client.initChannel();
        MDC.remove(Constants.TRACE_ID);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (StringUtil.isNotEmpty(ctx.channel().attr(Constants.TRACE_ID_KEY).get())) {
            MDC.put(Constants.TRACE_ID, ctx.channel().attr(Constants.TRACE_ID_KEY).get());
        }
        if (StringUtil.isEmpty(MDC.get(Constants.TRACE_ID))) {
            String traceId = SnowFlake.getTraceId();
            MDC.put(Constants.TRACE_ID, traceId);
        }
        log.info("server {} received from {}", NettyUtils.getLocalAddress(ctx.channel()), NettyUtils.getRemoteAddress(ctx.channel()));
        getReadConsumer(client).accept(msg);
        MDC.remove(Constants.TRACE_ID);
        ctx.channel().attr(Constants.TRACE_ID_KEY).set(null);
    }

    private Consumer getReadConsumer(CupySendClient client) {
        return msg -> {
            String traceId = MDC.get(Constants.TRACE_ID);
            executorGroup.submit(() -> {
                MDC.put(Constants.TRACE_ID, traceId);
                try {
                    if (msg instanceof ByteBuf) {
                        log.info("Hex msg:{}", ByteBufUtil.hexDump((ByteBuf) msg));
                    }
                    client.send(msg);
                } catch (Exception e) {
                    log.error("send error", e);
                    throw e;
                } finally {
                    MDC.remove(Constants.TRACE_ID);
                }
            });
        };
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("{} channelInactive ", ctx.channel());
        whenExceptionOrClose(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("exceptionCaught", cause);
        cause.printStackTrace();
        whenExceptionOrClose(ctx);
    }

    private void whenExceptionOrClose(ChannelHandlerContext ctx) {
        if (StringUtil.isNotEmpty(ctx.channel().attr(Constants.TRACE_ID_KEY).get())) {
            MDC.put(Constants.TRACE_ID, ctx.channel().attr(Constants.TRACE_ID_KEY).get());
        }
        if (StringUtil.isEmpty(MDC.get(Constants.TRACE_ID))) {
            String traceId = SnowFlake.getTraceId();
            MDC.put(Constants.TRACE_ID, traceId);
        }
        if (clientChannels.contains(this)) {
            clientChannels.remove(this);
        }
        ctx.close();
        if (!clientChannels.isEmpty()) {
            return;
        }
        if (proxyHandlers.contains(this)) {
            proxyHandlers.remove(this);
        }
        shutdown();
        MDC.get(Constants.TRACE_ID);
    }


    public void shutdown() {
        if (isShutDown) return;
        log.info("Shutting down,{}", isShutDown);
        clientReconnectListener.setReconnect(false);
        clientChannels.close().addListener((ChannelGroupFutureListener) future -> {
            clientWorkGroup.shutdownGracefully();
            clientBoosGroup.shutdownGracefully();
            isShutDown = true;
        });
    }

    public String getClientPort() {
        return clientPort;
    }

    public void initiativeReconnected() {
        //利用自动重连机制，主动关闭channel，让其重连
        if (null != client) {
            client.destroyNettyClientPool();
        }
        clientWorkGroup = new NioEventLoopGroup();
        clientBoosGroup = new NioEventLoopGroup();
        client = new CupySendClient(putCustomizeTargetChannelHandler(), new NettyClientPoolProperties(getNewTarget.get(), clientPort), clientBoosGroup, clientWorkGroup);
    }
}