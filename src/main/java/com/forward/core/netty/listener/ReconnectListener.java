package com.forward.core.netty.listener;

import com.forward.core.constant.Constants;
import com.forward.core.netty.pool.NettyClientPool;
import com.forward.core.utils.NettyUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public class ReconnectListener implements GenericFutureListener {

    private boolean reconnect = true;
    private Supplier<NettyClientPool> nettyClientPoolSupplier;

    public ReconnectListener(Supplier<NettyClientPool> nettyClientPoolSupplier) {
        this.nettyClientPoolSupplier = nettyClientPoolSupplier;
    }

    public NettyClientPool getNettyClientPool() {
        return nettyClientPoolSupplier.get();
    }

    @Override
    public void operationComplete(Future future) throws Exception {
        if (!reconnect) {
            log.info("与服务端的连接断开，proxy客户端无需重连");
            return;
        }
        if (future.isSuccess()) {
            final Channel closeChannel = ((ChannelFuture) future).channel();
            String finalAddress = NettyUtils.getRemoteAddress(closeChannel);
            if (Constants.LOCAL_PORT_RULE_SINGLE.equals(getNettyClientPool().getClientConfig().getRemoteServerAndLocalPort().get(finalAddress))) {
                log.info("特定端口：{}，无需重连", Constants.LOCAL_PORT_RULE_SINGLE);
                return;
            }
            if (getNettyClientPool().getPoolHandlers().get(closeChannel.remoteAddress()).getActiveConnections() <= 0) {
                if (getNettyClientPool().getWorkerGroup().isShutdown()){
                    log.info("client pool is shutdown, not reconnect");
                    return;
                }
                getNettyClientPool().getWorkerGroup().submit(() -> {
                    Channel channel = getNettyClientPool().getChannel(finalAddress);
                    getNettyClientPool().release(channel);
                });
            }
        }
    }


    public boolean isReconnect() {
        return reconnect;
    }

    public void setReconnect(boolean reconnect) {
        this.reconnect = reconnect;
    }
}
