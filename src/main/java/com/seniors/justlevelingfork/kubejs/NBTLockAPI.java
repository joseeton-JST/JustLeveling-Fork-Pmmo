package com.seniors.justlevelingfork.kubejs;

import com.seniors.justlevelingfork.handler.HandlerAptitude;
import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class NBTLockAPI {
    private static final Map<String, List<NbtLockRule>> ITEM_NBT_LOCKS = new LinkedHashMap<>();
    private static final Map<String, List<NbtLockRule>> MOD_NBT_LOCKS = new LinkedHashMap<>();
    private static final List<NbtLockRule> GENERIC_NBT_LOCKS = new ArrayList<>();

    private static String normalize(String value) {
        return RequirementParser.normalizeId(value);
    }

    private static CompoundTag parseCompoundTag(String snbt) {
        if (snbt == null || snbt.isBlank()) {
            throw new IllegalArgumentException("SNBT cannot be null or empty");
        }

        try {
            CompoundTag parsed = TagParser.parseTag(snbt);
            if (parsed == null || parsed.isEmpty()) {
                throw new IllegalArgumentException("SNBT must produce a non-empty CompoundTag");
            }
            return parsed;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid SNBT lock: " + snbt, e);
        }
    }

    public static void addItemNBTLock(String itemId, String snbt, Object... requirements) {
        String normalizedItem = normalize(itemId);
        if (normalizedItem.isEmpty()) {
            return;
        }

        Map<String, Integer> normalizedRequirements = RequirementParser.parseRequirements(requirements);
        if (normalizedRequirements.isEmpty()) {
            return;
        }

        CompoundTag requiredTag = parseCompoundTag(snbt);
        ITEM_NBT_LOCKS.computeIfAbsent(normalizedItem, ignored -> new ArrayList<>())
                .add(new NbtLockRule(NbtLockScope.ITEM, normalizedItem, null, requiredTag, normalizedRequirements));
        HandlerAptitude.invalidateCache();
    }

    public static void addModNBTLock(String modId, String snbt, Object... requirements) {
        String normalizedMod = normalize(modId);
        if (normalizedMod.isEmpty()) {
            return;
        }

        Map<String, Integer> normalizedRequirements = RequirementParser.parseRequirements(requirements);
        if (normalizedRequirements.isEmpty()) {
            return;
        }

        CompoundTag requiredTag = parseCompoundTag(snbt);
        MOD_NBT_LOCKS.computeIfAbsent(normalizedMod, ignored -> new ArrayList<>())
                .add(new NbtLockRule(NbtLockScope.MOD, null, normalizedMod, requiredTag, normalizedRequirements));
        HandlerAptitude.invalidateCache();
    }

    public static void addGenericNBTLock(String snbt, Object... requirements) {
        Map<String, Integer> normalizedRequirements = RequirementParser.parseRequirements(requirements);
        if (normalizedRequirements.isEmpty()) {
            return;
        }

        CompoundTag requiredTag = parseCompoundTag(snbt);
        GENERIC_NBT_LOCKS.add(new NbtLockRule(NbtLockScope.GENERIC, null, null, requiredTag, normalizedRequirements));
        HandlerAptitude.invalidateCache();
    }

    public static void removeItemNBTLocks(String itemId) {
        String normalizedItem = normalize(itemId);
        if (normalizedItem.isEmpty()) {
            return;
        }
        ITEM_NBT_LOCKS.remove(normalizedItem);
        HandlerAptitude.invalidateCache();
    }

    public static void removeModNBTLocks(String modId) {
        String normalizedMod = normalize(modId);
        if (normalizedMod.isEmpty()) {
            return;
        }
        MOD_NBT_LOCKS.remove(normalizedMod);
        HandlerAptitude.invalidateCache();
    }

    public static void clearAllNBTLocks() {
        ITEM_NBT_LOCKS.clear();
        MOD_NBT_LOCKS.clear();
        GENERIC_NBT_LOCKS.clear();
        HandlerAptitude.invalidateCache();
    }

    public static Map<String, Integer> getMatchingRequirements(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Map.of();
        }
        CompoundTag stackTag = stack.getTag();
        if (stackTag == null || stackTag.isEmpty()) {
            return Map.of();
        }

        String itemId = normalize(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(stack.getItem())).toString());
        String namespace = normalize(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(stack.getItem())).getNamespace());
        Map<String, Integer> merged = new LinkedHashMap<>();

        List<NbtLockRule> itemRules = ITEM_NBT_LOCKS.getOrDefault(itemId, List.of());
        for (NbtLockRule rule : itemRules) {
            mergeRequirementMap(merged, rule.requirements, stackTag, rule.requiredTag);
        }

        List<NbtLockRule> modRules = MOD_NBT_LOCKS.getOrDefault(namespace, List.of());
        for (NbtLockRule rule : modRules) {
            mergeRequirementMap(merged, rule.requirements, stackTag, rule.requiredTag);
        }

        for (NbtLockRule rule : GENERIC_NBT_LOCKS) {
            mergeRequirementMap(merged, rule.requirements, stackTag, rule.requiredTag);
        }

        return merged;
    }

    private static void mergeRequirementMap(Map<String, Integer> merged, Map<String, Integer> requirements, CompoundTag stackTag, CompoundTag requiredTag) {
        if (!matchesSubset(requiredTag, stackTag)) {
            return;
        }

        requirements.forEach((aptitude, level) ->
                merged.merge(aptitude, level, Math::max));
    }

    private static boolean matchesSubset(Tag required, Tag actual) {
        if (required == null) {
            return true;
        }
        if (actual == null) {
            return false;
        }

        if (required instanceof CompoundTag requiredCompound) {
            if (!(actual instanceof CompoundTag actualCompound)) {
                return false;
            }
            for (String key : requiredCompound.getAllKeys()) {
                if (!actualCompound.contains(key)) {
                    return false;
                }
                if (!matchesSubset(requiredCompound.get(key), actualCompound.get(key))) {
                    return false;
                }
            }
            return true;
        }

        if (required instanceof ListTag requiredList) {
            if (!(actual instanceof ListTag actualList)) {
                return false;
            }
            if (requiredList.size() != actualList.size()) {
                return false;
            }
            for (int i = 0; i < requiredList.size(); i++) {
                if (!matchesSubset(requiredList.get(i), actualList.get(i))) {
                    return false;
                }
            }
            return true;
        }

        return required.equals(actual);
    }

    private enum NbtLockScope {
        ITEM,
        MOD,
        GENERIC
    }

    private record NbtLockRule(
            NbtLockScope scope,
            String itemId,
            String modId,
            CompoundTag requiredTag,
            Map<String, Integer> requirements
    ) {
    }
}
