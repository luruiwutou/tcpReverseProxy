package com.forward.core.tcpReverseProxy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

@Data
@TableName("tcp_proxy_mapping")
public class TcpProxyMapping {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String localHost = "localhost";
    private String localPort;
    private String localClientPort;
    private String targetHost;
    private String targetPort;
    private String env ="SIT";
    private String channel;
    @TableField(exist = false)
    private List<String[]> targetConnections;


}
