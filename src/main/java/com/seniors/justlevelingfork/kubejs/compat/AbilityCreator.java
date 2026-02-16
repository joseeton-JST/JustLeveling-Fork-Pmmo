package com.seniors.justlevelingfork.kubejs.compat;

import com.seniors.justlevelingfork.JustLevelingFork;
import com.seniors.justlevelingfork.client.core.Value;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import com.seniors.justlevelingfork.registry.skills.Skill;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public class AbilityCreator {
    public static Skill createAbility(String name, String aptitudeName, int requiredLevel, String texture, Value... values) {
        ResourceLocation id = parseResourceLocation(name, true);
        if (id == null) {
            throw new IllegalArgumentException("Invalid ability name: " + name);
        }
        return Skill.addWithId(id, aptitudeName, requiredLevel, normalizeTexture(texture), values);
    }

    public static Skill createAbility(String name, String aptitudeName, int requiredLevel, String texture, int spCost, Value... values) {
        Skill skill = createAbility(name, aptitudeName, requiredLevel, texture, values);
        skill.setPointCost(spCost);
        return skill;
    }

    public static Skill createNewAbility(String id, String aptitudeName, int requiredLevel, String texture, Value... values) {
        ResourceLocation resourceLocation = parseResourceLocation(id, false);
        if (resourceLocation == null) {
            throw new IllegalArgumentException("Invalid ability id: " + id);
        }
        return Skill.addWithId(resourceLocation, aptitudeName, requiredLevel, normalizeTexture(texture), values);
    }

    public static Skill createNewAbility(String id, String aptitudeName, int requiredLevel, String texture, int spCost, Value... values) {
        Skill skill = createNewAbility(id, aptitudeName, requiredLevel, texture, values);
        skill.setPointCost(spCost);
        return skill;
    }

    public static Skill get(String nameOrId) {
        return RegistrySkills.getSkill(nameOrId);
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
