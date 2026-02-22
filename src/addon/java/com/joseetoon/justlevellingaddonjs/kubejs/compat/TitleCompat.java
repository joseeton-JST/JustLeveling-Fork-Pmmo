package com.joseetoon.justlevellingaddonjs.kubejs.compat;

import com.joseetoon.justlevellingaddonjs.compat.base121.BackportRegistryState;
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

        String normalized = name.toLowerCase(Locale.ROOT);
        try {
            Method add = Title.class.getMethod("add", String.class, boolean.class, boolean.class);
            Object value = add.invoke(null, normalized, defaultUnlocked, hideRequirements);
            if (value instanceof Title title) {
                return title;
            }
        } catch (Throwable ignored) {
        }

        Title title = new Title(new ResourceLocation(JustLevelingFork.MOD_ID, normalized), defaultUnlocked, hideRequirements);
        BackportRegistryState.addPendingTitle(normalized, title);
        return title;
    }

    public static Title getByName(String name) {
        if (name == null || name.isBlank()) {
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
}
