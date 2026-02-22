package com.joseetoon.justlevellingaddonjs.mixin;

import com.seniors.justlevelingfork.registry.skills.Skill;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Locale;

@Mixin(value = Skill.class, remap = false)
public abstract class MixSkillBackportApi {
    @Unique
    private int jlforkaddon$pointCost = 1;

    public Component getDisplayNameComponentOrFallback() {
        Skill self = (Skill) (Object) this;
        String translationKey = self.getKey();
        MutableComponent translated = Component.translatable(translationKey);
        if (translationKey.equals(translated.getString())) {
            return Component.literal(jlforkaddon$buildFallbackName(self.getName()));
        }
        return translated;
    }

    public Component getDescriptionComponentOrFallback() {
        Skill self = (Skill) (Object) this;
        String translationKey = self.getDescription();
        MutableComponent translated = self.getMutableDescription(translationKey);
        if (translationKey.equals(translated.getString())) {
            return Component.literal(getDisplayNameComponentOrFallback().getString());
        }
        return translated;
    }

    public int getPointCost() {
        return Math.max(1, this.jlforkaddon$pointCost);
    }

    public void setPointCost(int cost) {
        this.jlforkaddon$pointCost = Math.max(1, cost);
    }

    public int getSpCost() {
        return getPointCost();
    }

    public void setSpCost(int cost) {
        setPointCost(cost);
    }

    @Unique
    private static String jlforkaddon$buildFallbackName(String id) {
        if (id == null || id.isBlank()) {
            return "Unknown Skill";
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
