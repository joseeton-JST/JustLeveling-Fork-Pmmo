package com.seniors.justlevelingfork.kubejs;

import com.seniors.justlevelingfork.handler.HandlerAptitude;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LockItemAPI {
    private static final Map<String, Map<String, Integer>> ITEM_LOCKS = new LinkedHashMap<>();
    private static final Set<String> REMOVED_LOCKS = new HashSet<>();
    private static final Map<String, Map<String, Integer>> MOD_LOCKS = new LinkedHashMap<>();
    private static boolean clearDefaults = false;

    private static String normalize(String value) {
        return RequirementParser.normalizeId(value);
    }

    public static void clearAll() {
        clearDefaults = true;
        ITEM_LOCKS.clear();
        REMOVED_LOCKS.clear();
        MOD_LOCKS.clear();
        NBTLockAPI.clearAllNBTLocks();
        HandlerAptitude.invalidateCache();
    }

    public static void addLock(String itemId, Object... requirements) {
        String normalizedItem = normalize(itemId);
        if (normalizedItem.isEmpty()) {
            return;
        }

        Map<String, Integer> normalizedRequirements = RequirementParser.parseRequirements(requirements);
        if (normalizedRequirements.isEmpty()) {
            ITEM_LOCKS.remove(normalizedItem);
        } else {
            ITEM_LOCKS.put(normalizedItem, normalizedRequirements);
        }

        REMOVED_LOCKS.remove(normalizedItem);
        HandlerAptitude.invalidateCache();
    }

    public static void removeLock(String itemId) {
        String normalizedItem = normalize(itemId);
        if (normalizedItem.isEmpty()) {
            return;
        }

        ITEM_LOCKS.remove(normalizedItem);
        REMOVED_LOCKS.add(normalizedItem);
        HandlerAptitude.invalidateCache();
    }

    public static void addModLock(String modId, Object... requirements) {
        String normalizedMod = normalize(modId);
        if (normalizedMod.isEmpty()) {
            return;
        }

        Map<String, Integer> normalizedRequirements = RequirementParser.parseRequirements(requirements);
        if (normalizedRequirements.isEmpty()) {
            MOD_LOCKS.remove(normalizedMod);
        } else {
            MOD_LOCKS.put(normalizedMod, normalizedRequirements);
        }

        HandlerAptitude.invalidateCache();
    }

    public static boolean isClearDefaults() {
        return clearDefaults;
    }

    public static Map<String, Map<String, Integer>> getItemLocks() {
        return Collections.unmodifiableMap(ITEM_LOCKS);
    }

    public static Set<String> getRemovedLocks() {
        return Collections.unmodifiableSet(REMOVED_LOCKS);
    }

    public static Map<String, Map<String, Integer>> getModLocks() {
        return Collections.unmodifiableMap(MOD_LOCKS);
    }
}
