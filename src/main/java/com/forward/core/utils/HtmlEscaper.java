package com.forward.core.utils;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

public class HtmlEscaper {

    public static <T> T escapeHtml(T object) {
        if (object == null) {
            return null;
        }

        Class<?> clazz = object.getClass();
        Encoder encoder = ESAPI.encoder();

        try {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(object);
                if (value instanceof String) {
                    String escapedValue = encoder.encodeForHTML((String) value);
                    field.set(object, escapedValue);
                } else if (value instanceof Collection) {
                    escapeCollection((Collection<?>) value);
                } else if (value != null && !value.getClass().getName().startsWith("java")) {
                    // 递归调用，处理对象中的其他对象属性
                    escapeHtml(value);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return object;
    }
    public static void escapeCollection(Collection<?> collection) {
        List<Object> list = (List<Object>) collection;
        for (int i = 0; i < list.size(); i++) {
            Object element = list.get(i);
            if (element instanceof String) {
                String escapedElement = ESAPI.encoder().encodeForHTML((String) element);
                list.set(i, escapedElement);
            } else if (element instanceof Collection) {
                // 如果集合中的元素是另一个集合，递归调用处理
                escapeCollection((Collection<?>) element);
            } else if (element != null && !element.getClass().getName().startsWith("java")) {
                // 如果集合中的元素是其他类型的对象，递归调用处理
                escapeHtml(element);
            }
        }
    }
}
