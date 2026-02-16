package com.seniors.justlevelingfork.kubejs.compat;

import com.seniors.justlevelingfork.JustLevelingFork;
import com.seniors.justlevelingfork.registry.RegistryPassives;
import com.seniors.justlevelingfork.registry.passive.Passive;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Locale;

public class PassiveCreator {
    public static Passive createPassive(String name, String aptitudeName, String texture, String attributeId, String attributeUUID, Object attributeValue, int... levelsRequired) {
        ResourceLocation id = parseResourceLocation(name, true);
        if (id == null) {
            throw new IllegalArgumentException("Invalid passive name: " + name);
        }
        return createWithId(id, aptitudeName, texture, attributeId, attributeUUID, attributeValue, levelsRequired);
    }

    public static Passive createPassive(String name, String aptitudeName, String texture, String attributeId, String attributeUUID, Object attributeValue, int spCost, int[] levelsRequired) {
        Passive passive = createPassive(name, aptitudeName, texture, attributeId, attributeUUID, attributeValue, levelsRequired);
        passive.setPointCost(spCost);
        return passive;
    }

    public static Passive createPassiveWithCost(String name, String aptitudeName, String texture, String attributeId, String attributeUUID, Object attributeValue, int spCost, int... levelsRequired) {
        return createPassive(name, aptitudeName, texture, attributeId, attributeUUID, attributeValue, spCost, levelsRequired);
    }

    public static Passive createNewPassive(String id, String aptitudeName, String texture, String attributeId, String attributeUUID, Object attributeValue, int... levelsRequired) {
        ResourceLocation resourceLocation = parseResourceLocation(id, false);
        if (resourceLocation == null) {
            throw new IllegalArgumentException("Invalid passive id: " + id);
        }
        return createWithId(resourceLocation, aptitudeName, texture, attributeId, attributeUUID, attributeValue, levelsRequired);
    }

    public static Passive createNewPassive(String id, String aptitudeName, String texture, String attributeId, String attributeUUID, Object attributeValue, int spCost, int[] levelsRequired) {
        Passive passive = createNewPassive(id, aptitudeName, texture, attributeId, attributeUUID, attributeValue, levelsRequired);
        passive.setPointCost(spCost);
        return passive;
    }

    public static Passive createNewPassiveWithCost(String id, String aptitudeName, String texture, String attributeId, String attributeUUID, Object attributeValue, int spCost, int... levelsRequired) {
        return createNewPassive(id, aptitudeName, texture, attributeId, attributeUUID, attributeValue, spCost, levelsRequired);
    }

    public static Passive get(String nameOrId) {
        return RegistryPassives.getPassive(nameOrId);
    }

    private static Passive createWithId(ResourceLocation id, String aptitudeName, String texture, String attributeId, String attributeUUID, Object attributeValue, int... levelsRequired) {
        if (attributeId == null || attributeId.isBlank()) {
            throw new IllegalArgumentException("Attribute id cannot be empty for passive: " + id);
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

        return Passive.addWithId(id, aptitudeName, normalizeTexture(texture), attribute, attributeUUID, attributeValue, levelsRequired);
    }

    private static ResourceLocation parseResourceLocation(String raw, boolean forceDefaultNamespace) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.toLowerCase(Locale.ROOT);
        try {
            if (!forceDefaultNamespace && normalized.contains(":")) {
                return new ResourceLocation(normalized);
            }
            String path = normalized.contains(":")
                    ? normalized.substring(normalized.indexOf(':') + 1)
                    : normalized;
            return new ResourceLocation(JustLevelingFork.MOD_ID, path);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static String normalizeTexture(String texture) {
        if (texture == null || texture.isBlank()) {
            throw new IllegalArgumentException("Texture location cannot be empty");
        }
        return texture.toLowerCase(Locale.ROOT);
    }
}
