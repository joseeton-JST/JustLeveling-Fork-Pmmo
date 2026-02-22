package com.joseetoon.justlevellingaddonjs.kubejs.compat;

import com.seniors.justlevelingfork.JustLevelingFork;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public final class AddonResourceUtils {
    private AddonResourceUtils() {}

    public static ResourceLocation parseResourceLocation(String raw, boolean forceDefaultNamespace) {
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

    public static String normalizeTexture(String texture) {
        if (texture == null || texture.isBlank()) {
            throw new IllegalArgumentException("Texture location cannot be empty");
        }
        return texture.toLowerCase(Locale.ROOT);
    }
}
