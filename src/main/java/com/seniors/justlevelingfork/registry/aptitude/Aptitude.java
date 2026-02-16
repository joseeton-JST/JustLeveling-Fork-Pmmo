package com.seniors.justlevelingfork.registry.aptitude;

import com.seniors.justlevelingfork.JustLevelingFork;
import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import com.seniors.justlevelingfork.handler.HandlerCommonConfig;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.RegistryPassives;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import com.seniors.justlevelingfork.registry.passive.Passive;
import com.seniors.justlevelingfork.registry.skills.Skill;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Aptitude {
    public final int index;
    public final ResourceLocation key;
    public ResourceLocation[] lockedTexture;
    public ResourceLocation background;
    public List<Skill> list = new ArrayList<>();

    // KubeJS configurable fields
    private int levelCap = -1;
    private int baseLevelCost = -1;
    private boolean enabled = true;
    private boolean hidden = false;
    private String displayNameOverride = null;
    private int skillPointInterval = 2;
    private int backgroundRepeat = 0;
    private final Map<Integer, Integer> levelStaggering = new LinkedHashMap<>();
    private final Map<Integer, ResourceLocation> rankIcons = new LinkedHashMap<>();

    public Aptitude(int index, ResourceLocation key, ResourceLocation[] lockedTexture, ResourceLocation background) {
        this.index = index;
        this.key = key;
        this.lockedTexture = lockedTexture;
        this.background = background;
    }

    // KubeJS: Create a new custom aptitude
    public static Aptitude add(String name, String background, String... lockedTextures) {
        ResourceLocation id = parseResourceLocation(name, true);
        if (id == null) {
            throw new IllegalArgumentException("Invalid aptitude name: " + name);
        }
        return addWithId(id, background, lockedTextures);
    }

    public static Aptitude add(String name, String background, int backgroundRepeat, String... lockedTextures) {
        ResourceLocation id = parseResourceLocation(name, true);
        if (id == null) {
            throw new IllegalArgumentException("Invalid aptitude name: " + name);
        }
        return addWithId(id, background, backgroundRepeat, lockedTextures);
    }

    public static Aptitude addWithId(String nameOrId, String background, String... lockedTextures) {
        ResourceLocation id = parseResourceLocation(nameOrId, false);
        if (id == null) {
            throw new IllegalArgumentException("Invalid aptitude id: " + nameOrId);
        }
        return addWithId(id, background, lockedTextures);
    }

    public static Aptitude addWithId(String nameOrId, String background, int backgroundRepeat, String... lockedTextures) {
        ResourceLocation id = parseResourceLocation(nameOrId, false);
        if (id == null) {
            throw new IllegalArgumentException("Invalid aptitude id: " + nameOrId);
        }
        return addWithId(id, background, backgroundRepeat, lockedTextures);
    }

    public static Aptitude addWithId(ResourceLocation id, String background, String... lockedTextures) {
        return addWithId(id, background, 0, lockedTextures);
    }

    public static Aptitude addWithId(ResourceLocation id, String background, int backgroundRepeat, String... lockedTextures) {
        if (lockedTextures == null || lockedTextures.length == 0) {
            throw new IllegalArgumentException("At least one locked texture is required for aptitude: " + id);
        }
        ResourceLocation[] textures = buildTextureArray(lockedTextures);
        ResourceLocation bgLocation = new ResourceLocation(background);
        int idx = RegistryAptitudes.getNextIndex();
        Aptitude aptitude = new Aptitude(idx, id, textures, bgLocation);
        aptitude.setBackgroundRepeat(backgroundRepeat);
        RegistryAptitudes.addPendingAptitude(id, aptitude);
        return aptitude;
    }

    // KubeJS: Get an existing aptitude by name
    public static Aptitude getByName(String name) {
        return RegistryAptitudes.getAptitude(name.toLowerCase());
    }

    private static ResourceLocation[] buildTextureArray(String... textures) {
        ResourceLocation[] result = new ResourceLocation[4];
        for (int i = 0; i < 4; i++) {
            int idx = Math.min(i, textures.length - 1);
            result[i] = new ResourceLocation(textures[idx]);
        }
        return result;
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

    public Aptitude get() {
        return this;
    }

    public String getName() {
        return this.key.getPath();
    }

    public String getKey() {
        return "aptitude." + this.key.toLanguageKey();
    }

    public String getDescription() {
        return getKey() + ".description";
    }

    public String getDisplayNameOrFallback() {
        if (displayNameOverride != null && !displayNameOverride.isBlank()) {
            return displayNameOverride;
        }
        String translationKey = getKey();
        String translated = Component.translatable(translationKey).getString();
        return translated.equals(translationKey) ? buildFallbackName(getName()) : translated;
    }

    public String getAbbreviationOrFallback() {
        if (displayNameOverride != null && !displayNameOverride.isBlank()) {
            return buildFallbackAbbreviation(displayNameOverride);
        }
        String translationKey = getKey() + ".abbreviation";
        String translated = Component.translatable(translationKey).getString();
        return translated.equals(translationKey) ? buildFallbackAbbreviation(getName()) : translated;
    }

    // Per-aptitude level cap (-1 = use global)
    public int getLevelCap() {
        return levelCap > 0 ? levelCap : HandlerCommonConfig.HANDLER.instance().aptitudeMaxLevel;
    }

    public void setLevelCap(int cap) {
        this.levelCap = cap;
    }

    public int getBaseLevelCost() {
        return baseLevelCost > 0 ? baseLevelCost : HandlerCommonConfig.HANDLER.instance().aptitudeFirstCostLevel;
    }

    public void setBaseLevelCost(int cost) {
        this.baseLevelCost = cost;
        if (!levelStaggering.isEmpty()) {
            setLevelStaggering(getLevelStaggering());
        }
    }

    public String getDisplayNameOverride() {
        return displayNameOverride;
    }

    public void setDisplayNameOverride(String displayNameOverride) {
        this.displayNameOverride = (displayNameOverride == null || displayNameOverride.isBlank()) ? null : displayNameOverride;
    }

    public int getSkillPointInterval() {
        return Math.max(1, skillPointInterval);
    }

    public void setSkillPointInterval(int interval) {
        this.skillPointInterval = Math.max(1, interval);
    }

    public String[] getLevelStaggering() {
        return levelStaggering.entrySet().stream()
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

        this.levelStaggering.clear();
        if (configLevelStaggering.isEmpty()) {
            return;
        }

        int lastLevel = getBaseLevelCost();
        for (int i = 1; i < getLevelCap(); i++) {
            if (configLevelStaggering.containsKey(i)) {
                lastLevel = configLevelStaggering.get(i);
            }
            this.levelStaggering.put(i, lastLevel);
        }
    }

    public int getLevelUpExperienceLevels(int aptitudeLevel) {
        if (!levelStaggering.isEmpty()) {
            int base = getBaseLevelCost();
            int cumulative = 0;
            for (int i = 1; i <= aptitudeLevel; i++) {
                cumulative += levelStaggering.getOrDefault(i, 0);
            }
            return Math.max(0, base + cumulative);
        }
        return aptitudeLevel + getBaseLevelCost() - 1;
    }

    public int getLevelUpPointCost(int aptitudeLevel) {
        return getExperienceForLevel(getLevelUpExperienceLevels(aptitudeLevel));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void setBackground(ResourceLocation bg) {
        this.background = bg;
    }

    public int getBackgroundRepeat() {
        return backgroundRepeat;
    }

    public void setBackgroundRepeat(int repeat) {
        this.backgroundRepeat = Mth.clamp(repeat, 0, 64);
    }

    public void setLockedTextures(String... textures) {
        if (textures == null || textures.length == 0) {
            return;
        }
        this.lockedTexture = buildTextureArray(textures);
    }

    public void setRankIcon(int rank, String resourceLocation) {
        if (resourceLocation == null || resourceLocation.isBlank()) {
            return;
        }
        try {
            setRankIcon(rank, new ResourceLocation(resourceLocation));
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void setRankIcon(int rank, ResourceLocation resourceLocation) {
        if (rank < 0 || rank > 8 || resourceLocation == null) {
            return;
        }
        this.rankIcons.put(rank, resourceLocation);
    }

    public void removeRankIcon(int rank) {
        this.rankIcons.remove(rank);
    }

    public ResourceLocation getIconForLevel(int level) {
        int rank = Math.max(0, Math.min(8, Math.floorDiv(8 * Math.max(0, level), Math.max(1, getLevelCap()))));
        for (int i = rank; i >= 0; i--) {
            ResourceLocation icon = this.rankIcons.get(i);
            if (icon != null) {
                return icon;
            }
        }
        return getDefaultLockedTexture(level);
    }

    public void setList(List<Skill> list) {
        this.list = list;
    }

    private static String buildFallbackName(String aptitudeId) {
        if (aptitudeId == null || aptitudeId.isBlank()) {
            return "Unknown";
        }

        String[] parts = aptitudeId.split("[_\\-\\s]+");
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

        return builder.isEmpty() ? aptitudeId : builder.toString();
    }

    private static String buildFallbackAbbreviation(String aptitudeId) {
        if (aptitudeId == null || aptitudeId.isBlank()) {
            return "???";
        }

        String[] parts = aptitudeId.split("[^a-zA-Z0-9]+");
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
            String clean = aptitudeId.replaceAll("[^a-zA-Z0-9]", "");
            if (clean.isEmpty()) {
                return "???";
            }
            return clean.substring(0, Math.min(3, clean.length())).toUpperCase(Locale.ROOT);
        }

        return abbreviation.toString();
    }

    public List<Skill> getSkills(Aptitude aptitude) {
        List<Skill> list = new ArrayList<>();
        for (int i = 0; i < RegistrySkills.SKILLS_REGISTRY.get().getValues().stream().toList().size(); i++) {
            Skill skill = RegistrySkills.SKILLS_REGISTRY.get().getValues().stream().toList().get(i);
            if (skill.aptitude == aptitude) list.add(skill);
        }
        return list;
    }

    public List<Passive> getPassives(Aptitude aptitude) {
        List<Passive> list = new ArrayList<>();
        for (int i = 0; i < RegistryPassives.PASSIVES_REGISTRY.get().getValues().stream().toList().size(); i++) {
            Passive passive = RegistryPassives.PASSIVES_REGISTRY.get().getValues().stream().toList().get(i);
            if (passive.aptitude == aptitude) list.add(passive);
        }
        return list;
    }

    public int getLevel() {
        return AptitudeCapability.get().getAptitudeLevel(this);
    }

    public int getLevel(Player player) {
        return AptitudeCapability.get(player).getAptitudeLevel(this);
    }

    public MutableComponent getRank(int aptitudeLevel) {
        int maxLevel = getLevelCap();
        MutableComponent rank = Component.translatable("aptitude.justlevelingfork.rank.0");
        for (int i = 0; i < 9; i++) {
            if (aptitudeLevel >= (maxLevel / 8) * i) {
                rank = Component.translatable("aptitude.justlevelingfork.rank." + i);
            }
        }
        return rank;
    }

    public ResourceLocation getLockedTexture(int fromLevel) {
        return getIconForLevel(fromLevel);
    }

    public ResourceLocation getLockedTexture() {
        return getIconForLevel(getLevel());
    }

    private ResourceLocation getDefaultLockedTexture(int level) {
        int size = getLevelCap();
        int textureListSize = this.lockedTexture.length;

        if (getLevel() > size){
            AptitudeCapability.get().setAptitudeLevel(this, size);
        }

        int index = Math.floorDiv((Math.max(0, level) * textureListSize), Math.max(1, size));
        index = index == textureListSize ? index - 1 : index;

        if (index >= textureListSize) {
            index = textureListSize - 1;
        }
        if (index < 0) {
            index = 0;
        }

        return this.lockedTexture[index];
    }

    private static int getExperienceForLevel(int level) {
        if (level <= 0) return 0;
        if (level <= 15) return sum(level, 7, 2);
        if (level <= 30) return 315 + sum(level - 15, 37, 5);
        return 1395 + sum(level - 30, 112, 9);
    }

    private static int sum(int n, int a0, int d) {
        return n * (2 * a0 + (n - 1) * d) / 2;
    }
}
