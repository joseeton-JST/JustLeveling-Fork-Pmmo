package com.joseetoon.justlevellingaddonjs.compat.base121;

import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.seniors.justlevelingfork.registry.passive.Passive;
import com.seniors.justlevelingfork.registry.skills.Skill;
import com.seniors.justlevelingfork.registry.title.Title;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class BackportRegistryState {
    private static final Map<ResourceLocation, Aptitude> PENDING_APTITUDES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Skill> PENDING_SKILLS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Passive> PENDING_PASSIVES = new LinkedHashMap<>();
    private static final Map<String, Title> PENDING_TITLES = new LinkedHashMap<>();
    private static final Map<String, List<String>> TITLE_CONDITIONS = new LinkedHashMap<>();
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
}
