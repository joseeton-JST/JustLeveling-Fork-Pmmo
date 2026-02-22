package com.joseetoon.justlevellingaddonjs.compat;

import com.joseetoon.justlevellingaddonjs.kubejs.VisibilityLockAPI;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class JLScreenCompatAccess {
    private JLScreenCompatAccess() {
    }

    public static int getInt(Object target, String field, int fallback) {
        try {
            Field f = findField(target.getClass(), field);
            if (f == null) {
                return fallback;
            }
            f.setAccessible(true);
            Object value = f.get(target);
            return value instanceof Number number ? number.intValue() : fallback;
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    public static void setInt(Object target, String field, int value) {
        try {
            Field f = findField(target.getClass(), field);
            if (f == null) {
                return;
            }
            f.setAccessible(true);
            f.set(target, value);
        } catch (Throwable ignored) {
        }
    }

    public static String getString(Object target, String field, String fallback) {
        try {
            Field f = findField(target.getClass(), field);
            if (f == null) {
                return fallback;
            }
            f.setAccessible(true);
            Object value = f.get(target);
            return value instanceof String string ? string : fallback;
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    public static void setString(Object target, String field, String value) {
        try {
            Field f = findField(target.getClass(), field);
            if (f == null) {
                return;
            }
            f.setAccessible(true);
            f.set(target, value);
        } catch (Throwable ignored) {
        }
    }

    public static int getStaticInt(Class<?> type, String field, int fallback) {
        try {
            Field f = findField(type, field);
            if (f == null) {
                return fallback;
            }
            f.setAccessible(true);
            Object value = f.get(null);
            return value instanceof Number number ? number.intValue() : fallback;
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    public static boolean getBoolean(Object target, String field, boolean fallback) {
        try {
            Field f = findField(target.getClass(), field);
            if (f == null) {
                return fallback;
            }
            f.setAccessible(true);
            Object value = f.get(target);
            return value instanceof Boolean b ? b : fallback;
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    public static void setBoolean(Object target, String field, boolean value) {
        try {
            Field f = findField(target.getClass(), field);
            if (f == null) {
                return;
            }
            f.setAccessible(true);
            f.set(target, value);
        } catch (Throwable ignored) {
        }
    }

    public static List<Aptitude> getVisibleAptitudes(Player player) {
        List<Aptitude> aptitudeList = new ArrayList<>();
        if (player == null) {
            return aptitudeList;
        }

        for (Aptitude aptitude : RegistryAptitudes.APTITUDES_REGISTRY.get().getValues()) {
            if (aptitude != null
                    && AptitudeCompat.isEnabled(aptitude)
                    && !AptitudeCompat.isHidden(aptitude)
                    && VisibilityLockAPI.isVisible(player, AptitudeCompat.getName(aptitude))) {
                aptitudeList.add(aptitude);
            }
        }
        return aptitudeList;
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
