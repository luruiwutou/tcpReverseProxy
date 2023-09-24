package com.forward.core.tcpReverseProxy.interfaces;

@FunctionalInterface
public interface FourConsumer<T, U, V,R> {
    void accept(T t, U u, V v,R r);
}
