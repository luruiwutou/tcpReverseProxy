package com.forward.core.tcpReverseProxy.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SingletonBeanFactory<T> {

    private static volatile Map<Class, SingletonBeanFactory> instanceMap = new LinkedHashMap<Class, SingletonBeanFactory>();
    private static volatile SingletonBeanFactory instance;

    private final Supplier<T> supplier;

    private SingletonBeanFactory(Supplier<T> supplier) {
        this.supplier = supplier;
    }


    public static <T> SingletonBeanFactory<T> getBeanInstance(Class<T> clz) {
        SingletonBeanFactory singletonBeanFactory = instanceMap.get(clz);
        if (singletonBeanFactory == null) {
            synchronized (SingletonBeanFactory.class) {
                if (singletonBeanFactory == null) {
                    singletonBeanFactory = new SingletonBeanFactory<>(() -> SpringUtils.getBean(clz));
                    instanceMap.put(clz, singletonBeanFactory);
                }
            }
        }
        return singletonBeanFactory;
    }

    public T getSingleton() {
        return supplier.get();
    }
}
