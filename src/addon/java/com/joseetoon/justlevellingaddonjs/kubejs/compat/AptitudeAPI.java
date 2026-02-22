package com.joseetoon.justlevellingaddonjs.kubejs.compat;

import com.joseetoon.justlevellingaddonjs.compat.AptitudeCompat;
import com.joseetoon.justlevellingaddonjs.compat.base121.BackportRegistryState;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Method;
import java.util.Locale;

public class AptitudeAPI {
    private static final String DEFAULT_BACKGROUND = "minecraft:textures/block/stone.png";

    public static Aptitude add(String name, Object... args) {
        ResourceLocation id = AddonResourceUtils.parseResourceLocation(name, true);
        if (id == null) {
            throw new IllegalArgumentException("Invalid aptitude name: " + name);
        }
        return addWithId(id, args);
    }

    public static Aptitude addWithId(String nameOrId, Object... args) {
        ResourceLocation id = AddonResourceUtils.parseResourceLocation(nameOrId, false);
        if (id == null) {
            throw new IllegalArgumentException("Invalid aptitude id: " + nameOrId);
        }
        return addWithId(id, args);
    }

    private static Aptitude addWithId(ResourceLocation id, Object... args) {
        if (id == null || id.getPath().isBlank()) {
            throw new IllegalArgumentException("Invalid aptitude id: " + id);
        }

        if (args == null || args.length == 0) {
            String icon = defaultIcon(id);
            return invokeAddWithId(id, DEFAULT_BACKGROUND, null, icon, icon, icon, icon);
        }

        if (!(args[0] instanceof String background) || background.isBlank()) {
            throw new IllegalArgumentException("Background texture cannot be empty for aptitude: " + id);
        }

        int next = 1;
        Integer backgroundRepeat = null;
        if (next < args.length && args[next] instanceof Number number) {
            backgroundRepeat = number.intValue();
            next++;
        }

        String[] lockedTextures = new String[Math.max(0, args.length - next)];
        for (int i = next; i < args.length; i++) {
            Object texture = args[i];
            if (texture == null) {
                throw new IllegalArgumentException("Locked texture cannot be null for aptitude: " + id);
            }
            lockedTextures[i - next] = texture.toString();
        }

        if (backgroundRepeat == null) {
            return invokeAddWithId(id, background, null, lockedTextures);
        }
        return invokeAddWithId(id, background, backgroundRepeat, lockedTextures);
    }

    public static Aptitude getByName(String nameOrId) {
        Method byName;
        try {
            byName = Aptitude.class.getMethod("getByName", String.class);
            Object value = byName.invoke(null, nameOrId);
            if (value instanceof Aptitude aptitude) {
                return aptitude;
            }
        } catch (Throwable ignored) {
        }

        Aptitude fromRegistry = RegistryAptitudes.getAptitude(nameOrId);
        if (fromRegistry != null) {
            return fromRegistry;
        }

        ResourceLocation parsed = AddonResourceUtils.parseResourceLocation(nameOrId, false);
        String normalizedPath = parsed != null ? parsed.getPath() : BackportRegistryState.normalizePath(nameOrId);
        return BackportRegistryState.findPendingAptitude(parsed, normalizedPath);
    }

    public static void setBackground(Aptitude aptitude, String backgroundTexture) {
        if (aptitude == null) {
            throw new IllegalArgumentException("Aptitude cannot be null");
        }
        if (backgroundTexture == null || backgroundTexture.isBlank()) {
            throw new IllegalArgumentException("Background texture must be a non-empty resource location string (namespace:path)");
        }

        ResourceLocation resourceLocation;
        try {
            resourceLocation = new ResourceLocation(backgroundTexture.toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid background texture resource location: " + backgroundTexture, exception);
        }
        AptitudeCompat.setBackground(aptitude, resourceLocation);
    }

    private static Aptitude invokeAddWithId(ResourceLocation id, String background, Integer backgroundRepeat, String... lockedTextures) {
        Aptitude reflected = null;
        try {
            if (backgroundRepeat == null) {
                Method method = Aptitude.class.getMethod("addWithId", ResourceLocation.class, String.class, String[].class);
                Object value = method.invoke(null, id, background, lockedTextures);
                if (value instanceof Aptitude aptitude) {
                    reflected = aptitude;
                }
            } else {
                Method method = Aptitude.class.getMethod("addWithId", ResourceLocation.class, String.class, int.class, String[].class);
                Object value = method.invoke(null, id, background, backgroundRepeat, lockedTextures);
                if (value instanceof Aptitude aptitude) {
                    reflected = aptitude;
                }
            }
        } catch (Throwable ignored) {
        }

        if (reflected != null) {
            if (backgroundRepeat != null) {
                AptitudeCompat.setBackgroundRepeat(reflected, backgroundRepeat);
            }
            // Keep local pending visibility for startup-time lookups, even when base handles registration later.
            BackportRegistryState.addPendingAptitude(id, reflected);
            return reflected;
        }

        String[] effectiveLockedTextures = (lockedTextures == null || lockedTextures.length == 0)
                ? new String[]{defaultIcon(id)}
                : lockedTextures;

        ResourceLocation[] textures = new ResourceLocation[4];
        for (int i = 0; i < 4; i++) {
            int idx = Math.min(i, effectiveLockedTextures.length - 1);
            textures[i] = new ResourceLocation(effectiveLockedTextures[idx].toLowerCase(Locale.ROOT));
        }

        int nextIndex = BackportRegistryState.nextAptitudeIndex(RegistryAptitudes.APTITUDES_REGISTRY.get());
        Aptitude aptitude = new Aptitude(nextIndex, id, textures, new ResourceLocation(background.toLowerCase(Locale.ROOT)));
        if (backgroundRepeat != null) {
            AptitudeCompat.setBackgroundRepeat(aptitude, backgroundRepeat);
        }
        BackportRegistryState.addPendingAptitude(id, aptitude);
        return aptitude;
    }

    private static String defaultIcon(ResourceLocation id) {
        return id.getNamespace() + ":textures/skills/" + id.getPath() + ".png";
    }
}
