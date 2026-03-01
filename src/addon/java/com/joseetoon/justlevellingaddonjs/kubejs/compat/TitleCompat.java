package com.joseetoon.justlevellingaddonjs.kubejs.compat;

import com.joseetoon.justlevellingaddonjs.compat.base121.BackportRegistryState;
import com.joseetoon.justlevellingaddonjs.config.AddonCommonConfig;
import com.joseetoon.justlevellingaddonjs.kubejs.TitleAPI;
import com.seniors.justlevelingfork.JustLevelingFork;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import com.seniors.justlevelingfork.registry.title.Title;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Method;
import java.util.Locale;

public final class TitleCompat {
    private TitleCompat() {
    }

    public static Title add(String name) {
        return add(name, false, false);
    }

    public static Title add(String name, boolean defaultUnlocked, boolean hideRequirements) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Title name can't be null or empty");
        }

        String normalized = BackportRegistryState.normalizePath(name);
        if (normalized == null) {
            throw new IllegalArgumentException("Title name can't be null or empty");
        }

        BackportRegistryState.unmarkTitleDeleted(normalized);
        if (AddonCommonConfig.kubejsTitlesServerManagedByDefault.get()) {
            BackportRegistryState.markTitleServerManaged(normalized);
        }

        Title existing = getByName(normalized);
        if (existing != null) {
            BackportRegistryState.setTitleMetaOverride(normalized, defaultUnlocked, hideRequirements);
            return existing;
        }

        try {
            Method add = Title.class.getMethod("add", String.class, boolean.class, boolean.class);
            Object value = add.invoke(null, normalized, defaultUnlocked, hideRequirements);
            if (value instanceof Title title) {
                BackportRegistryState.setTitleMetaOverride(normalized, defaultUnlocked, hideRequirements);
                return title;
            }
        } catch (Throwable ignored) {
        }

        Title title = new Title(new ResourceLocation(JustLevelingFork.MOD_ID, normalized), defaultUnlocked, hideRequirements);
        BackportRegistryState.addPendingTitle(normalized, title);
        BackportRegistryState.setTitleMetaOverride(normalized, defaultUnlocked, hideRequirements);
        return title;
    }

    public static Title getByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        String normalized = BackportRegistryState.normalizePath(name);
        if (BackportRegistryState.isTitleDeleted(normalized)) {
            return null;
        }

        try {
            Method byName = Title.class.getMethod("getByName", String.class);
            Object value = byName.invoke(null, name);
            if (value instanceof Title title) {
                return title;
            }
        } catch (Throwable ignored) {
        }

        Title fromRegistry = RegistryTitles.getTitle(name.toLowerCase(Locale.ROOT));
        if (fromRegistry != null) {
            return fromRegistry;
        }
        return BackportRegistryState.findPendingTitle(name);
    }

    public static boolean remove(String name) {
        return TitleAPI.remove(name);
    }

    public static boolean restore(String name) {
        return TitleAPI.restore(name);
    }

    public static boolean update(String name, boolean defaultUnlocked, boolean hideRequirements) {
        return TitleAPI.update(name, defaultUnlocked, hideRequirements);
    }

    public static boolean setDisplayNameOverride(String name, String displayName) {
        return TitleAPI.setDisplayNameOverride(name, displayName);
    }

    public static boolean setDescriptionOverride(String name, String description) {
        return TitleAPI.setDescriptionOverride(name, description);
    }

    public static boolean clearTextOverrides(String name) {
        return TitleAPI.clearTextOverrides(name);
    }

    public static boolean setOverheadColor(String name, String hexRgb) {
        return TitleAPI.setOverheadColor(name, hexRgb);
    }

    public static boolean clearOverheadColor(String name) {
        return TitleAPI.clearOverheadColor(name);
    }

    public static String getOverheadColor(String name) {
        return TitleAPI.getOverheadColor(name);
    }
}
