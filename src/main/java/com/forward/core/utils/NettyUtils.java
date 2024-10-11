package com.forward.core.utils;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;

public class NettyUtils {
    public static String getRemoteAddress(Channel channel) {
        return channel.remoteAddress().toString().split("/")[1];
    }
    public static String getLocalAddress(Channel channel) {
        return channel.localAddress().toString().split("/")[1];
    }
    public static String getAddress(InetSocketAddress inetSocketAddress) {
        return inetSocketAddress.toString().split("/")[1];
    }
}
