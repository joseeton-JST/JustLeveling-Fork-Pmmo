package com.joseetoon.justlevellingaddonjs.mixin;

import com.joseetoon.justlevellingaddonjs.compat.AptitudeCompat;
import com.seniors.justlevelingfork.client.screen.JustLevelingScreen;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Locale;

@Mixin(value = JustLevelingScreen.class, remap = false)
public abstract class MixJLScreenAptitudeNameFallback {
    @Shadow
    public String selectedAptitude;

    @Redirect(
            method = "drawSkills",
            at = @At(value = "INVOKE", target = "Lcom/seniors/justlevelingfork/registry/aptitude/Aptitude;getKey()Ljava/lang/String;"),
            require = 0
    )
    private String jlforkaddon$resolveAptitudeDisplayName(Aptitude aptitude) {
        String key = aptitude != null ? aptitude.getKey() : "";
        return jlforkaddon$resolveDisplayName(key, aptitude);
    }

    @Redirect(
            method = "drawSkills",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;m_237115_(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"),
            require = 0
    )
    private MutableComponent jlforkaddon$resolveAptitudeHeaderTranslation(String key) {
        if (key == null || key.isBlank()) {
            return Component.empty();
        }
        if (key != null && key.startsWith("aptitude.")) {
            return Component.literal(jlforkaddon$resolveDisplayName(key, null));
        }
        return Component.translatable(key);
    }

    @Unique
    private String jlforkaddon$resolveDisplayName(String key, Aptitude direct) {
        Aptitude aptitude = direct;
        if (aptitude == null && this.selectedAptitude != null && !this.selectedAptitude.isBlank()) {
            aptitude = RegistryAptitudes.getAptitude(this.selectedAptitude);
        }

        if (aptitude != null) {
            String displayName = AptitudeCompat.getDisplayNameOrFallback(aptitude);
            if (displayName != null && !displayName.isBlank() && !displayName.equals(key)) {
                return displayName;
            }
            String rawName = AptitudeCompat.getName(aptitude);
            if (rawName != null && !rawName.isBlank()) {
                return jlforkaddon$prettifyTranslationKey(rawName);
            }
        }

        if (key != null && !key.isBlank()) {
            return jlforkaddon$prettifyTranslationKey(key);
        }

        return "Unknown";
    }

    @Unique
    private static String jlforkaddon$prettifyTranslationKey(String key) {
        if (key == null || key.isBlank()) {
            return "Unknown";
        }

        String path = key;
        int namespace = path.lastIndexOf(':');
        if (namespace >= 0 && namespace + 1 < path.length()) {
            path = path.substring(namespace + 1);
        }
        int lastDot = path.lastIndexOf('.');
        if (lastDot >= 0 && lastDot + 1 < path.length()) {
            path = path.substring(lastDot + 1);
        }

        String[] parts = path.split("[_\\-\\.\\s]+");
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

        return builder.isEmpty() ? path : builder.toString();
    }
}
