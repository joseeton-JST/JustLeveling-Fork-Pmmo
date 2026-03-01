package com.joseetoon.justlevellingaddonjs.compat;

import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class AptitudeCompat {
    private static final Map<String, Integer> LEVEL_CAP_OVERRIDES = new LinkedHashMap<>();
    private static final Map<String, Integer> BASE_LEVEL_COST_OVERRIDES = new LinkedHashMap<>();
    private static final Map<String, Integer> SKILL_POINT_INTERVAL_OVERRIDES = new LinkedHashMap<>();
    private static final Map<String, Integer> BACKGROUND_REPEAT_OVERRIDES = new LinkedHashMap<>();

    private AptitudeCompat() {
    }

    public static boolean isEnabled(Aptitude aptitude) {
        if (aptitude == null) {
            return false;
        }

        Boolean value = invokeBooleanMethod(aptitude, "isEnabled");
        if (value != null) {
            return value;
        }

        value = invokeBooleanMethod(aptitude, "getEnabled");
        if (value != null) {
            return value;
        }

        value = readBooleanField(aptitude, "enabled");
        return value != null ? value : true;
    }

    public static void setEnabled(Aptitude aptitude, boolean enabled) {
        if (aptitude == null) {
            return;
        }

        if (invokeVoidMethod(aptitude, "setEnabled", boolean.class, enabled)) {
            return;
        }

        writeField(aptitude, "enabled", enabled);
    }

    public static boolean isHidden(Aptitude aptitude) {
        if (aptitude == null) {
            return false;
        }

        Boolean value = invokeBooleanMethod(aptitude, "isHidden");
        if (value != null) {
            return value;
        }

        value = invokeBooleanMethod(aptitude, "getHidden");
        if (value != null) {
            return value;
        }

        value = readBooleanField(aptitude, "hidden");
        return value != null && value;
    }

    public static void setHidden(Aptitude aptitude, boolean hidden) {
        if (aptitude == null) {
            return;
        }

        if (invokeVoidMethod(aptitude, "setHidden", boolean.class, hidden)) {
            return;
        }

        writeField(aptitude, "hidden", hidden);
    }

    public static void setBackground(Aptitude aptitude, ResourceLocation resourceLocation) {
        if (aptitude == null || resourceLocation == null) {
            return;
        }

        if (invokeVoidMethod(aptitude, "setBackground", ResourceLocation.class, resourceLocation)) {
            return;
        }

        if (invokeVoidMethod(aptitude, "setBackgroundTexture", ResourceLocation.class, resourceLocation)) {
            return;
        }

        writeField(aptitude, "background", resourceLocation);
    }

    public static String getName(Aptitude aptitude) {
        if (aptitude == null) {
            return "";
        }

        String byMethod = invokeStringMethod(aptitude, "getName");
        if (byMethod != null && !byMethod.isBlank()) {
            return byMethod;
        }

        Object key = readField(aptitude, "key");
        if (key instanceof ResourceLocation resourceLocation) {
            return resourceLocation.getPath();
        }

        Object name = readField(aptitude, "name");
        if (name instanceof String string && !string.isBlank()) {
            return string;
        }

        return "";
    }

    public static String getDisplayNameOrFallback(Aptitude aptitude) {
        if (aptitude == null) {
            return "";
        }

        String byMethod = invokeStringMethod(aptitude, "getDisplayNameOrFallback");
        if (byMethod != null && !byMethod.isBlank()) {
            return byMethod;
        }

        String key = invokeStringMethod(aptitude, "getKey");
        if (key != null && !key.isBlank()) {
            String translated = net.minecraft.network.chat.Component.translatable(key).getString();
            if (!translated.equals(key)) {
                return translated;
            }
        }

        String name = getName(aptitude);
        if (name.isBlank()) {
            return "Unknown";
        }
        String[] parts = name.split("[_\\-\\s]+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
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
        return builder.isEmpty() ? name : builder.toString();
    }

    public static int getLevelCap(Aptitude aptitude) {
        if (aptitude == null) {
            return 1;
        }

        Object byMethod = invokeMethod(aptitude, "getLevelCap");
        if (byMethod instanceof Number number) {
            return Math.max(1, number.intValue());
        }

        Integer override = LEVEL_CAP_OVERRIDES.get(getName(aptitude));
        if (override != null && override > 0) {
            return override;
        }

        return 32;
    }

    public static void setLevelCap(Aptitude aptitude, int cap) {
        if (aptitude == null) {
            return;
        }

        if (invokeVoidMethod(aptitude, "setLevelCap", int.class, cap)) {
            return;
        }
        LEVEL_CAP_OVERRIDES.put(getName(aptitude), cap);
    }

    public static int getBaseLevelCost(Aptitude aptitude) {
        if (aptitude == null) {
            return 1;
        }

        Object byMethod = invokeMethod(aptitude, "getBaseLevelCost");
        if (byMethod instanceof Number number) {
            return Math.max(1, number.intValue());
        }

        Integer override = BASE_LEVEL_COST_OVERRIDES.get(getName(aptitude));
        if (override != null && override > 0) {
            return override;
        }

        return 2;
    }

    public static void setBaseLevelCost(Aptitude aptitude, int cost) {
        if (aptitude == null) {
            return;
        }

        if (invokeVoidMethod(aptitude, "setBaseLevelCost", int.class, cost)) {
            return;
        }
        BASE_LEVEL_COST_OVERRIDES.put(getName(aptitude), cost);
    }

    public static int getSkillPointInterval(Aptitude aptitude) {
        if (aptitude == null) {
            return 1;
        }

        Object byMethod = invokeMethod(aptitude, "getSkillPointInterval");
        if (byMethod instanceof Number number) {
            return Math.max(1, number.intValue());
        }

        Integer override = SKILL_POINT_INTERVAL_OVERRIDES.get(getName(aptitude));
        if (override != null && override > 0) {
            return override;
        }

        return 2;
    }

    public static void setSkillPointInterval(Aptitude aptitude, int interval) {
        if (aptitude == null) {
            return;
        }

        if (invokeVoidMethod(aptitude, "setSkillPointInterval", int.class, interval)) {
            return;
        }
        SKILL_POINT_INTERVAL_OVERRIDES.put(getName(aptitude), Math.max(1, interval));
    }

    public static int getBackgroundRepeat(Aptitude aptitude) {
        if (aptitude == null) {
            return 0;
        }

        Object byMethod = invokeMethod(aptitude, "getBackgroundRepeat");
        if (byMethod instanceof Number number) {
            return Math.max(0, number.intValue());
        }

        Integer override = BACKGROUND_REPEAT_OVERRIDES.get(getName(aptitude));
        return override != null ? Math.max(0, override) : 0;
    }

    public static void setBackgroundRepeat(Aptitude aptitude, int repeat) {
        if (aptitude == null) {
            return;
        }

        if (invokeVoidMethod(aptitude, "setBackgroundRepeat", int.class, repeat)) {
            return;
        }
        BACKGROUND_REPEAT_OVERRIDES.put(getName(aptitude), Math.max(0, repeat));
    }

    public static void setDisplayNameOverride(Aptitude aptitude, String displayName) {
        if (aptitude == null) {
            return;
        }
        invokeVoidMethod(aptitude, "setDisplayNameOverride", String.class, displayName);
    }

    public static String getAbbreviationOrFallback(Aptitude aptitude) {
        if (aptitude == null) {
            return "???";
        }

        String byMethod = invokeStringMethod(aptitude, "getAbbreviationOrFallback");
        if (byMethod != null && !byMethod.isBlank()) {
            return byMethod;
        }

        String displayName = getDisplayNameOrFallback(aptitude);
        if (displayName.isBlank()) {
            return "???";
        }

        String[] parts = displayName.split("[^a-zA-Z0-9]+");
        StringBuilder abbreviation = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }
            abbreviation.append(Character.toUpperCase(part.charAt(0)));
            if (abbreviation.length() >= 3) {
                break;
            }
        }

        if (abbreviation.isEmpty()) {
            String clean = displayName.replaceAll("[^a-zA-Z0-9]", "");
            if (clean.isEmpty()) {
                return "???";
            }
            return clean.substring(0, Math.min(3, clean.length())).toUpperCase(Locale.ROOT);
        }

        return abbreviation.toString();
    }

    public static String getAbbreviationOverride(Aptitude aptitude) {
        if (aptitude == null) {
            return null;
        }
        String value = invokeStringMethod(aptitude, "getAbbreviationOverride");
        return value == null || value.isBlank() ? null : value;
    }

    public static void setAbbreviationOverride(Aptitude aptitude, String abbreviation) {
        if (aptitude == null) {
            return;
        }
        invokeVoidMethod(aptitude, "setAbbreviationOverride", String.class, abbreviation);
    }

    public static void clearAbbreviationOverride(Aptitude aptitude) {
        if (aptitude == null) {
            return;
        }
        invokeMethodVoid(aptitude, "clearAbbreviationOverride", new Class[0], new Object[0]);
    }

    public static void setLevelStaggering(Aptitude aptitude, String... levelStaggering) {
        if (aptitude == null) {
            return;
        }
        invokeVoidMethod(aptitude, "setLevelStaggering", String[].class, (Object) levelStaggering);
    }

    public static String[] getLevelStaggering(Aptitude aptitude) {
        if (aptitude == null) {
            return new String[0];
        }
        Object value = invokeMethod(aptitude, "getLevelStaggering");
        return value instanceof String[] strings ? strings : new String[0];
    }

    public static void setLockedTextures(Aptitude aptitude, String... textures) {
        if (aptitude == null || textures == null || textures.length == 0) {
            return;
        }
        invokeVoidMethod(aptitude, "setLockedTextures", String[].class, (Object) textures);
    }

    public static void setRankIcon(Aptitude aptitude, int rank, String resourceLocation) {
        if (aptitude == null) {
            return;
        }
        if (!invokeVoidMethod(aptitude, "setRankIcon", int.class, String.class, rank, resourceLocation)) {
            try {
                invokeVoidMethod(aptitude, "setRankIcon", int.class, ResourceLocation.class, rank, new ResourceLocation(resourceLocation.toLowerCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public static void removeRankIcon(Aptitude aptitude, int rank) {
        if (aptitude == null) {
            return;
        }
        invokeVoidMethod(aptitude, "removeRankIcon", int.class, rank);
    }

    public static int getLevelUpPointCost(Aptitude aptitude, int aptitudeLevel) {
        if (aptitude == null) {
            return 0;
        }
        Object value = invokeMethod(aptitude, "getLevelUpPointCost", new Class[]{int.class}, new Object[]{aptitudeLevel});
        if (value instanceof Number number) {
            return Math.max(0, number.intValue());
        }
        return Math.max(0, getBaseLevelCost(aptitude));
    }

    public static int getLevelUpExperienceLevels(Aptitude aptitude, int aptitudeLevel) {
        if (aptitude == null) {
            return 0;
        }
        Object value = invokeMethod(aptitude, "getLevelUpExperienceLevels", new Class[]{int.class}, new Object[]{aptitudeLevel});
        if (value instanceof Number number) {
            return Math.max(0, number.intValue());
        }
        return 0;
    }

    private static Object invokeMethod(Object target, String methodName, Class<?>[] parameterTypes, Object[] parameters) {
        try {
            Method method = findMethod(target.getClass(), methodName, parameterTypes);
            if (method == null) {
                return null;
            }
            method.setAccessible(true);
            return method.invoke(target, parameters);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Object invokeMethod(Object target, String methodName) {
        return invokeMethod(target, methodName, new Class<?>[0], new Object[0]);
    }

    private static boolean invokeVoidMethod(Object target, String methodName, Class<?> parameterTypeA, Class<?> parameterTypeB, Object valueA, Object valueB) {
        return invokeMethodVoid(target, methodName, new Class[]{parameterTypeA, parameterTypeB}, new Object[]{valueA, valueB});
    }

    private static Boolean invokeBooleanMethod(Object target, String methodName) {
        try {
            Method method = findMethod(target.getClass(), methodName);
            if (method == null) {
                return null;
            }
            method.setAccessible(true);
            Object result = method.invoke(target);
            return result instanceof Boolean bool ? bool : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String invokeStringMethod(Object target, String methodName) {
        try {
            Method method = findMethod(target.getClass(), methodName);
            if (method == null) {
                return null;
            }
            method.setAccessible(true);
            Object result = method.invoke(target);
            return result instanceof String string ? string : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean invokeVoidMethod(Object target, String methodName, Class<?> parameterType, Object value) {
        return invokeMethodVoid(target, methodName, new Class[]{parameterType}, new Object[]{value});
    }

    private static boolean invokeMethodVoid(Object target, String methodName, Class<?>[] parameterTypes, Object[] parameters) {
        try {
            Method method = findMethod(target.getClass(), methodName, parameterTypes);
            if (method == null) {
                return false;
            }
            method.setAccessible(true);
            method.invoke(target, parameters);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Boolean readBooleanField(Object target, String fieldName) {
        Object value = readField(target, fieldName);
        return value instanceof Boolean bool ? bool : null;
    }

    private static Object readField(Object target, String fieldName) {
        try {
            Field field = findField(target.getClass(), fieldName);
            if (field == null) {
                return null;
            }
            field.setAccessible(true);
            return field.get(target);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void writeField(Object target, String fieldName, Object value) {
        try {
            Field field = findField(target.getClass(), fieldName);
            if (field == null) {
                return;
            }
            field.setAccessible(true);
            field.set(target, value);
        } catch (Throwable ignored) {
        }
    }

    private static Method findMethod(Class<?> type, String methodName, Class<?>... parameterTypes) {
        Class<?> cursor = type;
        while (cursor != null) {
            try {
                return cursor.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException ignored) {
                cursor = cursor.getSuperclass();
            }
        }
        return null;
    }

    private static Field findField(Class<?> type, String fieldName) {
        Class<?> cursor = type;
        while (cursor != null) {
            try {
                return cursor.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                cursor = cursor.getSuperclass();
            }
        }
        return null;
    }
}
