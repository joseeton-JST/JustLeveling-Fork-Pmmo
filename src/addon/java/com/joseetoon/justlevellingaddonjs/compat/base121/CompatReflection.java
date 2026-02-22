package com.joseetoon.justlevellingaddonjs.compat.base121;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class CompatReflection {
    private CompatReflection() {
    }

    public static Method findMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        Class<?> cursor = type;
        while (cursor != null) {
            try {
                Method method = cursor.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                cursor = cursor.getSuperclass();
            }
        }
        return null;
    }

    public static Field findField(Class<?> type, String name) {
        Class<?> cursor = type;
        while (cursor != null) {
            try {
                Field field = cursor.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                cursor = cursor.getSuperclass();
            }
        }
        return null;
    }

    public static Object getFieldValue(Object target, String name) {
        try {
            Field field = findField(target.getClass(), name);
            return field != null ? field.get(target) : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static boolean setFieldValue(Object target, String name, Object value) {
        try {
            Field field = findField(target.getClass(), name);
            if (field == null) {
                return false;
            }
            field.set(target, value);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
