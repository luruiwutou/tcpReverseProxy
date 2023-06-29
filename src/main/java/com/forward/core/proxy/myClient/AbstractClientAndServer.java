package com.forward.core.proxy.myClient;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractClientAndServer {

    private NioEventLoopGroup eventExecutors = null;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private Bootstrap bootstrap = null;

    private ServerBootstrap serverBootstrap = null;

    public static AtomicReference<Channel> atomicReference = new AtomicReference<>();//此通道进行发送消息

    public static AtomicReference<Channel> serverAtomicReference = new AtomicReference<>();//此通道进行发送消息

    public static List<Channel> connectChannelList = new CopyOnWriteArrayList<>();

    public ServerBootstrap getServerBootstrap() {
        return serverBootstrap;
    }

    public void setServerBootstrap(ServerBootstrap serverBootstrap) {
        this.serverBootstrap = serverBootstrap;
    }

    public abstract void init();

    public Channel getChannel(){
       return atomicReference.get();
    }

    protected void setChannel(Channel channel){
        atomicReference.set(channel);
    }

    public Channel getServerChannel(){
        return serverAtomicReference.get();
    }

    protected void setServerChannel(Channel channel){
        serverAtomicReference.set(channel);
    }

    public NioEventLoopGroup getEventExecutors() {
        return eventExecutors;
    }

    public Bootstrap getBootstrap(){
        return this.bootstrap;
    }

    public void setEventExecutors(NioEventLoopGroup eventExecutors) {
        this.eventExecutors = eventExecutors;
    }

    public void setBootstrap(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public EventLoopGroup getBossGroup() {
        return bossGroup;
    }

    public void setBossGroup(EventLoopGroup bossGroup) {
        this.bossGroup = bossGroup;
    }

    public EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    public void setWorkerGroup(EventLoopGroup workerGroup) {
        this.workerGroup = workerGroup;
    }

    public abstract void connect();

    public abstract void connect2();
    public abstract void connect3();
}
