package com.forward.core.sftp;

import com.jcraft.jsch.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ProxySSH implements Proxy {
    private final Channel channel;
    private final SocketFactory socketFactory;

    ProxySSH(Channel channel, SocketFactory socketFactory) {
        this.channel = channel;
        this.socketFactory = socketFactory;
    }

    public void connect(SocketFactory socket_factory, String host, int port, int timeout) throws JSchException {
        try {
            Socket socket = socketFactory.createSocket(host, port);
            channel.setInputStream(socket_factory.getInputStream(socket));
            channel.setOutputStream(socket_factory.getOutputStream(socket));
            channel.connect(timeout);
        } catch (IOException e) {
            throw new JSchException("Failed to connect to proxy", e);
        }
    }

    public InputStream getInputStream() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public OutputStream getOutputStream()  {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Socket getSocket() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void close() {
        channel.disconnect();
    }
}
