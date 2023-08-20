package com.forward.core.tcpReverseProxy.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class HostUtils {
    public static String getLocalServerAddress()  {
        InetAddress localHost = null;
        String ipAddress =null;
        try {
            localHost = InetAddress.getLocalHost();
            ipAddress = localHost.getHostAddress();
        } catch (UnknownHostException e) {
            log.info("can't get local server address'");
            ipAddress ="localhost";
        }
        return ipAddress;
    }
}
