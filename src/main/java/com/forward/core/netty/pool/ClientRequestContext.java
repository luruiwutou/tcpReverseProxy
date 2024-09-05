package com.forward.core.netty.pool;

import io.netty.util.concurrent.Promise;
import lombok.Data;

@Data
public class ClientRequestContext<T,R> {
    /**
     * 当前的TCP请求数据
     */
    T message;

    /**
     * 主线程发送完请求后，在该promise上等待
     */
    Promise<R> defaultPromise;

    ClientRequestContext(T message, Promise<R> defaultPromise) {
        this.defaultPromise = defaultPromise;
        this.message = message;
    }


}
