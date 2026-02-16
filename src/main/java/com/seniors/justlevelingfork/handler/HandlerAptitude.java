package com.seniors.justlevelingfork.handler;

import com.seniors.justlevelingfork.JustLevelingFork;
import com.seniors.justlevelingfork.client.core.Aptitudes;
import com.seniors.justlevelingfork.config.models.LockItem;
import com.seniors.justlevelingfork.kubejs.LockItemAPI;
import com.seniors.justlevelingfork.kubejs.NBTLockAPI;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class HandlerAptitude {
    private static Map<String, List<com.seniors.justlevelingfork.client.core.Aptitudes>> Aptitudes;

    public static void invalidateCache() {
        Aptitudes = null;
    }

    public static void UpdateLockItems(List<LockItem> lockItems) {
        Aptitudes = mapFromLockItems(lockItems);
    }

    public static Map<String, List<Aptitudes>> getAptitude() {
        return mapFromLockItems(HandlerLockItemsConfig.HANDLER.instance().lockItemList);
    }

    private static Map<String, List<Aptitudes>> mapFromLockItems(List<LockItem> lockItems) {
        Map<String, List<Aptitudes>> aptitudeMap = new HashMap<>();
        if (lockItems == null) {
            return aptitudeMap;
        }

        for (LockItem lockItem : lockItems) {
            if (lockItem == null || lockItem.Item == null || lockItem.Item.isEmpty()) {
                continue;
            }
            if (lockItem.Aptitudes == null) {
                continue;
            }

            List<Aptitudes> aptitudesList = new ArrayList<>();
            for (LockItem.Aptitude aptitude : lockItem.Aptitudes) {
                if (aptitude == null || aptitude.Aptitude == null || aptitude.Aptitude.isEmpty()) {
                    JustLevelingFork.getLOGGER().warn("Item {} with wrong aptitude (APTITUDE NOT FOUND), skipping", lockItem.Item);
                    continue;
                }

                Aptitude aptitudeName = RegistryAptitudes.getAptitude(aptitude.Aptitude);
                if (aptitudeName == null) {
                    JustLevelingFork.getLOGGER().warn("Item {} with wrong aptitude (APTITUDE \"{}\" NOT FOUND), skipping", lockItem.Item, aptitude.Aptitude);
                    continue;
                }

                if (!aptitudeName.isEnabled()) {
                    continue;
                }

                aptitudesList.add(new Aptitudes(aptitude.Aptitude, lockItem.Item, false, aptitudeName, aptitude.Level));
            }

            if (!aptitudesList.isEmpty()) {
                aptitudeMap.put(lockItem.Item.toLowerCase(Locale.ROOT), aptitudesList);
            }
        }

        return aptitudeMap;
    }

    private static List<Aptitudes> mapRequirements(String itemId, Map<String, Integer> requirements) {
        List<Aptitudes> aptitudesList = new ArrayList<>();
        if (requirements == null) {
            return aptitudesList;
        }

        for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
            String aptitudeId = entry.getKey();
            Integer level = entry.getValue();
            if (aptitudeId == null || aptitudeId.isEmpty() || level == null || level <= 0) {
                continue;
            }

            Aptitude aptitude = RegistryAptitudes.getAptitude(aptitudeId);
            if (aptitude == null || !aptitude.isEnabled()) {
                continue;
            }

            aptitudesList.add(new Aptitudes(aptitudeId.toLowerCase(Locale.ROOT), itemId, false, aptitude, level));
        }

        return aptitudesList;
    }

    private static Map<String, List<Aptitudes>> getMergedAptitudes() {
        Map<String, List<Aptitudes>> aptitudeMap = LockItemAPI.isClearDefaults() ? new HashMap<>() : getAptitude();

        // Apply mod locks as baseline rules.
        LockItemAPI.getModLocks().forEach((modId, requirements) -> {
            for (Item item : ForgeRegistries.ITEMS.getValues()) {
                ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
                if (itemId == null || !itemId.getNamespace().equalsIgnoreCase(modId)) {
                    continue;
                }

                String itemKey = itemId.toString().toLowerCase(Locale.ROOT);
                List<Aptitudes> mapped = mapRequirements(itemKey, requirements);
                if (!mapped.isEmpty()) {
                    aptitudeMap.put(itemKey, mapped);
                }
            }
        });

        // Remove locks explicitly removed by scripts.
        LockItemAPI.getRemovedLocks().forEach(lock -> aptitudeMap.remove(lock.toLowerCase(Locale.ROOT)));

        // Item-specific locks override everything else.
        LockItemAPI.getItemLocks().forEach((itemId, requirements) -> {
            String normalizedItem = itemId.toLowerCase(Locale.ROOT);
            List<Aptitudes> mapped = mapRequirements(normalizedItem, requirements);
            if (mapped.isEmpty()) {
                aptitudeMap.remove(normalizedItem);
            } else {
                aptitudeMap.put(normalizedItem, mapped);
            }
        });

        return aptitudeMap;
    }

    public static void ForceRefresh(){
        HandlerLockItemsConfig.HANDLER.load();
        Aptitudes = getMergedAptitudes();
    }

    public static List<Aptitudes> getValue(String key) {
        if (Aptitudes == null) {
            Aptitudes = getMergedAptitudes();
        }

        if (key == null || key.isEmpty()) {
            return null;
        }

        return Aptitudes.get(key.toLowerCase(Locale.ROOT));
    }

    public static List<Aptitudes> getValue(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return null;
        }

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
        if (itemId == null) {
            return null;
        }

        String itemKey = itemId.toString().toLowerCase(Locale.ROOT);
        Map<String, Integer> mergedRequirements = new LinkedHashMap<>();

        List<Aptitudes> plainRequirements = getValue(itemKey);
        if (plainRequirements != null) {
            for (Aptitudes requirement : plainRequirements) {
                if (requirement.getAptitude() != null && requirement.getAptitudeLvl() > 0) {
                    mergedRequirements.merge(requirement.getAptitude().getName(), requirement.getAptitudeLvl(), Math::max);
                }
            }
        }

        Map<String, Integer> nbtRequirements = NBTLockAPI.getMatchingRequirements(itemStack);
        nbtRequirements.forEach((aptitudeName, level) -> mergedRequirements.merge(aptitudeName, level, Math::max));

        return mergedRequirements.isEmpty() ? null : mapRequirements(itemKey, mergedRequirements);
    }

    public static List<LockItem> getResolvedLockItems() {
        Map<String, List<Aptitudes>> merged = getMergedAptitudes();
        List<String> sortedKeys = new ArrayList<>(merged.keySet());
        Collections.sort(sortedKeys);

        List<LockItem> resolved = new ArrayList<>();
        for (String itemId : sortedKeys) {
            List<Aptitudes> requirements = merged.get(itemId);
            if (requirements == null || requirements.isEmpty()) {
                continue;
            }

            LockItem lockItem = new LockItem(itemId);
            List<LockItem.Aptitude> lockAptitudes = new ArrayList<>();
            for (Aptitudes requirement : requirements) {
                lockAptitudes.add(new LockItem.Aptitude(requirement.getKey(), requirement.getAptitudeLvl()));
            }
            lockItem.Aptitudes = lockAptitudes;
            resolved.add(lockItem);
        }

        return resolved;
    }

    public static List<String> defaultLockItemList = Arrays.asList("minecraft:anvil#building:12", "minecraft:chipped_anvil#building:12", "minecraft:damaged_anvil#building:12", "minecraft:brewing_stand#building:12;magic:12;intelligence:12", "minecraft:enchanting_table#magic:12", "minecraft:beacon#building:20", "minecraft:end_crystal#magic:30;building:24", "minecraft:ender_chest#magic:20", "minecraft:respawn_anchor#magic:20", "minecraft:shulker_box#magic:20", "minecraft:white_shulker_box#magic:20", "minecraft:light_gray_shulker_box#magic:20", "minecraft:gray_shulker_box#magic:20", "minecraft:black_shulker_box#magic:20", "minecraft:brown_shulker_box#magic:20", "minecraft:red_shulker_box#magic:20", "minecraft:orange_shulker_box#magic:20", "minecraft:yellow_shulker_box#magic:20", "minecraft:lime_shulker_box#magic:20", "minecraft:green_shulker_box#magic:20", "minecraft:cyan_shulker_box#magic:20", "minecraft:light_blue_shulker_box#magic:20", "minecraft:blue_shulker_box#magic:20", "minecraft:purple_shulker_box#magic:20", "minecraft:magenta_shulker_box#magic:20", "minecraft:pink_shulker_box#magic:20", "minecraft:dragon_egg#magic:30", "minecraft:wither_skeleton_skull#magic:8;building:8", "minecraft:lodestone#building:16;intelligence:8", "minecraft:smithing_table#building:20;intelligence:16", "minecraft:grindstone#building:16;intelligence:16", "minecraft:cartography_table#building:12;intelligence:12", "minecraft:stonecutter#building:6;strength:6", "minecraft:smoker#building:6", "minecraft:blast_furnace#building:6", "minecraft:loom#building:8;intelligence:8", "minecraft:name_tag#intelligence:10", "minecraft:fishing_rod#luck:4", "minecraft:bone_meal#luck:12", "minecraft:shears#building:4", "minecraft:lead#intelligence:4", "minecraft:spyglass#intelligence:4;dexterity:4", "minecraft:brush#intelligence:12", "minecraft:fire_charge#intelligence:4", "minecraft:flint_and_steel#intelligence:6", "minecraft:redstone#intelligence:4", "minecraft:redstone_torch#intelligence:4", "minecraft:repeater#intelligence:4", "minecraft:comparator#intelligence:4", "minecraft:writable_book#intelligence:6", "minecraft:written_book#intelligence:6", "minecraft:tnt#intelligence:12", "minecraft:lectern#intelligence:6;building:4", "minecraft:ender_pearl#magic:8", "minecraft:ender_eye#magic:16", "minecraft:bow#dexterity:4;strength:2", "minecraft:crossbow#dexterity:6;strength:4", "minecraft:saddle#dexterity:6", "minecraft:elytra#dexterity:30", "minecraft:firework_rocket#dexterity:20;intelligence:20", "minecraft:experience_bottle#magic:12;luck:10", "minecraft:wheat_seeds#intelligence:6", "minecraft:cocoa_beans#intelligence:6", "minecraft:pumpkin_seeds#intelligence:6", "minecraft:melon_seeds#intelligence:6", "minecraft:beetroot_seeds#intelligence:6", "minecraft:torchflower_seeds#intelligence:6", "minecraft:pitcher_pod#intelligence:6", "minecraft:glow_berries#intelligence:6", "minecraft:sweet_berries#intelligence:6", "minecraft:nether_wart:#intelligence:10;magic:8", "minecraft:egg#constitution:4", "minecraft:frogspawn#intelligence:12;constitution:16", "minecraft:turtle_egg#intelligence:12;constitution:16", "minecraft:sniffer_egg#intelligence:12;constitution:16", "minecraft:oak_sapling#intelligence:8", "minecraft:spruce_sapling#intelligence:8", "minecraft:birch_sapling#intelligence:8", "minecraft:jungle_sapling#intelligence:8", "minecraft:acacia_sapling#intelligence:8", "minecraft:dark_oak_sapling#intelligence:8", "minecraft:mangrove_propagule#intelligence:8", "minecraft:cherry_sapling#intelligence:8", "minecraft:azalea#intelligence:8", "minecraft:flowering_azalea#intelligence:8", "minecraft:brown_mushroom#intelligence:8", "minecraft:red_mushroom#intelligence:8", "minecraft:crimson_fungus#intelligence:8", "minecraft:warped_fungus#intelligence:8", "minecraft:bamboo#intelligence:8", "minecraft:sugar_cane#intelligence:8", "minecraft:cactus#intelligence:8", "minecraft:chorus_plant#intelligence:12", "minecraft:chorus_flower#intelligence:12", "minecraft:shield#defense:4;constitution:4", "minecraft:chainmail_helmet#defense:4", "minecraft:chainmail_chestplate#defense:4", "minecraft:chainmail_leggings#defense:4", "minecraft:chainmail_boots#defense:4", "minecraft:iron_helmet#defense:8", "minecraft:iron_chestplate#defense:8", "minecraft:iron_leggings#defense:8", "minecraft:iron_boots#defense:8", "minecraft:golden_helmet#defense:6;magic:6", "minecraft:golden_chestplate#defense:6;magic:6", "minecraft:golden_leggings#defense:6;magic:6", "minecraft:golden_boots#defense:6;magic:6", "minecraft:diamond_helmet#defense:16", "minecraft:diamond_chestplate#defense:16", "minecraft:diamond_leggings#defense:16", "minecraft:diamond_boots#defense:16", "minecraft:netherite_helmet#defense:24", "minecraft:netherite_chestplate#defense:24", "minecraft:netherite_leggings#defense:24", "minecraft:netherite_boots#defense:24", "minecraft:turtle_helmet#defense:6;dexterity:6", "minecraft:golden_horse_armor#defense:4;dexterity:4", "minecraft:iron_horse_armor#defense:6;dexterity:6", "minecraft:diamond_horse_armor#defense:12;dexterity:12", "minecraft:totem_of_undying#constitution:16;magic:12<droppable>", "minecraft:trident#strength:20;dexterity:18", "minecraft:iron_hoe#building:8", "minecraft:iron_shovel#building:8", "minecraft:iron_pickaxe#building:8", "minecraft:iron_axe#strength:8;building:8", "minecraft:iron_sword#strength:8", "minecraft:golden_hoe#building:6", "minecraft:golden_shovel#building:6", "minecraft:golden_pickaxe#building:6", "minecraft:golden_axe#building:6;strength:6", "minecraft:golden_sword#strength:6", "minecraft:diamond_hoe#building:16", "minecraft:diamond_shovel#building:16", "minecraft:diamond_pickaxe#building:16", "minecraft:diamond_axe#strength:16;building:16", "minecraft:diamond_sword#strength:16", "minecraft:netherite_hoe#building:24", "minecraft:netherite_shovel#building:24", "minecraft:netherite_pickaxe#building:24", "minecraft:netherite_axe#strength:24;building:24", "minecraft:netherite_sword#strength:24", "minecraft:honey_bottle#constitution:4", "minecraft:potion#luck:4", "minecraft:splash_potion#luck:6;dexterity:6", "minecraft:lingering_potion#luck:6;magic:6");
}


