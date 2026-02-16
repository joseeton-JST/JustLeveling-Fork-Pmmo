package com.seniors.justlevelingfork.kubejs;

import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class LevelLockAPI {
    private static final Map<String, Map<Integer, Map<String, Integer>>> LEVEL_LOCKS = new LinkedHashMap<>();

    public static void addLevelLock(String aptitude, int targetLevel, Object... requirements) {
        String normalizedAptitude = RequirementParser.normalizeId(aptitude);
        if (normalizedAptitude.isEmpty() || targetLevel < 1) {
            return;
        }

        Map<String, Integer> normalizedRequirements = RequirementParser.parseRequirements(requirements);
        if (normalizedRequirements.isEmpty()) {
            removeLevelLock(normalizedAptitude, targetLevel);
            return;
        }

        Map<Integer, Map<String, Integer>> aptitudeLocks =
                LEVEL_LOCKS.computeIfAbsent(normalizedAptitude, ignored -> new LinkedHashMap<>());
        Map<String, Integer> merged =
                new LinkedHashMap<>(aptitudeLocks.getOrDefault(targetLevel, Collections.emptyMap()));
        normalizedRequirements.forEach((reqAptitude, reqLevel) -> merged.merge(reqAptitude, reqLevel, Math::max));
        aptitudeLocks.put(targetLevel, merged);
    }

    public static void removeLevelLock(String aptitude, int targetLevel) {
        String normalizedAptitude = RequirementParser.normalizeId(aptitude);
        if (normalizedAptitude.isEmpty() || targetLevel < 1) {
            return;
        }

        Map<Integer, Map<String, Integer>> aptitudeLocks = LEVEL_LOCKS.get(normalizedAptitude);
        if (aptitudeLocks == null) {
            return;
        }

        aptitudeLocks.remove(targetLevel);
        if (aptitudeLocks.isEmpty()) {
            LEVEL_LOCKS.remove(normalizedAptitude);
        }
    }

    public static void clearLevelLocks(String aptitude) {
        String normalizedAptitude = RequirementParser.normalizeId(aptitude);
        if (normalizedAptitude.isEmpty()) {
            return;
        }
        LEVEL_LOCKS.remove(normalizedAptitude);
    }

    public static void clearAllLevelLocks() {
        LEVEL_LOCKS.clear();
    }

    public static Map<String, Map<Integer, Map<String, Integer>>> getLevelLocks() {
        Map<String, Map<Integer, Map<String, Integer>>> copy = new LinkedHashMap<>();
        LEVEL_LOCKS.forEach((aptitude, levelMap) -> {
            Map<Integer, Map<String, Integer>> levelsCopy = new LinkedHashMap<>();
            levelMap.forEach((level, requirements) ->
                    levelsCopy.put(level, Collections.unmodifiableMap(new LinkedHashMap<>(requirements))));
            copy.put(aptitude, Collections.unmodifiableMap(levelsCopy));
        });
        return Collections.unmodifiableMap(copy);
    }

    public static boolean canReachLevel(Player player, String aptitude, int targetLevel) {
        if (targetLevel <= 1) {
            return true;
        }

        String normalizedAptitude = RequirementParser.normalizeId(aptitude);
        if (normalizedAptitude.isEmpty()) {
            return false;
        }

        Map<Integer, Map<String, Integer>> aptitudeLocks = LEVEL_LOCKS.get(normalizedAptitude);
        if (aptitudeLocks == null || aptitudeLocks.isEmpty()) {
            return true;
        }

        Map<String, Integer> requirements = aptitudeLocks.get(targetLevel);
        if (requirements == null || requirements.isEmpty()) {
            return true;
        }

        if (player == null) {
            return false;
        }

        AptitudeCapability capability = AptitudeCapability.get(player);
        if (capability == null) {
            return false;
        }

        for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
            Aptitude requiredAptitude = RegistryAptitudes.getAptitude(entry.getKey());
            if (requiredAptitude == null) {
                return false;
            }

            if (capability.getAptitudeLevel(requiredAptitude) < entry.getValue()) {
                return false;
            }
        }

        return true;
    }
}
