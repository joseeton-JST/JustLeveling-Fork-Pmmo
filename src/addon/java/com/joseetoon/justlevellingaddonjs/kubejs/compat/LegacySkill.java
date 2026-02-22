package com.joseetoon.justlevellingaddonjs.kubejs.compat;

import com.joseetoon.justlevellingaddonjs.compat.AptitudeCompat;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public class LegacySkill {
    private final Aptitude aptitude;

    public LegacySkill(Aptitude aptitude) {
        if (aptitude == null) {
            throw new IllegalArgumentException("Aptitude cannot be null");
        }
        this.aptitude = aptitude;
    }

    public Aptitude asAptitude() {
        return aptitude;
    }

    public String getName() {
        return AptitudeCompat.getDisplayNameOrFallback(aptitude);
    }

    public void setName(String name) {
        AptitudeCompat.setDisplayNameOverride(aptitude, name);
    }

    public int getLevelCap() {
        return AptitudeCompat.getLevelCap(aptitude);
    }

    public void setLevelCap(int cap) {
        AptitudeCompat.setLevelCap(aptitude, cap);
    }

    public int getCap() {
        return AptitudeCompat.getLevelCap(aptitude);
    }

    public void setCap(int cap) {
        AptitudeCompat.setLevelCap(aptitude, cap);
    }

    public boolean getEnabled() {
        return AptitudeCompat.isEnabled(aptitude);
    }

    public void setEnabled(boolean enabled) {
        AptitudeCompat.setEnabled(aptitude, enabled);
    }

    public int getBaseLevelCost() {
        return AptitudeCompat.getBaseLevelCost(aptitude);
    }

    public void setBaseLevelCost(int cost) {
        AptitudeCompat.setBaseLevelCost(aptitude, cost);
    }

    public String[] getLevelStaggering() {
        return AptitudeCompat.getLevelStaggering(aptitude);
    }

    public void setLevelStaggering(String[] stagger) {
        AptitudeCompat.setLevelStaggering(aptitude, stagger);
    }

    public int getSkillPointInterval() {
        return AptitudeCompat.getSkillPointInterval(aptitude);
    }

    public void setSkillPointInterval(int interval) {
        AptitudeCompat.setSkillPointInterval(aptitude, interval);
    }

    public boolean isHidden() {
        return AptitudeCompat.isHidden(aptitude);
    }

    public void setHidden(boolean hidden) {
        AptitudeCompat.setHidden(aptitude, hidden);
    }

    public int getBackgroundRepeat() {
        return AptitudeCompat.getBackgroundRepeat(aptitude);
    }

    public void setBackgroundRepeat(int repeat) {
        AptitudeCompat.setBackgroundRepeat(aptitude, repeat);
    }

    public void setBackground(String backgroundTexture) {
        if (backgroundTexture == null || backgroundTexture.isBlank()) {
            throw new IllegalArgumentException("Background texture must be a non-empty resource location string (namespace:path)");
        }

        try {
            AptitudeCompat.setBackground(aptitude, new ResourceLocation(backgroundTexture.toLowerCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid background texture resource location: " + backgroundTexture, exception);
        }
    }

    public void setLockedTextures(String... textures) {
        AptitudeCompat.setLockedTextures(aptitude, textures);
    }

    public void setRankIcon(int rank, String resourceLocation) {
        AptitudeCompat.setRankIcon(aptitude, rank, resourceLocation);
    }

    public void removeRankIcon(int rank) {
        AptitudeCompat.removeRankIcon(aptitude, rank);
    }
}

