package com.joseetoon.justlevellingaddonjs.mixin;

import com.joseetoon.justlevellingaddonjs.compat.base121.Base121Bridge;
import com.joseetoon.justlevellingaddonjs.compat.base121.CompatReflection;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Mixin(value = Aptitude.class, remap = false)
public abstract class MixAptitudeBackportApi {
    @Unique
    private int jlforkaddon$levelCap = -1;
    @Unique
    private int jlforkaddon$baseLevelCost = -1;
    @Unique
    private boolean jlforkaddon$enabled = true;
    @Unique
    private boolean jlforkaddon$hidden = false;
    @Unique
    private String jlforkaddon$displayNameOverride = null;
    @Unique
    private int jlforkaddon$skillPointInterval = 2;
    @Unique
    private int jlforkaddon$backgroundRepeat = 0;
    @Unique
    private ResourceLocation jlforkaddon$backgroundOverride = null;
    @Unique
    private ResourceLocation[] jlforkaddon$lockedTextureOverride = null;
    @Unique
    private final Map<Integer, Integer> jlforkaddon$levelStaggering = new LinkedHashMap<>();
    @Unique
    private final Map<Integer, ResourceLocation> jlforkaddon$rankIcons = new LinkedHashMap<>();

    public String getDisplayNameOrFallback() {
        if (this.jlforkaddon$displayNameOverride != null && !this.jlforkaddon$displayNameOverride.isBlank()) {
            return this.jlforkaddon$displayNameOverride;
        }

        Aptitude self = (Aptitude) (Object) this;
        String translationKey = self.getKey();
        String translated = Component.translatable(translationKey).getString();
        return translated.equals(translationKey) ? jlforkaddon$buildFallbackName(self.getName()) : translated;
    }

    public String getAbbreviationOrFallback() {
        if (this.jlforkaddon$displayNameOverride != null && !this.jlforkaddon$displayNameOverride.isBlank()) {
            return jlforkaddon$buildFallbackAbbreviation(this.jlforkaddon$displayNameOverride);
        }

        Aptitude self = (Aptitude) (Object) this;
        String translationKey = self.getKey() + ".abbreviation";
        String translated = Component.translatable(translationKey).getString();
        return translated.equals(translationKey) ? jlforkaddon$buildFallbackAbbreviation(self.getName()) : translated;
    }

    public int getLevelCap() {
        return this.jlforkaddon$levelCap > 0 ? this.jlforkaddon$levelCap : Base121Bridge.aptitudeDefaultLevelCap();
    }

    public void setLevelCap(int cap) {
        this.jlforkaddon$levelCap = cap;
    }

    public int getBaseLevelCost() {
        return this.jlforkaddon$baseLevelCost > 0 ? this.jlforkaddon$baseLevelCost : Base121Bridge.aptitudeDefaultBaseCost();
    }

    public void setBaseLevelCost(int cost) {
        this.jlforkaddon$baseLevelCost = cost;
        if (!this.jlforkaddon$levelStaggering.isEmpty()) {
            setLevelStaggering(getLevelStaggering());
        }
    }

    public String getDisplayNameOverride() {
        return this.jlforkaddon$displayNameOverride;
    }

    public void setDisplayNameOverride(String displayNameOverride) {
        this.jlforkaddon$displayNameOverride = (displayNameOverride == null || displayNameOverride.isBlank()) ? null : displayNameOverride;
    }

    public int getSkillPointInterval() {
        return Math.max(1, this.jlforkaddon$skillPointInterval);
    }

    public void setSkillPointInterval(int interval) {
        this.jlforkaddon$skillPointInterval = Math.max(1, interval);
    }

    public String[] getLevelStaggering() {
        return this.jlforkaddon$levelStaggering.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .map(entry -> entry.getKey() + "|" + entry.getValue())
                .toArray(String[]::new);
    }

