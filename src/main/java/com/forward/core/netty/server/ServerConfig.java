package com.forward.core.netty.server;

import io.netty.util.internal.StringUtil;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Setter
public class ServerConfig {

    private String addresses;

    private Integer[] ports;

//
//    @Bean
//    public NettyForwardServer forwardServer() {
//        NettyForwardServer forwardServer1 = new NettyForwardServer(getInetAddresses(addresses));
//        try {
//            for (int i = 0; i < ports.length; i++) {
//                forwardServer1.start(ports[i]);
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("netty socket service startup exception !!", e);
//        }
//        return forwardServer1;
//    }

    public SocketAddress[] getInetAddresses(String addresses) {
//        SocketAddress[] addressArr = new InetSocketAddress[]{};
        List<SocketAddress> addressArr = new ArrayList(4);
        if (StringUtil.isNullOrEmpty(addresses)) {
            throw new RuntimeException("address列表为空");
        }
        String[] splits = addresses.split(",");
        for (int i = 0; i < splits.length; i++) {
            String[] split = splits[i].split(":");
            if (split.length == 0) {
                throw new RuntimeException("[" + splits[i] + "]不符合IP:PORT格式");
            }
            addressArr.add(new InetSocketAddress(split[0], Integer.parseInt(split[1])));
        }
        return addressArr.toArray(new SocketAddress[addressArr.size()]);
    }
}
