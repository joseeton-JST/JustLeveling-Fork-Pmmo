package com.joseetoon.justlevellingaddonjs.mixin;

import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

@Mixin(value = Aptitude.class, remap = false)
public abstract class MixAptitudeBackgroundStringAlias {
    public void setBackgroundTexture(String backgroundTexture) {
        if (backgroundTexture == null || backgroundTexture.isBlank()) {
            throw new IllegalArgumentException("Background texture must be a non-empty resource location string (namespace:path)");
        }

        ResourceLocation parsedTexture;
        try {
            parsedTexture = new ResourceLocation(backgroundTexture.toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid background texture resource location: " + backgroundTexture, exception);
        }

        Aptitude self = (Aptitude) (Object) this;
        if (invokeSetBackground(self, parsedTexture)) {
            return;
        }

        if (writeBackgroundField(self, parsedTexture)) {
            return;
        }

        throw new IllegalStateException("Unable to set aptitude background on this JustLevelingFork version. Missing setBackground(ResourceLocation) and background field.");
    }

    private static boolean invokeSetBackground(Aptitude aptitude, ResourceLocation background) {
        try {
            Method method = aptitude.getClass().getMethod("setBackground", ResourceLocation.class);
            method.invoke(aptitude, background);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean writeBackgroundField(Aptitude aptitude, ResourceLocation background) {
        try {
            Field field = findField(aptitude.getClass(), "background");
            if (field == null) {
                return false;
            }
            field.setAccessible(true);
            field.set(aptitude, background);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Field findField(Class<?> type, String fieldName) {
        Class<?> cursor = type;
        while (cursor != null) {
            try {
                return cursor.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                cursor = cursor.getSuperclass();
            }
        }
        return null;
    }
}
