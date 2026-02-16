package com.seniors.justlevelingfork.kubejs.compat;

import com.seniors.justlevelingfork.JustLevelingFork;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public class SkillCreator {
    public static LegacySkill createSkill(String name, String backgroundLocation) {
        ResourceLocation id = parseResourceLocation(name, true);
        if (id == null) {
            throw new IllegalArgumentException("Invalid skill name: " + name);
        }
        return createWithId(id, backgroundLocation);
    }

    public static LegacySkill createNewSkill(String nameLocation, String backgroundLocation) {
        ResourceLocation id = parseResourceLocation(nameLocation, false);
        if (id == null) {
            throw new IllegalArgumentException("Invalid skill id: " + nameLocation);
        }
        return createWithId(id, backgroundLocation);
    }

    public static LegacySkill get(String nameOrId) {
        Aptitude aptitude = RegistryAptitudes.getAptitude(nameOrId);
        return aptitude == null ? null : new LegacySkill(aptitude);
    }

    private static LegacySkill createWithId(ResourceLocation id, String backgroundLocation) {
        String background = normalizeTexture(backgroundLocation, "minecraft");
        String icon = id.getNamespace() + ":textures/skills/" + id.getPath() + ".png";

        Aptitude aptitude = Aptitude.addWithId(id, background, icon, icon, icon, icon);
        return new LegacySkill(aptitude);
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

    private static String normalizeTexture(String texture, String defaultNamespace) {
        if (texture == null || texture.isBlank()) {
            throw new IllegalArgumentException("Texture/background location cannot be empty");
        }
        String normalized = texture.toLowerCase(Locale.ROOT);
        return normalized.contains(":") ? normalized : defaultNamespace + ":" + normalized;
    }
}

