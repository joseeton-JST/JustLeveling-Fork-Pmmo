package com.joseetoon.justlevellingaddonjs.compat.base121;

import com.joseetoon.justlevellingaddonjs.config.AddonCommonConfig;
import com.seniors.justlevelingfork.config.models.TitleModel;
import com.seniors.justlevelingfork.handler.HandlerTitlesConfig;
import com.seniors.justlevelingfork.JustLevelingFork;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.seniors.justlevelingfork.registry.passive.Passive;
import com.seniors.justlevelingfork.registry.skills.Skill;
import com.seniors.justlevelingfork.registry.title.Title;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class BackportRegistryState {
    private static final String TITLELESS = "titleless";
    public static final String SERVER_MANAGED_MIGRATION_FLAG = "jlfork_titles_server_managed_migrated_v1";
    private static final Map<ResourceLocation, Aptitude> PENDING_APTITUDES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Skill> PENDING_SKILLS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Passive> PENDING_PASSIVES = new LinkedHashMap<>();
    private static final Map<String, Title> PENDING_TITLES = new LinkedHashMap<>();
    private static final Map<String, List<String>> TITLE_CONDITIONS = new LinkedHashMap<>();
    private static final Set<String> DELETED_TITLES = new HashSet<>();
    private static final Set<String> DELETED_APTITUDES = new HashSet<>();
    private static final Set<String> SERVER_MANAGED_TITLES = new HashSet<>();
    private static final Map<String, TitleMetaOverride> TITLE_META_OVERRIDES = new LinkedHashMap<>();
    private static final Map<String, TitleTextOverride> TITLE_TEXT_OVERRIDES = new LinkedHashMap<>();
    private static final Map<String, Integer> TITLE_OVERHEAD_COLOR_OVERRIDES = new LinkedHashMap<>();
    private static final Map<String, String> APTITUDE_ABBREVIATION_OVERRIDES = new LinkedHashMap<>();
    private static final Map<String, Integer> APTITUDE_GLOBAL_LEVEL_WEIGHTS = new LinkedHashMap<>();
    private static int nextAptitudeIndex = 8;

    private BackportRegistryState() {
    }

    public static synchronized int nextAptitudeIndex(IForgeRegistry<Aptitude> registry) {
        int max = nextAptitudeIndex;
        if (registry != null) {
            for (Aptitude aptitude : registry.getValues()) {
                max = Math.max(max, aptitude.index + 1);
            }
        }
        for (Aptitude aptitude : PENDING_APTITUDES.values()) {
            max = Math.max(max, aptitude.index + 1);
        }
        nextAptitudeIndex = max + 1;
        return max;
    }

    public static synchronized void addPendingAptitude(ResourceLocation id, Aptitude aptitude) {
        if (id == null || aptitude == null) {
            return;
        }
        PENDING_APTITUDES.put(id, aptitude);
    }

    public static synchronized Map<ResourceLocation, Aptitude> pendingAptitudesSnapshot() {
        return Map.copyOf(PENDING_APTITUDES);
    }

    public static synchronized Aptitude findPendingAptitude(ResourceLocation id, String normalizedPath) {
        if (id != null) {
            Aptitude byId = PENDING_APTITUDES.get(id);
            if (byId != null) {
                return byId;
            }
        }

        if (normalizedPath == null) {
            return null;
        }

        return PENDING_APTITUDES.entrySet().stream()
                .filter(entry -> entry.getKey().getPath().equalsIgnoreCase(normalizedPath))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    public static synchronized void addPendingSkill(ResourceLocation id, Skill skill) {
        if (id == null || skill == null) {
            return;
        }
        PENDING_SKILLS.put(id, skill);
    }

    public static synchronized Map<ResourceLocation, Skill> pendingSkillsSnapshot() {
        return Map.copyOf(PENDING_SKILLS);
    }

    public static synchronized Skill findPendingSkill(ResourceLocation id, String normalizedPath) {
        if (id != null) {
            Skill byId = PENDING_SKILLS.get(id);
            if (byId != null) {
                return byId;
            }
        }

        if (normalizedPath == null) {
            return null;
        }

        return PENDING_SKILLS.entrySet().stream()
                .filter(entry -> entry.getKey().getPath().equalsIgnoreCase(normalizedPath))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    public static synchronized void addPendingPassive(ResourceLocation id, Passive passive) {
        if (id == null || passive == null) {
            return;
        }
        PENDING_PASSIVES.put(id, passive);
    }

    public static synchronized Map<ResourceLocation, Passive> pendingPassivesSnapshot() {
        return Map.copyOf(PENDING_PASSIVES);
    }

    public static synchronized Passive findPendingPassive(ResourceLocation id, String normalizedPath) {
        if (id != null) {
            Passive byId = PENDING_PASSIVES.get(id);
            if (byId != null) {
                return byId;
            }
        }

        if (normalizedPath == null) {
            return null;
        }

        return PENDING_PASSIVES.entrySet().stream()
                .filter(entry -> entry.getKey().getPath().equalsIgnoreCase(normalizedPath))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    public static synchronized void addPendingTitle(String name, Title title) {
        String normalized = normalizePath(name);
        if (normalized == null || title == null) {
            return;
        }
        DELETED_TITLES.remove(normalized);
        PENDING_TITLES.put(normalized, title);
    }

    public static synchronized Title findPendingTitle(String titleName) {
        String normalized = normalizePath(titleName);
        if (normalized == null) {
            return null;
        }
        return PENDING_TITLES.get(normalized);
    }

    public static synchronized Map<String, Title> pendingTitlesSnapshot() {
        return Map.copyOf(PENDING_TITLES);
    }

    public static synchronized boolean markTitleDeleted(String titleName) {
        String normalized = normalizePath(titleName);
        if (normalized == null) {
            return false;
        }
        return DELETED_TITLES.add(normalized);
    }

    public static synchronized boolean unmarkTitleDeleted(String titleName) {
        String normalized = normalizePath(titleName);
        if (normalized == null) {
            return false;
        }
        return DELETED_TITLES.remove(normalized);
    }

    public static synchronized boolean isTitleDeleted(String titleName) {
        String normalized = normalizePath(titleName);
        return normalized != null && DELETED_TITLES.contains(normalized);
    }

    public static synchronized boolean isTitleDeleted(ResourceLocation id) {
        return id != null && isTitleDeleted(id.getPath());
    }

    public static synchronized Set<String> deletedTitlesSnapshot() {
        return Set.copyOf(DELETED_TITLES);
    }

    public static synchronized boolean markAptitudeDeleted(String aptitudeName) {
        String normalized = normalizePath(aptitudeName);
        if (normalized == null) {
            return false;
        }
        return DELETED_APTITUDES.add(normalized);
    }

    public static synchronized boolean isAptitudeDeleted(String aptitudeName) {
        String normalized = normalizePath(aptitudeName);
        return normalized != null && DELETED_APTITUDES.contains(normalized);
    }

    public static synchronized boolean isAptitudeDeleted(ResourceLocation id) {
        return id != null && isAptitudeDeleted(id.getPath());
    }

    public static synchronized Set<String> deletedAptitudesSnapshot() {
        return Set.copyOf(DELETED_APTITUDES);
    }

    public static synchronized void setAptitudeAbbreviationOverride(String aptitudeName, String abbreviation) {
        String normalized = normalizePath(aptitudeName);
        String normalizedAbbreviation = normalizeText(abbreviation);
        if (normalized == null) {
            return;
        }
        if (normalizedAbbreviation == null) {
            APTITUDE_ABBREVIATION_OVERRIDES.remove(normalized);
            return;
        }
        APTITUDE_ABBREVIATION_OVERRIDES.put(normalized, normalizedAbbreviation);
    }

    public static synchronized void clearAptitudeAbbreviationOverride(String aptitudeName) {
        String normalized = normalizePath(aptitudeName);
        if (normalized == null) {
            return;
        }
        APTITUDE_ABBREVIATION_OVERRIDES.remove(normalized);
    }

    public static synchronized String getAptitudeAbbreviationOverride(String aptitudeName) {
        String normalized = normalizePath(aptitudeName);
        if (normalized == null) {
            return null;
        }
        return APTITUDE_ABBREVIATION_OVERRIDES.get(normalized);
    }

    public static synchronized Map<String, String> aptitudeAbbreviationOverridesSnapshot() {
        return Map.copyOf(APTITUDE_ABBREVIATION_OVERRIDES);
    }

    public static synchronized void setAptitudeGlobalLevelWeight(String name, int weight) {
        if (name == null || name.isEmpty()) {
            return;
        }
        APTITUDE_GLOBAL_LEVEL_WEIGHTS.put(name.toLowerCase(Locale.ROOT), Math.max(1, weight));
    }

    public static int getAptitudeGlobalLevelWeight(String name) {
        if (name == null) {
            return 1;
        }
        return APTITUDE_GLOBAL_LEVEL_WEIGHTS.getOrDefault(name.toLowerCase(Locale.ROOT), 1);
    }

    public static boolean isSkillBlockedByDeletedAptitude(Skill skill) {
        if (skill == null || skill.aptitude == null) {
            return false;
        }
        return isAptitudeDeleted(skill.aptitude.getName());
    }

    public static boolean isPassiveBlockedByDeletedAptitude(Passive passive) {
        if (passive == null || passive.aptitude == null) {
            return false;
        }
        return isAptitudeDeleted(passive.aptitude.getName());
    }

    public static synchronized boolean markTitleServerManaged(String titleName) {
        String normalized = normalizePath(titleName);
        if (normalized == null) {
            return false;
        }
        return SERVER_MANAGED_TITLES.add(normalized);
    }

    public static synchronized boolean unmarkTitleServerManaged(String titleName) {
        String normalized = normalizePath(titleName);
        if (normalized == null) {
            return false;
        }
        return SERVER_MANAGED_TITLES.remove(normalized);
    }

    public static synchronized boolean isTitleServerManaged(String titleName) {
        String normalized = normalizePath(titleName);
        return normalized != null && SERVER_MANAGED_TITLES.contains(normalized);
    }

    public static synchronized Set<String> serverManagedTitlesSnapshot() {
        return Set.copyOf(SERVER_MANAGED_TITLES);
    }

    public static synchronized void setTitleMetaOverride(String titleName, boolean defaultUnlocked, boolean hideRequirements) {
        String normalized = normalizePath(titleName);
        if (normalized == null) {
            return;
        }
        TITLE_META_OVERRIDES.put(normalized, new TitleMetaOverride(defaultUnlocked, hideRequirements));
    }

    public static synchronized TitleMetaOverride getTitleMetaOverride(String titleName) {
        String normalized = normalizePath(titleName);
        if (normalized == null) {
            return null;
        }
        return TITLE_META_OVERRIDES.get(normalized);
    }

    public static synchronized Map<String, TitleMetaOverride> titleMetaOverridesSnapshot() {
        return Map.copyOf(TITLE_META_OVERRIDES);
    }

    public static synchronized void setTitleDisplayNameOverride(String titleName, String displayName) {
        String normalized = normalizePath(titleName);
        if (normalized == null) {
            return;
        }
        TitleTextOverride current = TITLE_TEXT_OVERRIDES.get(normalized);
        String normalizedName = normalizeText(displayName);
        String description = current != null ? current.descriptionOverride() : null;
        if (normalizedName == null && description == null) {
            TITLE_TEXT_OVERRIDES.remove(normalized);
            return;
        }
        TITLE_TEXT_OVERRIDES.put(normalized, new TitleTextOverride(normalizedName, description));
    }

    public static synchronized void setTitleDescriptionOverride(String titleName, String description) {
        String normalized = normalizePath(titleName);
        if (normalized == null) {
            return;
        }
        TitleTextOverride current = TITLE_TEXT_OVERRIDES.get(normalized);
        String normalizedDescription = normalizeText(description);
        String displayName = current != null ? current.displayNameOverride() : null;
        if (displayName == null && normalizedDescription == null) {
            TITLE_TEXT_OVERRIDES.remove(normalized);
            return;
        }
        TITLE_TEXT_OVERRIDES.put(normalized, new TitleTextOverride(displayName, normalizedDescription));
    }

    public static synchronized void clearTitleTextOverrides(String titleName) {
        String normalized = normalizePath(titleName);
        if (normalized == null) {
            return;
        }
        TITLE_TEXT_OVERRIDES.remove(normalized);
    }

    public static synchronized TitleTextOverride getTitleTextOverride(String titleName) {
        String normalized = normalizePath(titleName);
        if (normalized == null) {
            return null;
        }
        return TITLE_TEXT_OVERRIDES.get(normalized);
    }

    public static synchronized Map<String, TitleTextOverride> titleTextOverridesSnapshot() {
        return Map.copyOf(TITLE_TEXT_OVERRIDES);
    }

    public static synchronized void setTitleOverheadColorOverride(String titleName, int rgb) {
        String normalized = normalizePath(titleName);
        if (normalized == null) {
            return;
        }
        TITLE_OVERHEAD_COLOR_OVERRIDES.put(normalized, rgb & 0xFFFFFF);
    }

    public static synchronized void clearTitleOverheadColorOverride(String titleName) {
        String normalized = normalizePath(titleName);
        if (normalized == null) {
            return;
        }
        TITLE_OVERHEAD_COLOR_OVERRIDES.remove(normalized);
    }

    public static synchronized Integer getTitleOverheadColorOverride(String titleName) {
        String normalized = normalizePath(titleName);
        if (normalized == null) {
            return null;
        }
        return TITLE_OVERHEAD_COLOR_OVERRIDES.get(normalized);
    }

    public static synchronized Map<String, Integer> titleOverheadColorOverridesSnapshot() {
        return Map.copyOf(TITLE_OVERHEAD_COLOR_OVERRIDES);
    }

    public static synchronized void replaceTitleOverheadColorOverrides(Map<String, Integer> replacements) {
        TITLE_OVERHEAD_COLOR_OVERRIDES.clear();
        if (replacements == null || replacements.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Integer> entry : replacements.entrySet()) {
            String normalized = normalizePath(entry.getKey());
            Integer rgb = entry.getValue();
            if (normalized == null || rgb == null) {
                continue;
            }
            TITLE_OVERHEAD_COLOR_OVERRIDES.put(normalized, rgb & 0xFFFFFF);
        }
    }

    public static synchronized boolean getEffectiveDefaultUnlocked(Title title) {
        if (title == null) {
            return false;
        }
        TitleMetaOverride override = getTitleMetaOverride(title.getName());
        return override != null ? override.defaultUnlocked() : title.Requirement;
    }

    public static synchronized boolean getEffectiveHideRequirements(Title title) {
        if (title == null) {
            return false;
        }
        TitleMetaOverride override = getTitleMetaOverride(title.getName());
        return override != null ? override.hideRequirements() : title.HideRequirements;
    }

    public static synchronized Title ensureTitlelessPresent(IForgeRegistry<Title> registry) {
        DELETED_TITLES.remove(TITLELESS);

        Title pending = PENDING_TITLES.get(TITLELESS);
        if (pending != null) {
            return pending;
        }

        if (registry != null) {
            ResourceLocation titlelessId = new ResourceLocation(JustLevelingFork.MOD_ID, TITLELESS);
            Title fromId = registry.getValue(titlelessId);
            if (fromId != null) {
                return fromId;
            }

            Title byPath = registry.getValues().stream()
                    .filter(title -> TITLELESS.equalsIgnoreCase(title.getName()))
                    .findFirst()
                    .orElse(null);
            if (byPath != null) {
                return byPath;
            }
        }

        Title recreated = new Title(new ResourceLocation(JustLevelingFork.MOD_ID, TITLELESS), true, true);
        PENDING_TITLES.put(TITLELESS, recreated);
        return recreated;
    }

    public static synchronized void setTitleConditions(String titleName, List<String> conditions) {
        String normalized = normalizePath(titleName);
        if (normalized == null) {
            return;
        }

        List<String> normalizedConditions = new ArrayList<>();
        if (conditions != null) {
            for (String condition : conditions) {
                if (condition != null && !condition.isBlank()) {
                    normalizedConditions.add(condition.trim());
                }
            }
        }

        TITLE_CONDITIONS.put(normalized, normalizedConditions);
    }

    public static synchronized void clearTitleConditions(String titleName) {
        String normalized = normalizePath(titleName);
        if (normalized == null) {
            return;
        }
        TITLE_CONDITIONS.remove(normalized);
    }

    public static synchronized Map<String, List<String>> titleConditionsSnapshot() {
        Map<String, List<String>> copy = new LinkedHashMap<>();
        TITLE_CONDITIONS.forEach((key, value) -> copy.put(key, List.copyOf(value)));
        return Collections.unmodifiableMap(copy);
    }

    public static boolean isTitleDisabledByAddonConfig(String titleName) {
        if (!AddonCommonConfig.disableBaseModTitles.get()) {
            return false;
        }

        String normalized = normalizePath(titleName);
        if (normalized == null || TITLELESS.equals(normalized)) {
            return false;
        }

        return baseTitleIdsSnapshot().contains(normalized);
    }

    public static synchronized Set<String> baseTitleIdsSnapshot() {
        Set<String> baseTitles = new HashSet<>();
        try {
            List<TitleModel> titleList = HandlerTitlesConfig.HANDLER.instance().titleList;
            if (titleList == null) {
                return Set.of();
            }

            for (TitleModel model : titleList) {
                if (model == null) {
                    continue;
                }
                String normalized = normalizePath(model.TitleId);
                if (normalized != null && !TITLELESS.equals(normalized)) {
                    baseTitles.add(normalized);
                }
            }
        } catch (Throwable ignored) {
            return Set.of();
        }
        return Set.copyOf(baseTitles);
    }

    public static String normalizePath(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        ResourceLocation parsed = ResourceLocation.tryParse(raw.toLowerCase(Locale.ROOT));
        if (parsed != null) {
            return parsed.getPath().toLowerCase(Locale.ROOT);
        }

        return raw.toLowerCase(Locale.ROOT);
    }

    private static String normalizeText(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record TitleMetaOverride(boolean defaultUnlocked, boolean hideRequirements) {
    }

    public record TitleTextOverride(String displayNameOverride, String descriptionOverride) {
    }
}
