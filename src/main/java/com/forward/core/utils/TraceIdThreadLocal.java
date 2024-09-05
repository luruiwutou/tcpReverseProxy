package com.forward.core.utils;

public class TraceIdThreadLocal {
    private static ThreadLocal<String> traceIdThreadLocal = new ThreadLocal<>();

    public static void set(String traceId) {
        traceIdThreadLocal.set(traceId);
    }

    public static String get() {
        return traceIdThreadLocal.get();
    }

    public static void clear() {
        traceIdThreadLocal.remove();
    }
}