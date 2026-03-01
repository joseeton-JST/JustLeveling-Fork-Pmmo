package com.joseetoon.justlevellingaddonjs.kubejs;

import com.joseetoon.justlevellingaddonjs.compat.base121.BackportRegistryState;
import com.joseetoon.justlevellingaddonjs.kubejs.compat.TitleCompat;
import com.joseetoon.justlevellingaddonjs.network.AddonNetworking;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import com.seniors.justlevelingfork.registry.title.Title;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class TitleAPI {
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^[0-9a-fA-F]{6}$");

    public static ConditionBuilder conditions(String titleName) {
        return new ConditionBuilder(titleName);
    }

    public static void clearConditions(String titleName) {
        BackportRegistryState.clearTitleConditions(titleName);
    }

    public static Map<String, List<String>> getKubeJSConditionsSnapshot() {
        Map<String, List<String>> snapshot = BackportRegistryState.titleConditionsSnapshot();
        Map<String, List<String>> copy = new LinkedHashMap<>();
        snapshot.forEach((key, value) -> copy.put(key, new ArrayList<>(value)));
        return copy;
    }

    public static boolean remove(String titleName) {
        String normalized = BackportRegistryState.normalizePath(titleName);
        if (normalized == null) {
            return false;
        }
        BackportRegistryState.markTitleDeleted(normalized);
        return true;
    }

    public static boolean restore(String titleName) {
        String normalized = BackportRegistryState.normalizePath(titleName);
        if (normalized == null) {
            return false;
        }

        if (!BackportRegistryState.isTitleDeleted(normalized)) {
            return titleKnown(normalized);
        }

        BackportRegistryState.unmarkTitleDeleted(normalized);
        if ("titleless".equals(normalized)) {
            BackportRegistryState.ensureTitlelessPresent(RegistryTitles.TITLES_REGISTRY.get());
        }
        return true;
    }

    public static boolean update(String titleName, boolean defaultUnlocked, boolean hideRequirements) {
        String normalized = BackportRegistryState.normalizePath(titleName);
        if (normalized == null) {
            return false;
        }
        if (!titleKnown(normalized) && !BackportRegistryState.isTitleDeleted(normalized)) {
            return false;
        }
        BackportRegistryState.setTitleMetaOverride(normalized, defaultUnlocked, hideRequirements);
        return true;
    }

    public static boolean setDisplayNameOverride(String titleName, String displayName) {
        String normalized = BackportRegistryState.normalizePath(titleName);
        if (normalized == null) {
            return false;
        }
        BackportRegistryState.setTitleDisplayNameOverride(normalized, displayName);
        return true;
    }

    public static boolean setDescriptionOverride(String titleName, String description) {
        String normalized = BackportRegistryState.normalizePath(titleName);
        if (normalized == null) {
            return false;
        }
        BackportRegistryState.setTitleDescriptionOverride(normalized, description);
        return true;
    }

    public static boolean clearTextOverrides(String titleName) {
        String normalized = BackportRegistryState.normalizePath(titleName);
        if (normalized == null) {
            return false;
        }
        BackportRegistryState.clearTitleTextOverrides(normalized);
        return true;
    }

    public static boolean setServerManaged(String titleName, boolean value) {
        String normalized = BackportRegistryState.normalizePath(titleName);
        if (normalized == null) {
            return false;
        }
        if (!titleKnown(normalized) && !BackportRegistryState.isTitleDeleted(normalized)) {
            return false;
        }
        if (value) {
            BackportRegistryState.markTitleServerManaged(normalized);
        } else {
            BackportRegistryState.unmarkTitleServerManaged(normalized);
        }
        return true;
    }

    public static boolean isServerManaged(String titleName) {
        return BackportRegistryState.isTitleServerManaged(titleName);
    }

    public static boolean setOverheadColor(String titleName, String hexRgb) {
        String normalized = BackportRegistryState.normalizePath(titleName);
        if (normalized == null) {
            return false;
        }
        if (!titleKnown(normalized)
                || BackportRegistryState.isTitleDeleted(normalized)
                || BackportRegistryState.isTitleDisabledByAddonConfig(normalized)) {
            return false;
        }

        Integer rgb = parseHexRgb(hexRgb);
        if (rgb == null) {
            return false;
        }

        BackportRegistryState.setTitleOverheadColorOverride(normalized, rgb);
        AddonNetworking.syncTitleColorsToAllPlayers();
        return true;
    }

    public static boolean clearOverheadColor(String titleName) {
        String normalized = BackportRegistryState.normalizePath(titleName);
        if (normalized == null) {
            return false;
        }
        if (!titleKnown(normalized)
                || BackportRegistryState.isTitleDeleted(normalized)
                || BackportRegistryState.isTitleDisabledByAddonConfig(normalized)) {
            return false;
        }

        BackportRegistryState.clearTitleOverheadColorOverride(normalized);
        AddonNetworking.syncTitleColorsToAllPlayers();
        return true;
    }

    public static String getOverheadColor(String titleName) {
        Integer rgb = BackportRegistryState.getTitleOverheadColorOverride(titleName);
        return rgb == null ? "" : formatHexRgb(rgb);
    }

    public static boolean exists(String titleName) {
        String normalized = BackportRegistryState.normalizePath(titleName);
        if (normalized == null
                || BackportRegistryState.isTitleDeleted(normalized)
                || BackportRegistryState.isTitleDisabledByAddonConfig(normalized)) {
            return false;
        }
        return TitleCompat.getByName(normalized) != null;
    }

    public static boolean isDeleted(String titleName) {
        return BackportRegistryState.isTitleDeleted(titleName);
    }

    public static Map<String, Object> getStateSnapshot() {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("deletedTitles", new ArrayList<>(BackportRegistryState.deletedTitlesSnapshot()));

        Map<String, Object> metaOverrides = new LinkedHashMap<>();
        BackportRegistryState.titleMetaOverridesSnapshot().forEach((key, override) -> {
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("defaultUnlocked", override.defaultUnlocked());
            meta.put("hideRequirements", override.hideRequirements());
            metaOverrides.put(key, meta);
        });
        snapshot.put("metaOverrides", metaOverrides);

        Map<String, Object> textOverrides = new LinkedHashMap<>();
        BackportRegistryState.titleTextOverridesSnapshot().forEach((key, override) -> {
            Map<String, Object> text = new LinkedHashMap<>();
            text.put("displayName", override.displayNameOverride());
            text.put("description", override.descriptionOverride());
            textOverrides.put(key, text);
        });
        snapshot.put("textOverrides", textOverrides);
        Map<String, String> colorOverrides = new LinkedHashMap<>();
        BackportRegistryState.titleOverheadColorOverridesSnapshot()
                .forEach((key, value) -> colorOverrides.put(key, formatHexRgb(value)));
        snapshot.put("overheadColorOverrides", colorOverrides);
        snapshot.put("serverManagedTitles", new ArrayList<>(BackportRegistryState.serverManagedTitlesSnapshot()));

        snapshot.put("conditions", getKubeJSConditionsSnapshot());
        return snapshot;
    }

    private static boolean titleKnown(String normalized) {
        if (normalized == null || normalized.isBlank()) {
            return false;
        }

        if (BackportRegistryState.pendingTitlesSnapshot().containsKey(normalized)) {
            return true;
        }

        Title direct = TitleCompat.getByName(normalized);
        if (direct != null) {
            return true;
        }

        if (RegistryTitles.TITLES_REGISTRY.get() == null) {
            return false;
        }

        ResourceLocation id = new ResourceLocation("justlevelingfork", normalized);
        if (RegistryTitles.TITLES_REGISTRY.get().containsKey(id)) {
            return true;
        }

        for (Title title : RegistryTitles.TITLES_REGISTRY.get().getValues()) {
            if (title != null && normalized.equalsIgnoreCase(title.getName())) {
                return true;
            }
        }
        return false;
    }

    private static Integer parseHexRgb(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }
        if (!HEX_COLOR_PATTERN.matcher(normalized).matches()) {
            return null;
        }

        try {
            return Integer.parseInt(normalized, 16) & 0xFFFFFF;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String formatHexRgb(int rgb) {
        return String.format(Locale.ROOT, "#%06X", rgb & 0xFFFFFF);
    }

    public enum Comparator {
        EQUALS,
        GREATER,
        LESS,
        GREATER_OR_EQUAL,
        LESS_OR_EQUAL;

        public String serializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public static class ConditionBuilder {
        private final String titleName;
        private final List<String> conditions = new ArrayList<>();

        private ConditionBuilder(String titleName) {
            if (titleName == null || titleName.isBlank()) {
                throw new IllegalArgumentException("titleName can't be null or empty");
            }
            this.titleName = titleName.toLowerCase(Locale.ROOT);
        }

        public ConditionBuilder aptitude(String aptitudeName, Comparator comparator, int value) {
            conditions.add(buildCondition("aptitude", normalizePath(aptitudeName), comparator, Integer.toString(value)));
            return this;
        }

        public ConditionBuilder stat(String statId, Comparator comparator, int value) {
            conditions.add(buildCondition("stat", normalizePath(statId), comparator, Integer.toString(value)));
            return this;
        }

        public ConditionBuilder entityKilled(String entityId, Comparator comparator, int value) {
            conditions.add(buildCondition("entityKilled", normalizePath(entityId), comparator, Integer.toString(value)));
            return this;
        }

        public ConditionBuilder special(String key, Comparator comparator, String value) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Special condition value can't be null or empty");
            }
            conditions.add(buildCondition("special", normalizePath(key), comparator, value.toLowerCase(Locale.ROOT)));
            return this;
        }

        public void register() {
            BackportRegistryState.setTitleConditions(titleName, conditions);
        }

        private static String buildCondition(String type, String variable, Comparator comparator, String value) {
            if (comparator == null) {
                throw new IllegalArgumentException("Comparator can't be null");
            }
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Condition value can't be null or empty");
            }
            return type + "/" + variable + "/" + comparator.serializedName() + "/" + value;
        }

        private static String normalizePath(String value) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Condition variable can't be null or empty");
            }
            return value.toLowerCase(Locale.ROOT);
        }
    }
}

