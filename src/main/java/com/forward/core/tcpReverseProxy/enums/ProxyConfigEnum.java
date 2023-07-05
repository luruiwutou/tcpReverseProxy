package com.forward.core.tcpReverseProxy.enums;

import com.forward.core.tcpReverseProxy.entity.ProxyConfig;
import com.forward.core.tcpReverseProxy.mapper.ProxyConfigMapper;

import java.util.Optional;

public enum ProxyConfigEnum {
    DEFAULT_ENV("DEFAULT_ENV", "sit","默认环境"),
    RUNTIME_ENV("RUNTIME_ENV", null,"运行时环境"),
    ;
    private String key;
    private String defVal;
    private String desc;

    ProxyConfigEnum(String key,String defVal, String desc) {
        this.key = key;
        this.defVal = defVal;
        this.desc = desc;
    }

    public String getKeyVal(ProxyConfigMapper proxyConfigMapper) {
        Optional<ProxyConfig> byConfKey = proxyConfigMapper.findByConfKey(this.getKey());
        if (byConfKey.isPresent()) {
            return byConfKey.get().getConfVal();
        }else{
            ProxyConfig proxyConfig = new ProxyConfig(this.key, this.defVal, this.desc);
            proxyConfigMapper.insert(proxyConfig);
            return proxyConfig.getConfVal();
        }
    }
    public String update(String val,ProxyConfigMapper proxyConfigMapper) {
        Optional<ProxyConfig> byConfKey = proxyConfigMapper.findByConfKey(this.getKey());
        if (byConfKey.isPresent()) {
            byConfKey.get().setConfVal(val);
            proxyConfigMapper.updateById(byConfKey.get());
            return byConfKey.get().getConfVal();
        }else{
            ProxyConfig proxyConfig = new ProxyConfig(this.key, val, this.desc);
            proxyConfigMapper.insert(proxyConfig);
            return proxyConfig.getConfVal();
        }
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDefVal() {
        return defVal;
    }

    public void setDefVal(String defVal) {
        this.defVal = defVal;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
