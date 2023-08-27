package com.forward.core.tcpReverseProxy.enums;

import com.forward.core.constant.Constants;
import com.forward.core.sftp.utils.StringUtil;
import com.forward.core.tcpReverseProxy.entity.ChannelProxyConfig;
import com.forward.core.tcpReverseProxy.mapper.ProxyConfigMapper;

import java.util.List;
import java.util.Optional;

/**
 * 渠道代理配置枚举
 */
public enum ChannelProxyConfigEnum {
    DEFAULT_ENV("DEFAULT_ENV", "SIT", "默认环境"),
    RUNTIME_ENV("RUNTIME_ENV", "SIT", "运行时环境"),
    ;

    private String key;
    private String defVal;
    private String desc;

    ChannelProxyConfigEnum(String key, String defVal, String desc) {
        this.key = key;
        this.defVal = defVal;
        this.desc = desc;
    }

    public String getKeyVal(String channel, ProxyConfigMapper proxyConfigMapper) {
        if (Constants.ASTERISK.equals(channel)) {
            return this.getDefVal();
        }
        Optional<ChannelProxyConfig> byConfKey = proxyConfigMapper.findByConfKey(channel, this.getKey());
        if (byConfKey.isPresent()) {
            return byConfKey.get().getConfVal();
        } else {
            ChannelProxyConfig channelProxyConfig = new ChannelProxyConfig(channel, this.key, this.defVal, this.desc);
            proxyConfigMapper.insert(channelProxyConfig);
            return channelProxyConfig.getConfVal();
        }
    }

    public String update(String channel, String val, ProxyConfigMapper proxyConfigMapper) {
        if (Constants.ASTERISK.equals(channel)) {
            channel = null;
        }
        Integer updCount = proxyConfigMapper.updChannelEnvConf(channel, this.key, val);
        if (null != channel && updCount < 1) {
            ChannelProxyConfig channelProxyConfig = new ChannelProxyConfig(channel, this.key, val, this.desc);
            proxyConfigMapper.insert(channelProxyConfig);
        }
        return val;
    }

    public List<ChannelProxyConfig> getChannelEnv(ProxyConfigMapper proxyConfigMapper) {
        return proxyConfigMapper.distinctChannelConfig(this.getKey());
    }

    public String getKey() {
        return key;
    }

    public String getDefVal() {
        return defVal;
    }


    public String getDesc() {
        return desc;
    }


}
