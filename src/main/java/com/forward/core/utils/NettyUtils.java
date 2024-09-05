package com.forward.core.utils;

import io.netty.channel.Channel;

public class NettyUtils {
    public static String getRemoteAddress(Channel channel) {
        return channel.remoteAddress().toString().split("/")[1];
    }
}
