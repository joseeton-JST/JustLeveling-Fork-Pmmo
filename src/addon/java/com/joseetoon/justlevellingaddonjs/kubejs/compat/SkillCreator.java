package com.joseetoon.justlevellingaddonjs.kubejs.compat;

import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public class SkillCreator {
    public static LegacySkill createSkill(String name, String backgroundLocation) {
        ResourceLocation id = AddonResourceUtils.parseResourceLocation(name, true);
        if (id == null) {
            throw new IllegalArgumentException("Invalid skill name: " + name);
        }
        return createWithId(id, backgroundLocation);
    }

    public static LegacySkill createNewSkill(String nameLocation, String backgroundLocation) {
        ResourceLocation id = AddonResourceUtils.parseResourceLocation(nameLocation, false);
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
        String background = normalizeTextureWithNamespace(backgroundLocation, "minecraft");
        String icon = id.getNamespace() + ":textures/skills/" + id.getPath() + ".png";

        Aptitude aptitude = AptitudeAPI.addWithId(id.toString(), background, icon, icon, icon, icon);
        return new LegacySkill(aptitude);
    }

    private static String normalizeTextureWithNamespace(String texture, String defaultNamespace) {
        if (texture == null || texture.isBlank()) {
            throw new IllegalArgumentException("Texture/background location cannot be empty");
        }
        String normalized = texture.toLowerCase(Locale.ROOT);
        return normalized.contains(":") ? normalized : defaultNamespace + ":" + normalized;
    }
}
