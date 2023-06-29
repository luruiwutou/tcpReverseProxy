package com.forward.core.proxy.channel;

import com.forward.core.proxy.handler.MyServerCupyHandler;
import com.forward.core.proxy.handler.SendCupyHandler;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;

@ChannelHandler.Sharable
public class ServerChannelInitializer<T extends Channel, B extends AbstractBootstrap> extends ChannelInitializer<T> {

    public ServerChannelInitializer() {
    }

    @Override
    protected void initChannel(T t) throws Exception {
        t.pipeline().addLast(new MyServerCupyHandler());
        t.pipeline().addLast(new SendCupyHandler());

    }
}
