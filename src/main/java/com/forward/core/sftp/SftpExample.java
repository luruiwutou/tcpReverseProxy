package com.forward.core.sftp;

import com.jcraft.jsch.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.net.Proxy;

public class SftpExample {

    public static void doConnect() {
        String gatewayHost = "25.6.72.40";
        String gatewayUsername = "root";
        String privateKeyPath = "/root/.ssh/upi_id_rsa";
        int gatewayPort = 22;

        String targetHost = "203.184.81.98";
        String targetUsername = "cybhk_t";
        int targetPort = 9990;

        JSch jsch = new JSch();
        try {
            // 建立到网关服务器的SSH连接
            Session gatewaySession = jsch.getSession(gatewayUsername, gatewayHost, gatewayPort);
            gatewaySession.setConfig("StrictHostKeyChecking", "no");  // 忽略对网关服务器的Host Key验证
            jsch.addIdentity(privateKeyPath);
            gatewaySession.connect();

            // 创建Socks5代理Socket
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("localhost", gatewaySession.getPort()));
            SocketFactory socketFactory = new ProxySocketFactory(proxy);

            // 设置代理
            Session targetSession = jsch.getSession(targetUsername, targetHost, targetPort);
            targetSession.setSocketFactory(socketFactory);
            targetSession.setConfig("StrictHostKeyChecking", "no");  // 忽略对外网SFTP服务器的Host Key验证
            jsch.addIdentity(privateKeyPath);
            targetSession.connect();

            // 创建SFTP通道
            ChannelSftp channelSftp = (ChannelSftp) targetSession.openChannel("sftp");
            channelSftp.connect();

            // 下载文件
            String remoteFilePath = "/path/to/remote/file.txt";
            String localFilePath = "/path/to/local/file.txt";
            channelSftp.get(remoteFilePath, localFilePath);

            // 关闭SFTP通道和会话
            channelSftp.disconnect();
            targetSession.disconnect();

            // 关闭网关服务器的SSH连接
            gatewaySession.disconnect();
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
        }
    }

    // 自定义ProxySocketFactory实现SocketFactory接口
    static class ProxySocketFactory implements SocketFactory {
        private final Proxy proxy;

        ProxySocketFactory(Proxy proxy) {
            this.proxy = proxy;
        }

        public Socket createSocket(String host, int port) throws IOException {
            return new Socket(proxy);
        }

        public InputStream getInputStream(Socket socket) throws IOException {
            return socket.getInputStream();
        }

        public OutputStream getOutputStream(Socket socket) throws IOException {
            return socket.getOutputStream();
        }
    }
}
