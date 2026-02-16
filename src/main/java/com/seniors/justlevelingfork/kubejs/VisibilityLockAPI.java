package com.seniors.justlevelingfork.kubejs;

import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class VisibilityLockAPI {
    private static final Map<String, Map<String, Integer>> VISIBILITY_LOCKS = new LinkedHashMap<>();

    public static void addVisibilityLock(String aptitude, Object... requirements) {
        String normalizedAptitude = RequirementParser.normalizeId(aptitude);
        if (normalizedAptitude.isEmpty()) {
            return;
        }

        Map<String, Integer> normalizedRequirements = RequirementParser.parseRequirements(requirements);
        if (normalizedRequirements.isEmpty()) {
            VISIBILITY_LOCKS.remove(normalizedAptitude);
            return;
        }

        VISIBILITY_LOCKS.put(normalizedAptitude, normalizedRequirements);
    }

    public static void removeVisibilityLock(String aptitude) {
        String normalizedAptitude = RequirementParser.normalizeId(aptitude);
        if (normalizedAptitude.isEmpty()) {
            return;
        }
        VISIBILITY_LOCKS.remove(normalizedAptitude);
    }

    public static void clearAllVisibilityLocks() {
        VISIBILITY_LOCKS.clear();
    }

    public static boolean isVisible(Player player, String aptitude) {
        String normalizedAptitude = RequirementParser.normalizeId(aptitude);
        if (normalizedAptitude.isEmpty()) {
            return false;
        }

        Map<String, Integer> requirements = VISIBILITY_LOCKS.get(normalizedAptitude);
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

    public static Map<String, Map<String, Integer>> getVisibilityLocks() {
        Map<String, Map<String, Integer>> copy = new LinkedHashMap<>();
        VISIBILITY_LOCKS.forEach((aptitude, requirements) ->
                copy.put(aptitude, Collections.unmodifiableMap(new LinkedHashMap<>(requirements))));
        return Collections.unmodifiableMap(copy);
    }
}
