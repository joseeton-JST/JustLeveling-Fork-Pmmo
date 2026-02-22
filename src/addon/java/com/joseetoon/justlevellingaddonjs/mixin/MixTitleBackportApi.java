package com.joseetoon.justlevellingaddonjs.mixin;

import com.joseetoon.justlevellingaddonjs.compat.base121.Base121Bridge;
import com.seniors.justlevelingfork.registry.title.Title;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Locale;

@Mixin(value = Title.class, remap = false)
public abstract class MixTitleBackportApi {
    public Component getDisplayNameComponentOrFallback() {
        Title self = (Title) (Object) this;
        String translationKey = self.getKey();
        MutableComponent translated = Component.translatable(translationKey);
        if (translationKey.equals(translated.getString())) {
            return Component.literal(jlforkaddon$buildFallbackName(self.getName()));
        }
        return translated;
    }

    public String getDisplayNameOrFallback() {
        return getDisplayNameComponentOrFallback().getString();
    }

    public Component getDescriptionComponentOrFallback() {
        Title self = (Title) (Object) this;
        String translationKey = self.getDescription();
        MutableComponent translated = Component.translatable(translationKey);
        if (translationKey.equals(translated.getString())) {
            return Component.literal(getDisplayNameOrFallback());
        }
        return translated;
    }

    @Unique
    private static String jlforkaddon$buildFallbackName(String id) {
        if (id == null || id.isBlank()) {
            return "Unknown Title";
        }

        String[] parts = id.split("[_\\-\\s]+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }

            if (builder.length() > 0) {
                builder.append(' ');
            }

            String normalized = part.toLowerCase(Locale.ROOT);
            builder.append(Character.toUpperCase(normalized.charAt(0)));
            if (normalized.length() > 1) {
                builder.append(normalized.substring(1));
            }
        }

        return builder.isEmpty() ? id : builder.toString();
    }
}
