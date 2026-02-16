package com.seniors.justlevelingfork.kubejs;

import com.seniors.justlevelingfork.registry.RegistryTitles;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TitleAPI {

    public static ConditionBuilder conditions(String titleName) {
        return new ConditionBuilder(titleName);
    }

    public static void clearConditions(String titleName) {
        RegistryTitles.clearKubeJSConditions(titleName);
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
            RegistryTitles.setKubeJSConditions(titleName, conditions);
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
