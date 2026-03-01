package com.joseetoon.justlevellingaddonjs.compat.base121;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class TitleBlockState {
    private static final String ROOT_KEY = "justlevellingaddonjs";
    private static final String BLOCKED_TITLES_KEY = "blocked_titles";
    private static final String TITLELESS = "titleless";

    private TitleBlockState() {
    }

    public static boolean isBlocked(Player player, String titleName) {
        if (player == null) {
            return false;
        }

        String normalized = BackportRegistryState.normalizePath(titleName);
        if (normalized == null || TITLELESS.equals(normalized)) {
            return false;
        }

        ListTag list = getBlockedList(player, false);
        if (list == null) {
            return false;
        }

        for (Tag raw : list) {
            if (raw instanceof StringTag entry && normalized.equals(entry.getAsString())) {
                return true;
            }
        }
        return false;
    }

    public static boolean setBlocked(Player player, String titleName, boolean blocked) {
        if (player == null) {
            return false;
        }

        String normalized = BackportRegistryState.normalizePath(titleName);
        if (normalized == null || TITLELESS.equals(normalized)) {
            return false;
        }

        ListTag list = getBlockedList(player, blocked);
        if (list == null) {
            return false;
        }

        boolean hasEntry = false;
        for (Tag raw : list) {
            if (raw instanceof StringTag entry && normalized.equals(entry.getAsString())) {
                hasEntry = true;
                break;
            }
        }

        if (blocked) {
            if (hasEntry) {
                return false;
            }
            list.add(StringTag.valueOf(normalized));
            return true;
        }

        if (!hasEntry) {
            return false;
        }

        for (int i = list.size() - 1; i >= 0; i--) {
            if (normalized.equals(list.getString(i))) {
                list.remove(i);
            }
        }

        CompoundTag root = getRoot(player, false);
        if (root != null && list.isEmpty()) {
            root.remove(BLOCKED_TITLES_KEY);
        }
        return true;
    }

    public static List<String> getBlockedTitles(Player player) {
        if (player == null) {
            return List.of();
        }

        ListTag list = getBlockedList(player, false);
        if (list == null || list.isEmpty()) {
            return List.of();
        }

        Set<String> unique = new LinkedHashSet<>();
        for (Tag raw : list) {
            if (raw instanceof StringTag entry) {
                String normalized = BackportRegistryState.normalizePath(entry.getAsString());
                if (normalized != null && !TITLELESS.equals(normalized)) {
                    unique.add(normalized);
                }
            }
        }

        return unique.isEmpty() ? List.of() : new ArrayList<>(unique);
    }

    public static void clearBlockedTitles(Player player) {
        CompoundTag root = getRoot(player, false);
        if (root != null) {
            root.remove(BLOCKED_TITLES_KEY);
        }
    }

    private static ListTag getBlockedList(Player player, boolean create) {
        CompoundTag root = getRoot(player, create);
        if (root == null) {
            return null;
        }

        if (!root.contains(BLOCKED_TITLES_KEY, Tag.TAG_LIST)) {
            if (!create) {
                return null;
            }
            ListTag created = new ListTag();
            root.put(BLOCKED_TITLES_KEY, created);
            return created;
        }

        return root.getList(BLOCKED_TITLES_KEY, Tag.TAG_STRING);
    }

    private static CompoundTag getRoot(Player player, boolean create) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT_KEY, Tag.TAG_COMPOUND)) {
            if (!create) {
                return null;
            }
            persistent.put(ROOT_KEY, new CompoundTag());
        }
        return persistent.getCompound(ROOT_KEY);
    }
}
