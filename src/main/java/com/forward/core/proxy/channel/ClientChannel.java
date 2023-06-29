package com.forward.core.proxy.channel;

import com.forward.core.proxy.handler.MessageHandler;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

public class ClientChannel<T extends Channel, B extends AbstractBootstrap> extends ChannelInitializer<T> {

    @Override
    protected void initChannel(T t) throws Exception {
        t.pipeline().addLast(new MessageHandler());
    }
}
