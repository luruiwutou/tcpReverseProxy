package com.forward.core.proxy.config;

import com.forward.core.proxy.myClient.MyClient;
import com.forward.core.proxy.myServer.MyServer;
import com.forward.core.proxy.properties.NettyProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableConfigurationProperties({NettyProperties.class})
public class BeanConfig {
    private Logger logger = LoggerFactory.getLogger(BeanConfig.class);

    @Autowired
    private NettyProperties nettyProperties;
//    @Bean
    public MyClient myClient() {
        MyClient myClient = null;
        logger.info("---myClient初始化---");
        try {
            List<InetSocketAddress> socketAddressList = new ArrayList<>();
            for (Integer clientPort : nettyProperties.getClientPort()) {
                InetSocketAddress socketAddress = new InetSocketAddress(nettyProperties.getClientHost(), clientPort);
                socketAddressList.add(socketAddress);
            }

            myClient = new MyClient(socketAddressList);
            myClient.init();
            myClient.connect();
//            myClient.connect2();
//            myClient.connect3();
        } catch (Exception e) {
            logger.error("---cupyClient Connecting Error---" + e.getMessage());
        }
        return myClient;
    }

//    @Bean
    public MyServer myServer(){
        MyServer myServer = null;
        logger.info("====myServer初始化====");
        List<InetSocketAddress> socketServerAddressList = new ArrayList<>();
        for (Integer serverPort : nettyProperties.getServerPorts()) {
            InetSocketAddress socketAddress = new InetSocketAddress(nettyProperties.getServerHost(), serverPort);
            socketServerAddressList.add(socketAddress);
        }
        myServer = new MyServer(socketServerAddressList);
        myServer.init();
        myServer.connect();
        myServer.connect2();
        myServer.connect3();
        return myServer;
    }
}
