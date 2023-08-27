package com.forward.core.tcpReverseProxy.schedule;

import com.forward.core.tcpReverseProxy.controller.TcpProxyController;
import com.forward.core.tcpReverseProxy.entity.ChannelProxyConfig;
import com.forward.core.tcpReverseProxy.enums.ChannelProxyConfigEnum;
import com.forward.core.tcpReverseProxy.mapper.ProxyConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TcpProxyScheduler {
    Logger logger = LoggerFactory.getLogger(TcpProxyScheduler.class);

    @Autowired
    private TcpProxyController controller;

    @Autowired
    ProxyConfigMapper proxyConfigMapper;

    @Scheduled(cron = "00 */3 * * * ?")
    public void reportTask() throws Exception {
        logger.info("scheduled reload emv");

        List<ChannelProxyConfig> channelEnv = ChannelProxyConfigEnum.RUNTIME_ENV.getChannelEnv(proxyConfigMapper);
        for (ChannelProxyConfig channelProxyConfig : channelEnv) {
            controller.reload(channelProxyConfig.getChannel(), channelProxyConfig.getConfVal());
        }
    }

}
