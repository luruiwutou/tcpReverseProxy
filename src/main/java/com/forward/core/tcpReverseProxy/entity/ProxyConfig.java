package com.forward.core.tcpReverseProxy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

@Data
@TableName("proxy_config")
public class ProxyConfig {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String confKey  ;
    private String confVal;
    private String confDesc;

    public ProxyConfig() {
    }

    public ProxyConfig(String confKey, String confVal, String confDesc) {
        this.confKey = confKey;
        this.confVal = confVal;
        this.confDesc = confDesc;
    }

    public ProxyConfig(String confKey, String confVal) {
        this.confKey = confKey;
        this.confVal = confVal;
    }

}
