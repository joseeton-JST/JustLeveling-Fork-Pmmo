package com.joseetoon.justlevellingaddonjs.kubejs.compat;

import com.seniors.justlevelingfork.registry.RegistryPassives;
import com.seniors.justlevelingfork.registry.passive.Passive;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

public class PassiveCreator {
    public static PassiveDraft createPassive(String name) {
        ResourceLocation id = AddonResourceUtils.parseResourceLocation(name, true);
        if (id == null) {
            throw new IllegalArgumentException("Invalid passive name: " + name);
        }
        return new PassiveDraft(id);
    }

    public static PassiveDraft createNewPassive(String id) {
        ResourceLocation resourceLocation = AddonResourceUtils.parseResourceLocation(id, false);
        if (resourceLocation == null) {
            throw new IllegalArgumentException("Invalid passive id: " + id);
        }
        return new PassiveDraft(resourceLocation);
    }

    public static Passive createPassive(String name, String aptitudeName, String texture, String attributeId, Object attributeValue, int... levelsRequired) {
        ResourceLocation id = AddonResourceUtils.parseResourceLocation(name, true);
        if (id == null) {
            throw new IllegalArgumentException("Invalid passive name: " + name);
        }
        return createWithId(id, aptitudeName, texture, attributeId, null, attributeValue, levelsRequired);
    }

    public static Passive createPassiveWithCost(String name, String aptitudeName, String texture, String attributeId, Number attributeValue, int spCost, int... levelsRequired) {
        Passive passive = createPassive(name, aptitudeName, texture, attributeId, attributeValue, levelsRequired);
        PassiveCompat.setPointCost(passive, spCost);
        return passive;
    }

    public static Passive createPassive(String name, String aptitudeName, String texture, String attributeId, String attributeUUID, Object attributeValue, int... levelsRequired) {
        ResourceLocation id = AddonResourceUtils.parseResourceLocation(name, true);
        if (id == null) {
            throw new IllegalArgumentException("Invalid passive name: " + name);
        }
        return createWithId(id, aptitudeName, texture, attributeId, attributeUUID, attributeValue, levelsRequired);
    }

    public static Passive createPassiveWithCost(String name, String aptitudeName, String texture, String attributeId, String attributeUUID, Object attributeValue, int spCost, int... levelsRequired) {
        Passive passive = createPassive(name, aptitudeName, texture, attributeId, attributeUUID, attributeValue, levelsRequired);
        PassiveCompat.setPointCost(passive, spCost);
        return passive;
    }

    public static Passive createNewPassive(String id, String aptitudeName, String texture, String attributeId, Object attributeValue, int... levelsRequired) {
        ResourceLocation resourceLocation = AddonResourceUtils.parseResourceLocation(id, false);
        if (resourceLocation == null) {
            throw new IllegalArgumentException("Invalid passive id: " + id);
        }
        return createWithId(resourceLocation, aptitudeName, texture, attributeId, null, attributeValue, levelsRequired);
    }

    public static Passive createNewPassiveWithCost(String id, String aptitudeName, String texture, String attributeId, Number attributeValue, int spCost, int... levelsRequired) {
        Passive passive = createNewPassive(id, aptitudeName, texture, attributeId, attributeValue, levelsRequired);
        PassiveCompat.setPointCost(passive, spCost);
        return passive;
    }

    public static Passive createNewPassive(String id, String aptitudeName, String texture, String attributeId, String attributeUUID, Object attributeValue, int... levelsRequired) {
        ResourceLocation resourceLocation = AddonResourceUtils.parseResourceLocation(id, false);
        if (resourceLocation == null) {
            throw new IllegalArgumentException("Invalid passive id: " + id);
        }
        return createWithId(resourceLocation, aptitudeName, texture, attributeId, attributeUUID, attributeValue, levelsRequired);
    }

    public static Passive createNewPassiveWithCost(String id, String aptitudeName, String texture, String attributeId, String attributeUUID, Object attributeValue, int spCost, int... levelsRequired) {
        Passive passive = createNewPassive(id, aptitudeName, texture, attributeId, attributeUUID, attributeValue, levelsRequired);
        PassiveCompat.setPointCost(passive, spCost);
        return passive;
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

        String resolvedUuid = resolveAttributeUuid(attributeUUID, id, attributeLocation);
        return PassiveCompat.addWithId(id, aptitudeName, AddonResourceUtils.normalizeTexture(texture), attribute, resolvedUuid, attributeValue, levelsRequired);
    }

    private static String resolveAttributeUuid(String attributeUUID, ResourceLocation passiveId, ResourceLocation attributeLocation) {
        if (attributeUUID == null || attributeUUID.isBlank()) {
            return generateDeterministicUuid(passiveId, attributeLocation);
        }

        String normalized = attributeUUID.trim().toLowerCase(Locale.ROOT);
        try {
            UUID.fromString(normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid attribute UUID: " + attributeUUID, exception);
        }
        return normalized;
    }

    private static String generateDeterministicUuid(ResourceLocation passiveId, ResourceLocation attributeLocation) {
        String seed = "jlforkaddon-passive|" + passiveId + "|" + attributeLocation;
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
