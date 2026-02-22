package com.joseetoon.justlevellingaddonjs.kubejs.compat;

import com.joseetoon.justlevellingaddonjs.compat.base121.BackportRegistryState;
import com.joseetoon.justlevellingaddonjs.compat.base121.Base121Bridge;
import com.seniors.justlevelingfork.registry.RegistryPassives;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.seniors.justlevelingfork.registry.passive.Passive;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class PassiveCompat {
    private static final Map<String, Integer> POINT_COST_OVERRIDES = new LinkedHashMap<>();

    private PassiveCompat() {
    }

    public static Passive addByAttributeId(String passiveName, String aptitudeName, String texture, String attributeId, String attributeUUID, Object attributeValue, int... levelsRequired) {
        return add(passiveName, aptitudeName, texture, attributeId, attributeUUID, attributeValue, levelsRequired);
    }

    public static Passive add(String passiveName, String aptitudeName, String texture, String attributeId, String attributeUUID, Object attributeValue, int... levelsRequired) {
        if (attributeId == null || attributeId.isBlank()) {
            throw new IllegalArgumentException("Attribute id cannot be empty for passive: " + passiveName);
        }

        ResourceLocation attributeLocation;
        try {
            String normalizedAttribute = attributeId.toLowerCase(Locale.ROOT);
            attributeLocation = normalizedAttribute.contains(":")
                    ? new ResourceLocation(normalizedAttribute)
                    : new ResourceLocation("minecraft", normalizedAttribute);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid attribute id: " + attributeId, exception);
        }

        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(attributeLocation);
        if (attribute == null) {
            throw new IllegalArgumentException("Attribute id doesn't exist: " + attributeId);
        }

        ResourceLocation id = AddonResourceUtils.parseResourceLocation(passiveName, true);
        if (id == null) {
            throw new IllegalArgumentException("Invalid passive name: " + passiveName);
        }
        return addWithId(id, aptitudeName, texture, attribute, attributeUUID, attributeValue, levelsRequired);
    }

    public static Passive addWithId(String passiveNameOrId, String aptitudeName, String texture, Attribute attribute, String attributeUUID, Object attributeValue, int... levelsRequired) {
        ResourceLocation id = AddonResourceUtils.parseResourceLocation(passiveNameOrId, false);
        if (id == null) {
            throw new IllegalArgumentException("Invalid passive id: " + passiveNameOrId);
        }
        return addWithId(id, aptitudeName, texture, attribute, attributeUUID, attributeValue, levelsRequired);
    }

    public static Passive addWithId(ResourceLocation id, String aptitudeName, String texture, Attribute attribute, String attributeUUID, Object attributeValue, int... levelsRequired) {
        if (id == null || id.getPath().isBlank()) {
            throw new IllegalArgumentException("Passive id cannot be empty");
        }
        if (aptitudeName == null || aptitudeName.isBlank()) {
            throw new IllegalArgumentException("Aptitude name cannot be empty for passive: " + id);
        }
        if (attribute == null) {
            throw new IllegalArgumentException("Attribute cannot be null for passive: " + id);
        }
        if (levelsRequired == null || levelsRequired.length == 0) {
            throw new IllegalArgumentException("levelsRequired must contain at least one value for passive: " + id);
        }

        String normalizedAptitude = aptitudeName.toLowerCase(Locale.ROOT);
        Aptitude aptitude = AptitudeAPI.getByName(normalizedAptitude);
        if (aptitude == null) {
            throw new IllegalArgumentException("Aptitude name doesn't exist: " + aptitudeName);
        }

        Passive passive = new Passive(
                id,
                aptitude,
                Base121Bridge.parseTexture(AddonResourceUtils.normalizeTexture(texture)),
                attribute,
                attributeUUID,
                attributeValue,
                levelsRequired
        );
        BackportRegistryState.addPendingPassive(id, passive);
        return passive;
    }

    public static Passive getByName(String nameOrId) {
        Passive fromRegistry = RegistryPassives.getPassive(nameOrId);
        if (fromRegistry != null) {
            return fromRegistry;
        }

        ResourceLocation parsed = AddonResourceUtils.parseResourceLocation(nameOrId, false);
        String normalizedPath = parsed != null ? parsed.getPath() : BackportRegistryState.normalizePath(nameOrId);
        return BackportRegistryState.findPendingPassive(parsed, normalizedPath);
    }

    public static int getPointCost(Passive passive) {
        if (passive == null) {
            return 1;
        }

        Object value = invoke(passive, "getPointCost");
        if (value instanceof Number number) {
            return Math.max(1, number.intValue());
        }

        Integer override = POINT_COST_OVERRIDES.get(passive.getName());
        return override != null ? Math.max(1, override) : 1;
    }

    public static void setPointCost(Passive passive, int cost) {
        if (passive == null) {
            return;
        }

        int safeCost = Math.max(1, cost);
        if (!invokeVoid(passive, "setPointCost", new Class[]{int.class}, safeCost)) {
            invokeVoid(passive, "setSpCost", new Class[]{int.class}, safeCost);
        }
        POINT_COST_OVERRIDES.put(passive.getName(), safeCost);
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
