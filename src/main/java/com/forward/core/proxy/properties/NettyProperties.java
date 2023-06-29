package com.forward.core.proxy.properties;

import io.netty.util.internal.StringUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "forward.netty.server")
public class NettyProperties {

    private String addresses;

    private String serverHost;

    private List<Integer> serverPorts;

    private String clientHost;

    private List<Integer> clientPort;

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
