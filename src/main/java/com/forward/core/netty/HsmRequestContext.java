package com.forward.core.netty;

import io.netty.util.concurrent.Promise;

public class HsmRequestContext {
    /**
     * 当前的TCP请求byte
     */
    byte[] message;

    /**
     * 主线程发送完请求后，在该promise上等待
     */
    Promise<byte[]> defaultPromise;
    HsmRequestContext(byte[] message, Promise<byte[]> defaultPromise) {
        this.defaultPromise = defaultPromise;
        this.message = message;
    }

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public Promise<byte[]> getDefaultPromise() {
        return defaultPromise;
    }

    public void setDefaultPromise(Promise<byte[]> defaultPromise) {
        this.defaultPromise = defaultPromise;
    }
}