    public void setLevelStaggering(String... stagger) {
        Map<Integer, Integer> configLevelStaggering = new LinkedHashMap<>();
        if (stagger != null) {
            for (String value : stagger) {
                if (value == null || value.isBlank()) {
                    continue;
                }
                String[] split = value.split("\\|");
                if (split.length != 2) {
                    continue;
                }
                try {
                    int level = Integer.parseInt(split[0].trim());
                    int delta = Integer.parseInt(split[1].trim());
                    if (level > 0) {
                        configLevelStaggering.put(level, delta);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        this.jlforkaddon$levelStaggering.clear();
        if (configLevelStaggering.isEmpty()) {
            return;
        }

        int lastLevel = getBaseLevelCost();
        for (int i = 1; i < getLevelCap(); i++) {
            if (configLevelStaggering.containsKey(i)) {
                lastLevel = configLevelStaggering.get(i);
            }
            this.jlforkaddon$levelStaggering.put(i, lastLevel);
        }
    }

    public int getLevelUpExperienceLevels(int aptitudeLevel) {
        if (!this.jlforkaddon$levelStaggering.isEmpty()) {
            int base = getBaseLevelCost();
            int cumulative = 0;
            for (int i = 1; i <= aptitudeLevel; i++) {
                cumulative += this.jlforkaddon$levelStaggering.getOrDefault(i, 0);
            }
            return Math.max(0, base + cumulative);
        }
        return aptitudeLevel + getBaseLevelCost() - 1;
    }

    public int getLevelUpPointCost(int aptitudeLevel) {
        return jlforkaddon$getExperienceForLevel(getLevelUpExperienceLevels(aptitudeLevel));
    }

    public boolean isEnabled() {
        return this.jlforkaddon$enabled;
    }

    public void setEnabled(boolean enabled) {
        this.jlforkaddon$enabled = enabled;
    }

    public boolean isHidden() {
        return this.jlforkaddon$hidden;
    }

    public void setHidden(boolean hidden) {
        this.jlforkaddon$hidden = hidden;
    }

    public void setBackground(ResourceLocation bg) {
        this.jlforkaddon$backgroundOverride = bg;
        Aptitude self = (Aptitude) (Object) this;
        CompatReflection.setFieldValue(self, "background", bg);
    }

    public int getBackgroundRepeat() {
        return this.jlforkaddon$backgroundRepeat;
    }

    public void setBackgroundRepeat(int repeat) {
        this.jlforkaddon$backgroundRepeat = Mth.clamp(repeat, 0, 64);
    }

    public void setLockedTextures(String... textures) {
        if (textures == null || textures.length == 0) {
            return;
        }

        this.jlforkaddon$lockedTextureOverride = jlforkaddon$buildTextureArray(textures);
        Aptitude self = (Aptitude) (Object) this;
        CompatReflection.setFieldValue(self, "lockedTexture", this.jlforkaddon$lockedTextureOverride);
    }

    public void setRankIcon(int rank, String resourceLocation) {
        if (resourceLocation == null || resourceLocation.isBlank()) {
            return;
        }
        try {
            setRankIcon(rank, new ResourceLocation(resourceLocation.toLowerCase(Locale.ROOT)));
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void setRankIcon(int rank, ResourceLocation resourceLocation) {
        if (rank < 0 || rank > 8 || resourceLocation == null) {
            return;
        }
        this.jlforkaddon$rankIcons.put(rank, resourceLocation);
    }

    public void removeRankIcon(int rank) {
        this.jlforkaddon$rankIcons.remove(rank);
    }

    public ResourceLocation getIconForLevel(int level) {
        int rank = Math.max(0, Math.min(8, Math.floorDiv(8 * Math.max(0, level), Math.max(1, getLevelCap()))));
        for (int i = rank; i >= 0; i--) {
            ResourceLocation icon = this.jlforkaddon$rankIcons.get(i);
            if (icon != null) {
                return icon;
            }
        }
        return jlforkaddon$getDefaultLockedTexture(level);
    }

    @Inject(method = "getLockedTexture(I)Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"), cancellable = true, require = 0)
    private void jlforkaddon$getLockedTexture(int fromLevel, CallbackInfoReturnable<ResourceLocation> cir) {
        cir.setReturnValue(getIconForLevel(fromLevel));
    }

    @Inject(method = "getLockedTexture()Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"), cancellable = true, require = 0)
    private void jlforkaddon$getLockedTextureCurrent(CallbackInfoReturnable<ResourceLocation> cir) {
        Aptitude self = (Aptitude) (Object) this;
        cir.setReturnValue(getIconForLevel(self.getLevel()));
    }

    @Unique
    private ResourceLocation jlforkaddon$getDefaultLockedTexture(int level) {
        Aptitude self = (Aptitude) (Object) this;
        ResourceLocation[] textures = this.jlforkaddon$lockedTextureOverride;
        if (textures == null || textures.length == 0) {
            Object fromField = CompatReflection.getFieldValue(self, "lockedTexture");
            if (fromField instanceof ResourceLocation[] resourceLocations && resourceLocations.length > 0) {
                textures = resourceLocations;
            }
        }
        if (textures == null || textures.length == 0) {
            return this.jlforkaddon$backgroundOverride != null ? this.jlforkaddon$backgroundOverride : new ResourceLocation("minecraft:textures/block/stone.png");
        }

        int size = getLevelCap();
        int textureListSize = textures.length;
        int index = Math.floorDiv((Math.max(0, level) * textureListSize), Math.max(1, size));
        index = index == textureListSize ? index - 1 : index;
        if (index >= textureListSize) {
            index = textureListSize - 1;
        }
        if (index < 0) {
            index = 0;
        }
        return textures[index];
    }

    @Unique
    private static ResourceLocation[] jlforkaddon$buildTextureArray(String... textures) {
        ResourceLocation[] result = new ResourceLocation[4];
        for (int i = 0; i < 4; i++) {
            int idx = Math.min(i, textures.length - 1);
            result[i] = new ResourceLocation(textures[idx].toLowerCase(Locale.ROOT));
        }
        return result;
    }

    @Unique
    private static int jlforkaddon$getExperienceForLevel(int level) {
        if (level <= 0) return 0;
        if (level <= 15) return jlforkaddon$sum(level, 7, 2);
        if (level <= 30) return 315 + jlforkaddon$sum(level - 15, 37, 5);
        return 1395 + jlforkaddon$sum(level - 30, 112, 9);
    }

    @Unique
    private static int jlforkaddon$sum(int n, int a0, int d) {
        return n * (2 * a0 + (n - 1) * d) / 2;
    }

    @Unique
    private static String jlforkaddon$buildFallbackName(String aptitudeId) {
        if (aptitudeId == null || aptitudeId.isBlank()) {
            return "Unknown";
        }

        String normalizedId = aptitudeId.trim();
        int namespaceSep = normalizedId.lastIndexOf(':');
        if (namespaceSep >= 0 && namespaceSep + 1 < normalizedId.length()) {
            normalizedId = normalizedId.substring(namespaceSep + 1);
        }
        int dotSep = normalizedId.lastIndexOf('.');
        if (dotSep >= 0 && dotSep + 1 < normalizedId.length()) {
            normalizedId = normalizedId.substring(dotSep + 1);
        }

        String[] parts = normalizedId.split("[_\\-\\.\\s]+");
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
        return builder.isEmpty() ? normalizedId : builder.toString();
    }

    @Unique
    private static String jlforkaddon$buildFallbackAbbreviation(String aptitudeId) {
        if (aptitudeId == null || aptitudeId.isBlank()) {
            return "???";
        }

        String normalizedId = aptitudeId.trim();
        int namespaceSep = normalizedId.lastIndexOf(':');
        if (namespaceSep >= 0 && namespaceSep + 1 < normalizedId.length()) {
            normalizedId = normalizedId.substring(namespaceSep + 1);
        }
        int dotSep = normalizedId.lastIndexOf('.');
        if (dotSep >= 0 && dotSep + 1 < normalizedId.length()) {
            normalizedId = normalizedId.substring(dotSep + 1);
        }

        String[] parts = normalizedId.split("[^a-zA-Z0-9]+");
        StringBuilder abbreviation = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }
            abbreviation.append(Character.toUpperCase(part.charAt(0)));
            if (abbreviation.length() >= 3) {
                break;
            }
        }

        if (abbreviation.isEmpty()) {
            String clean = normalizedId.replaceAll("[^a-zA-Z0-9]", "");
            if (clean.isEmpty()) {
                return "???";
            }
            return clean.substring(0, Math.min(3, clean.length())).toUpperCase(Locale.ROOT);
        }

        return abbreviation.toString();
    }
}
