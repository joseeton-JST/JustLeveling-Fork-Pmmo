package com.joseetoon.justlevellingaddonjs.kubejs.compat;

import com.joseetoon.justlevellingaddonjs.compat.base121.BackportRegistryState;
import com.joseetoon.justlevellingaddonjs.compat.base121.Base121Bridge;
import com.seniors.justlevelingfork.client.core.Value;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.seniors.justlevelingfork.registry.skills.Skill;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class SkillCompat {
    private static final Map<String, Integer> POINT_COST_OVERRIDES = new LinkedHashMap<>();

    private SkillCompat() {
    }

    public static Skill add(String skillName, String aptitudeName, int levelRequirement, String texture, Value... values) {
        ResourceLocation id = AddonResourceUtils.parseResourceLocation(skillName, true);
        if (id == null) {
            throw new IllegalArgumentException("Invalid skill name: " + skillName);
        }
        return addWithId(id, aptitudeName, levelRequirement, texture, values);
    }

    public static Skill addWithId(String skillNameOrId, String aptitudeName, int levelRequirement, String texture, Value... values) {
        ResourceLocation id = AddonResourceUtils.parseResourceLocation(skillNameOrId, false);
        if (id == null) {
            throw new IllegalArgumentException("Invalid skill id: " + skillNameOrId);
        }
        return addWithId(id, aptitudeName, levelRequirement, texture, values);
    }

    public static Skill addWithId(ResourceLocation id, String aptitudeName, int levelRequirement, String texture, Value... values) {
        if (id == null || id.getPath().isBlank()) {
            throw new IllegalArgumentException("Skill id cannot be empty");
        }
        if (aptitudeName == null || aptitudeName.isBlank()) {
            throw new IllegalArgumentException("Aptitude name cannot be empty for skill: " + id);
        }

        String normalizedAptitude = aptitudeName.toLowerCase(Locale.ROOT);
        Aptitude aptitude = AptitudeAPI.getByName(normalizedAptitude);
        if (aptitude == null) {
            throw new IllegalArgumentException("Aptitude name doesn't exist: " + aptitudeName);
        }

        Skill skill = new Skill(id, aptitude, levelRequirement, Base121Bridge.parseTexture(AddonResourceUtils.normalizeTexture(texture)), values);
        BackportRegistryState.addPendingSkill(id, skill);
        return skill;
    }

    public static Skill getByName(String nameOrId) {
        Skill fromRegistry = RegistrySkills.getSkill(nameOrId);
        if (fromRegistry != null) {
            return fromRegistry;
        }

        ResourceLocation parsed = AddonResourceUtils.parseResourceLocation(nameOrId, false);
        String normalizedPath = parsed != null ? parsed.getPath() : BackportRegistryState.normalizePath(nameOrId);
        return BackportRegistryState.findPendingSkill(parsed, normalizedPath);
    }

    public static int getPointCost(Skill skill) {
        if (skill == null) {
            return 1;
        }

        Object value = invoke(skill, "getPointCost");
        if (value instanceof Number number) {
            return Math.max(1, number.intValue());
        }

        Integer override = POINT_COST_OVERRIDES.get(skill.getName());
        return override != null ? Math.max(1, override) : 1;
    }

    public static void setPointCost(Skill skill, int cost) {
        if (skill == null) {
            return;
        }

        int safeCost = Math.max(1, cost);
        if (!invokeVoid(skill, "setPointCost", new Class[]{int.class}, safeCost)) {
            invokeVoid(skill, "setSpCost", new Class[]{int.class}, safeCost);
        }
        POINT_COST_OVERRIDES.put(skill.getName(), safeCost);
    }

    private static Object invoke(Object target, String methodName, Class<?>... parameterTypes) {
        try {
            Method method = findMethod(target.getClass(), methodName, parameterTypes);
            if (method == null) {
                return null;
            }
            method.setAccessible(true);
            return method.invoke(target);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean invokeVoid(Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
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
