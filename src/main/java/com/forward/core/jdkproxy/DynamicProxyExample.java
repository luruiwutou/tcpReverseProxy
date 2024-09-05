package com.forward.core.jdkproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

interface SomeInterface {
    void doSomething();
}

class SomeClass implements SomeInterface {
    public void doSomething() {
        System.out.println("Original operation.");
    }
}

class CustomInvocationHandler implements InvocationHandler {
    private final Object target;

    public CustomInvocationHandler(Object target) {
        this.target = target;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("Before invoking method.");
        Object result = method.invoke(target, args);
        System.out.println("After invoking method.");
        return result;
    }
}

public class DynamicProxyExample {
//    public static void main(String[] args) {
//        SomeInterface original = new SomeClass();
//        InvocationHandler handler = new CustomInvocationHandler(original);
//
//        SomeInterface proxy = (SomeInterface) Proxy.newProxyInstance(
//                original.getClass().getClassLoader(),
//                original.getClass().getInterfaces(),
//                handler
//        );
//
//        proxy.doSomething();
//    }
}
