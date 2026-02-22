package com.joseetoon.justlevellingaddonjs.compat;

import com.joseetoon.justlevellingaddonjs.kubejs.compat.PassiveCompat;
import com.joseetoon.justlevellingaddonjs.kubejs.compat.SkillCompat;
import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.seniors.justlevelingfork.registry.skills.Skill;

import java.lang.reflect.Method;

public final class CapabilityCompat {
    private CapabilityCompat() {
    }

    public static boolean isSkillUnlocked(AptitudeCapability capability, Skill skill) {
        Boolean result = invokeBoolean(capability, "isSkillUnlocked", new Class[]{Skill.class}, skill);
        return result != null ? result : false;
    }

    public static void setSkillUnlocked(AptitudeCapability capability, Skill skill, boolean unlocked) {
        if (!invokeVoid(capability, "setSkillUnlocked", new Class[]{Skill.class, boolean.class}, skill, unlocked)
                && capability != null && skill != null && !unlocked) {
            capability.setToggleSkill(skill, false);
        }
    }

    public static boolean tryUnlockSkill(AptitudeCapability capability, Skill skill) {
        Boolean result = invokeBoolean(capability, "tryUnlockSkill", new Class[]{Skill.class}, skill);
        return result != null ? result : false;
    }

    public static int getAptitudeSkillPointsAvailable(AptitudeCapability capability, Aptitude aptitude) {
        Number value = invokeNumber(capability, "getAptitudeSkillPointsAvailable", new Class[]{Aptitude.class}, aptitude);
        return value != null ? Math.max(0, value.intValue()) : 0;
    }

    public static int getAptitudeSkillPointsSpent(AptitudeCapability capability, Aptitude aptitude) {
        Number value = invokeNumber(capability, "getAptitudeSkillPointsSpent", new Class[]{Aptitude.class}, aptitude);
        return value != null ? Math.max(0, value.intValue()) : 0;
    }

    public static boolean trySpendAptitudePoints(AptitudeCapability capability, Aptitude aptitude, int amount) {
        Boolean result = invokeBoolean(capability, "trySpendAptitudePoints", new Class[]{Aptitude.class, int.class}, aptitude, amount);
        return result != null ? result : false;
    }

    public static void refundAptitudePoints(AptitudeCapability capability, Aptitude aptitude, int amount) {
        if (!invokeVoid(capability, "refundAptitudePoints", new Class[]{Aptitude.class, int.class}, aptitude, amount)
                && capability != null && aptitude != null && amount > 0) {
            int spent = getAptitudeSkillPointsSpent(capability, aptitude);
            int targetSpent = Math.max(0, spent - amount);
            int available = getAptitudeSkillPointsAvailable(capability, aptitude);
            int total = spent + available;
            if (total > 0 && targetSpent <= total) {
                // Best-effort fallback: no-op when backport methods are missing.
            }
        }
    }

    public static void respecAptitude(AptitudeCapability capability, Aptitude aptitude) {
        invokeVoid(capability, "respecAptitude", new Class[]{Aptitude.class}, aptitude);
    }

    public static int getSkillPointCost(Skill skill) {
        return SkillCompat.getPointCost(skill);
    }

    public static int getPassivePointCost(com.seniors.justlevelingfork.registry.passive.Passive passive) {
        return PassiveCompat.getPointCost(passive);
    }

    private static Boolean invokeBoolean(Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
        Object value = invoke(target, methodName, parameterTypes, args);
        return value instanceof Boolean bool ? bool : null;
    }

    private static Number invokeNumber(Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
        Object value = invoke(target, methodName, parameterTypes, args);
        return value instanceof Number number ? number : null;
    }

    private static boolean invokeVoid(Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
        if (target == null) {
            return false;
        }
        try {
            Method method = findMethod(target.getClass(), methodName, parameterTypes);
            if (method == null) {
                return false;
            }
            method.setAccessible(true);
            method.invoke(target, args);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
        if (target == null) {
            return null;
        }
        try {
            Method method = findMethod(target.getClass(), methodName, parameterTypes);
            if (method == null) {
                return null;
            }
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (Throwable ignored) {
            return null;
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
}
