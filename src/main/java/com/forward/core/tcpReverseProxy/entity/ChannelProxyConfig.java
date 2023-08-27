package com.forward.core.tcpReverseProxy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("proxy_config")
public class ChannelProxyConfig {
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 渠道
     */
    private String channel;
    private String confKey;
    private String confVal;
    private String confDesc;

    public ChannelProxyConfig() {
    }

    public ChannelProxyConfig(String confKey, String confVal, String confDesc) {
        this.confKey = confKey;
        this.confVal = confVal;
        this.confDesc = confDesc;
    }

    public ChannelProxyConfig(String channel, String confKey, String confVal, String confDesc) {
        this.channel = channel;
        this.confKey = confKey;
        this.confVal = confVal;
        this.confDesc = confDesc;
    }

    public ChannelProxyConfig(String confKey, String confVal) {
        this.confKey = confKey;
        this.confVal = confVal;
    }

}
