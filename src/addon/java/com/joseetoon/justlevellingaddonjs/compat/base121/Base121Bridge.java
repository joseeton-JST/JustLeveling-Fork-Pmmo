package com.joseetoon.justlevellingaddonjs.compat.base121;

import com.mojang.logging.LogUtils;
import com.seniors.justlevelingfork.handler.HandlerAptitude;
import com.seniors.justlevelingfork.network.packet.common.AptitudeLevelUpSP;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.seniors.justlevelingfork.registry.title.Title;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

public final class Base121Bridge {
    private static final Logger LOGGER = LogUtils.getLogger();

    private Base121Bridge() {
    }

    public static void refreshAptitudeCache() {
        try {
            Method invalidate = HandlerAptitude.class.getMethod("invalidateCache");
            invalidate.invoke(null);
            return;
        } catch (Throwable ignored) {
        }

        try {
            Method forceRefresh = HandlerAptitude.class.getMethod("ForceRefresh");
            forceRefresh.invoke(null);
        } catch (Throwable throwable) {
            LOGGER.warn("[JustLevellingAddonJS] Could not refresh HandlerAptitude cache via compatibility bridge", throwable);
        }
    }

    public static int requiredPoints(int aptitudeLevel, Aptitude aptitude) {
        try {
            Method method = AptitudeLevelUpSP.class.getMethod("requiredPoints", int.class, Aptitude.class);
            Object value = method.invoke(null, aptitudeLevel, aptitude);
            if (value instanceof Number number) {
                return number.intValue();
            }
        } catch (Throwable ignored) {
        }

        try {
            Method method = AptitudeLevelUpSP.class.getMethod("requiredPoints", int.class);
            Object value = method.invoke(null, aptitudeLevel);
            if (value instanceof Number number) {
                return number.intValue();
            }
        } catch (Throwable ignored) {
        }

        return 0;
    }

    public static int requiredExperienceLevels(int aptitudeLevel, Aptitude aptitude) {
        try {
            Method method = AptitudeLevelUpSP.class.getMethod("requiredExperienceLevels", int.class, Aptitude.class);
            Object value = method.invoke(null, aptitudeLevel, aptitude);
            if (value instanceof Number number) {
                return number.intValue();
            }
        } catch (Throwable ignored) {
        }

        try {
            Method method = AptitudeLevelUpSP.class.getMethod("requiredExperienceLevels", int.class);
            Object value = method.invoke(null, aptitudeLevel);
            if (value instanceof Number number) {
                return number.intValue();
            }
        } catch (Throwable ignored) {
        }

        return 0;
    }

    public static Component titleDisplayComponentOrFallback(Title title) {
        if (title == null) {
            return Component.empty();
        }

        try {
            Method method = title.getClass().getMethod("getDisplayNameComponentOrFallback");
            Object value = method.invoke(title);
            if (value instanceof Component component) {
                return component;
            }
        } catch (Throwable ignored) {
        }

        String key = safeStringCall(title, "getKey", "");
        if (!key.isEmpty()) {
            return Component.translatable(key);
        }

        String name = safeStringCall(title, "getName", "title");
        return Component.literal(fallbackName(name));
    }

    public static String titleDisplayNameOrFallback(Title title) {
        return titleDisplayComponentOrFallback(title).getString();
    }

    public static int aptitudeDefaultLevelCap() {
        try {
            Class<?> handlerClass = Class.forName("com.seniors.justlevelingfork.handler.HandlerCommonConfig");
            Field handlerField = handlerClass.getField("HANDLER");
            Object holder = handlerField.get(null);
            Method instanceMethod = holder.getClass().getMethod("instance");
            Object config = instanceMethod.invoke(holder);
            Field capField = findField(config.getClass(), "aptitudeMaxLevel");
            if (capField != null) {
                capField.setAccessible(true);
                Object value = capField.get(config);
                if (value instanceof Number number) {
                    return Math.max(1, number.intValue());
                }
            }
        } catch (Throwable ignored) {
        }
        return 32;
    }

    public static int aptitudeDefaultBaseCost() {
        try {
            Class<?> handlerClass = Class.forName("com.seniors.justlevelingfork.handler.HandlerCommonConfig");
            Field handlerField = handlerClass.getField("HANDLER");
            Object holder = handlerField.get(null);
            Method instanceMethod = holder.getClass().getMethod("instance");
            Object config = instanceMethod.invoke(holder);
            Field baseField = findField(config.getClass(), "aptitudeFirstCostLevel");
            if (baseField != null) {
                baseField.setAccessible(true);
                Object value = baseField.get(config);
                if (value instanceof Number number) {
                    return Math.max(1, number.intValue());
                }
            }
        } catch (Throwable ignored) {
        }
        return 2;
    }

    public static ResourceLocation parseTexture(String texture) {
        if (texture == null || texture.isBlank()) {
            throw new IllegalArgumentException("Texture cannot be null or empty");
        }

        String normalized = texture.toLowerCase(Locale.ROOT);
        try {
            if (normalized.contains(":")) {
                return new ResourceLocation(normalized);
            }

            Class<?> handlerResources = Class.forName("com.seniors.justlevelingfork.handler.HandlerResources");
            Method create = handlerResources.getMethod("create", String.class);
            Object value = create.invoke(null, normalized);
            if (value instanceof ResourceLocation resourceLocation) {
                return resourceLocation;
            }
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Throwable ignored) {
        }

        return new ResourceLocation(normalized);
    }

    private static String safeStringCall(Object target, String methodName, String fallback) {
        try {
            Method method = target.getClass().getMethod(methodName);
            Object value = method.invoke(target);
            return value instanceof String string ? string : fallback;
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    private static Field findField(Class<?> type, String name) {
        Class<?> cursor = type;
        while (cursor != null) {
            try {
                return cursor.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                cursor = cursor.getSuperclass();
            }
        }
        return null;
    }

    private static String fallbackName(String raw) {
        if (raw == null || raw.isBlank()) {
            return "Title";
        }

        String[] parts = raw.split("[_\\-\\s]+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }

            if (builder.length() > 0) {
                builder.append(' ');
            }

            String normalized = part.toLowerCase(Locale.ROOT);
            builder.append(Character.toUpperCase(normalized.charAt(0)));
            if (normalized.length() > 1) {
                builder.append(normalized.substring(1));
            }
        }

        return builder.isEmpty() ? raw : builder.toString();
    }
}
