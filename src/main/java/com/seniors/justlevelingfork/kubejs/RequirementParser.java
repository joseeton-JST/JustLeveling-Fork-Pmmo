package com.seniors.justlevelingfork.kubejs;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class RequirementParser {
    private RequirementParser() {
    }

    static String normalizeId(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    static int parsePositiveLevel(Object value) {
        if (value == null) {
            return -1;
        }

        if (value instanceof Number number) {
            int parsed = (int) Math.floor(number.doubleValue());
            return parsed > 0 ? parsed : -1;
        }

        try {
            int parsed = (int) Math.floor(Double.parseDouble(value.toString().trim()));
            return parsed > 0 ? parsed : -1;
        } catch (Exception ignored) {
            return -1;
        }
    }

    static Map<String, Integer> normalizeRequirements(Map<?, ?> requirements) {
        Map<String, Integer> normalized = new LinkedHashMap<>();
        if (requirements == null) {
            return normalized;
        }

        requirements.forEach((aptitude, rawLevel) -> {
            String aptitudeId = normalizeId(aptitude == null ? null : aptitude.toString());
            int level = parsePositiveLevel(rawLevel);
            if (!aptitudeId.isEmpty() && level > 0) {
                normalized.merge(aptitudeId, level, Math::max);
            }
        });

        return normalized;
    }

    static Map<String, Integer> parseRequirements(Object... requirements) {
        Map<String, Integer> normalized = new LinkedHashMap<>();
        if (requirements == null || requirements.length == 0) {
            return normalized;
        }

        List<String> tokens = new ArrayList<>();
        for (Object requirement : requirements) {
            collectRequirement(requirement, normalized, tokens);
        }

        Map<String, Integer> tokenRequirements = parseRequirementTokens(tokens.toArray(String[]::new));
        tokenRequirements.forEach((aptitude, level) -> normalized.merge(aptitude, level, Math::max));
        return normalized;
    }

    private static void collectRequirement(Object requirement, Map<String, Integer> normalized, List<String> tokens) {
        if (requirement == null) {
            return;
        }

        if (requirement instanceof Map<?, ?> requirementMap) {
            Map<String, Integer> mapRequirements = normalizeRequirements(requirementMap);
            mapRequirements.forEach((aptitude, level) -> normalized.merge(aptitude, level, Math::max));
            return;
        }

        if (requirement instanceof Iterable<?> iterable && !(requirement instanceof CharSequence)) {
            for (Object nested : iterable) {
                collectRequirement(nested, normalized, tokens);
            }
            return;
        }

        if (requirement.getClass().isArray()) {
            int length = Array.getLength(requirement);
            for (int index = 0; index < length; index++) {
                collectRequirement(Array.get(requirement, index), normalized, tokens);
            }
            return;
        }

        tokens.add(requirement.toString());
    }

    static Map<String, Integer> parseRequirementTokens(String... requirements) {
        Map<String, Integer> normalized = new LinkedHashMap<>();
        if (requirements == null) {
            return normalized;
        }

        for (int i = 0; i < requirements.length; i++) {
            String requirement = requirements[i];
            if (requirement == null || requirement.isBlank()) {
                continue;
            }

            // Pair format compatibility: "aptitude", "level"
            if (!requirement.contains("|") && i + 1 < requirements.length) {
                String maybeLevel = requirements[i + 1];
                int parsedLevel = parsePositiveLevel(maybeLevel);
                String aptitudeId = normalizeId(requirement);
                if (!aptitudeId.isEmpty() && parsedLevel > 0) {
                    normalized.merge(aptitudeId, parsedLevel, Math::max);
                    i++;
                    continue;
                }
            }

            String[] split = requirement.split("\\|");
            if (split.length != 2) {
                continue;
            }

            String aptitudeId = normalizeId(split[0]);
            int level = parsePositiveLevel(split[1]);
            if (aptitudeId.isEmpty() || level <= 0) {
                continue;
            }

            normalized.merge(aptitudeId, level, Math::max);
        }

        return normalized;
    }
}
