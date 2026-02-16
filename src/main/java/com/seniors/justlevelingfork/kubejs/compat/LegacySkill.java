package com.seniors.justlevelingfork.kubejs.compat;

import com.seniors.justlevelingfork.registry.aptitude.Aptitude;

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
        return aptitude.getDisplayNameOrFallback();
    }

    public void setName(String name) {
        aptitude.setDisplayNameOverride(name);
    }

    public int getLevelCap() {
        return aptitude.getLevelCap();
    }

    public void setLevelCap(int cap) {
        aptitude.setLevelCap(cap);
    }

    public int getCap() {
        return aptitude.getLevelCap();
    }

    public void setCap(int cap) {
        aptitude.setLevelCap(cap);
    }

    public boolean getEnabled() {
        return aptitude.isEnabled();
    }

    public void setEnabled(boolean enabled) {
        aptitude.setEnabled(enabled);
    }

    public int getBaseLevelCost() {
        return aptitude.getBaseLevelCost();
    }

    public void setBaseLevelCost(int cost) {
        aptitude.setBaseLevelCost(cost);
    }

    public String[] getLevelStaggering() {
        return aptitude.getLevelStaggering();
    }

    public void setLevelStaggering(String[] stagger) {
        aptitude.setLevelStaggering(stagger);
    }

    public int getSkillPointInterval() {
        return aptitude.getSkillPointInterval();
    }

    public void setSkillPointInterval(int interval) {
        aptitude.setSkillPointInterval(interval);
    }

    public boolean isHidden() {
        return aptitude.isHidden();
    }

    public void setHidden(boolean hidden) {
        aptitude.setHidden(hidden);
    }

    public int getBackgroundRepeat() {
        return aptitude.getBackgroundRepeat();
    }

    public void setBackgroundRepeat(int repeat) {
        aptitude.setBackgroundRepeat(repeat);
    }

    public void setRankIcon(int rank, String resourceLocation) {
        aptitude.setRankIcon(rank, resourceLocation);
    }

    public void removeRankIcon(int rank) {
        aptitude.removeRankIcon(rank);
    }
}
