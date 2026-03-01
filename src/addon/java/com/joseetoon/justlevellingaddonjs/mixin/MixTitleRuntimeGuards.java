package com.joseetoon.justlevellingaddonjs.mixin;

import com.joseetoon.justlevellingaddonjs.compat.base121.BackportRegistryState;
import com.seniors.justlevelingfork.registry.title.Title;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

@Mixin(value = Title.class, remap = false)
public abstract class MixTitleRuntimeGuards {
    @Unique
    private static final String JLFORKADDON_NONE_LABEL = "(none)";

    @Shadow
    public abstract String getName();

    @Shadow
    public abstract String getKey();

    @Shadow
    public abstract String getDescription();

    @Inject(method = "setRequirement", at = @At("HEAD"), cancellable = true, require = 0)
    private void jlforkaddon$skipDeletedTitleRequirement(ServerPlayer serverPlayer, boolean check, CallbackInfo ci) {
        if (BackportRegistryState.isTitleDeleted(this.getName())
                || BackportRegistryState.isTitleDisabledByAddonConfig(this.getName())
                || BackportRegistryState.isTitleServerManaged(this.getName())) {
            ci.cancel();
        }
    }

    @Inject(method = "getDisplayNameComponentOrFallback", at = @At("HEAD"), cancellable = true, require = 1)
    private void jlforkaddon$displayNameFallback(CallbackInfoReturnable<Component> cir) {
        if (jlforkaddon$isTitleless(this.getName())) {
            cir.setReturnValue(Component.literal(JLFORKADDON_NONE_LABEL));
            return;
        }

        BackportRegistryState.TitleTextOverride textOverride = BackportRegistryState.getTitleTextOverride(this.getName());
        if (textOverride != null && textOverride.displayNameOverride() != null) {
            cir.setReturnValue(Component.literal(textOverride.displayNameOverride()));
            return;
        }

        String translationKey = this.getKey();
        MutableComponent translated = Component.translatable(translationKey);
        String resolved = translated.getString();
        if (jlforkaddon$isMissingTranslation(translationKey, resolved)) {
            cir.setReturnValue(Component.literal(jlforkaddon$buildFallbackName(this.getName())));
        }
    }

    @Inject(method = "getDisplayNameOrFallback", at = @At("HEAD"), cancellable = true, require = 1)
    private void jlforkaddon$displayNameStringFallback(CallbackInfoReturnable<String> cir) {
        if (jlforkaddon$isTitleless(this.getName())) {
            cir.setReturnValue(JLFORKADDON_NONE_LABEL);
            return;
        }

        BackportRegistryState.TitleTextOverride textOverride = BackportRegistryState.getTitleTextOverride(this.getName());
        if (textOverride != null && textOverride.displayNameOverride() != null) {
            cir.setReturnValue(textOverride.displayNameOverride());
            return;
        }

        String translationKey = this.getKey();
        String resolved = Component.translatable(translationKey).getString();
        if (jlforkaddon$isMissingTranslation(translationKey, resolved)) {
            cir.setReturnValue(jlforkaddon$buildFallbackName(this.getName()));
        }
    }

    @Inject(method = "getDescriptionComponentOrFallback", at = @At("HEAD"), cancellable = true, require = 1)
    private void jlforkaddon$descriptionFallback(CallbackInfoReturnable<Component> cir) {
        BackportRegistryState.TitleTextOverride textOverride = BackportRegistryState.getTitleTextOverride(this.getName());
        if (textOverride != null && textOverride.descriptionOverride() != null) {
            cir.setReturnValue(Component.literal(textOverride.descriptionOverride()));
            return;
        }

        String translationKey = this.getDescription();
        MutableComponent translated = Component.translatable(translationKey);
        String resolved = translated.getString();
        if (jlforkaddon$isMissingTranslation(translationKey, resolved)) {
            if (textOverride != null && textOverride.displayNameOverride() != null) {
                cir.setReturnValue(Component.literal(textOverride.displayNameOverride()));
            } else {
                cir.setReturnValue(Component.literal(jlforkaddon$buildFallbackName(this.getName())));
            }
        }
    }

    @Unique
    private static boolean jlforkaddon$isMissingTranslation(String key, String resolved) {
        if (key == null || resolved == null) {
            return true;
        }
        String trimmed = resolved.trim();
        if (key.equals(trimmed)) {
            return true;
        }
        return ("<" + key + ">").equals(trimmed);
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

    @Unique
    private static boolean jlforkaddon$isTitleless(String titleName) {
        return "titleless".equalsIgnoreCase(titleName);
    }
}
