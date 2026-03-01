package com.joseetoon.justlevellingaddonjs.mixin;

import com.joseetoon.justlevellingaddonjs.compat.base121.BackportRegistryState;
import com.joseetoon.justlevellingaddonjs.compat.base121.Base121Bridge;
import com.joseetoon.justlevellingaddonjs.compat.base121.TitleBlockState;
import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import com.seniors.justlevelingfork.network.packet.client.SyncAptitudeCapabilityCP;
import com.seniors.justlevelingfork.registry.RegistryCapabilities;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import com.seniors.justlevelingfork.registry.title.Title;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.registries.IForgeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.Supplier;

@Mixin(value = RegistryTitles.class, remap = false)
public abstract class MixRegistryTitlesCrudControls {
    @Shadow
    public static Supplier<IForgeRegistry<Title>> TITLES_REGISTRY;

    @Inject(method = "getTitle", at = @At("HEAD"), cancellable = true, require = 0)
    private static void jlforkaddon$guardDeletedTitles(String titleName, CallbackInfoReturnable<Title> cir) {
        String normalized = BackportRegistryState.normalizePath(titleName);
        if (normalized == null) {
            return;
        }

        IForgeRegistry<Title> registry = TITLES_REGISTRY != null ? TITLES_REGISTRY.get() : null;
        if ("titleless".equals(normalized)) {
            BackportRegistryState.ensureTitlelessPresent(registry);
            return;
        }

        if (BackportRegistryState.isTitleDeleted(normalized)
                || BackportRegistryState.isTitleDisabledByAddonConfig(normalized)) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "serverPlayerTitles", at = @At("TAIL"), require = 0)
    private static void jlforkaddon$enforceDeletedAndOverrides(ServerPlayer serverPlayer, CallbackInfo ci) {
        if (serverPlayer == null || serverPlayer.isDeadOrDying() || TITLES_REGISTRY == null) {
            return;
        }

        IForgeRegistry<Title> registry = TITLES_REGISTRY.get();
        if (registry == null) {
            return;
        }

        Map<String, List<String>> titleConfigConditions = new HashMap<>();
        Map<String, Boolean> titleConfigDefaults = new HashMap<>();
        jlforkaddon$loadConfigTitleConditions(titleConfigConditions, titleConfigDefaults);

        Map<String, List<String>> backportConditions = BackportRegistryState.titleConditionsSnapshot();

        serverPlayer.getCapability(RegistryCapabilities.APTITUDE).ifPresent(capability -> {
            boolean dirty = false;
            dirty |= jlforkaddon$migrateServerManagedOnce(serverPlayer, capability, registry);

            for (Title title : registry.getValues()) {
                if (title == null) {
                    continue;
                }

                String normalized = BackportRegistryState.normalizePath(title.getName());
                if (normalized == null) {
                    continue;
                }

                boolean disabledByConfig = BackportRegistryState.isTitleDisabledByAddonConfig(normalized);
                if (BackportRegistryState.isTitleDeleted(normalized) || disabledByConfig) {
                    if (capability.getLockTitle(title)) {
                        capability.setUnlockTitle(title, false);
                        dirty = true;
                    }
                    continue;
                }

                if (TitleBlockState.isBlocked(serverPlayer, normalized)) {
                    if (capability.getLockTitle(title)) {
                        capability.setUnlockTitle(title, false);
                        dirty = true;
                    }
                    continue;
                }

                if (BackportRegistryState.isTitleServerManaged(normalized)) {
                    continue;
                }

                List<String> conditions = backportConditions.get(normalized);
                Boolean baseDefault = null;
                if (conditions == null) {
                    conditions = titleConfigConditions.get(normalized);
                    baseDefault = titleConfigDefaults.get(normalized);
                }

                BackportRegistryState.TitleMetaOverride override = BackportRegistryState.getTitleMetaOverride(normalized);
                boolean effectiveDefault = override != null
                        ? override.defaultUnlocked()
                        : (baseDefault != null ? baseDefault : title.Requirement);

                if (conditions != null) {
                    boolean meets = jlforkaddon$checkTitleRequirements(title, conditions, effectiveDefault, serverPlayer);
                    if (capability.getLockTitle(title) != meets) {
                        capability.setUnlockTitle(title, meets);
                        dirty = true;
                    }
                } else if (override != null) {
                    if (capability.getLockTitle(title) != effectiveDefault) {
                        capability.setUnlockTitle(title, effectiveDefault);
                        dirty = true;
                    }
                } else if (effectiveDefault && !capability.getLockTitle(title)) {
                    capability.setUnlockTitle(title, true);
                    dirty = true;
                }
            }

            String currentTitleName = capability.getPlayerTitle();
            if (BackportRegistryState.isTitleDeleted(currentTitleName)
                    || BackportRegistryState.isTitleDisabledByAddonConfig(currentTitleName)
                    || TitleBlockState.isBlocked(serverPlayer, currentTitleName)) {
                Title titleless = BackportRegistryState.ensureTitlelessPresent(registry);
                if (titleless != null) {
                    if (!"titleless".equals(currentTitleName.toLowerCase(Locale.ROOT))) {
                        capability.setPlayerTitle(titleless);
                        dirty = true;
                    }
                    if (!capability.getLockTitle(titleless)) {
                        capability.setUnlockTitle(titleless, true);
                        dirty = true;
                    }
                    serverPlayer.setCustomName(Base121Bridge.titleDisplayComponentOrFallback(titleless));
                    serverPlayer.refreshDisplayName();
                    serverPlayer.refreshTabListName();
                }
            }

            if (dirty) {
                SyncAptitudeCapabilityCP.send(serverPlayer);
            }
        });
    }

    private static boolean jlforkaddon$migrateServerManagedOnce(ServerPlayer serverPlayer, AptitudeCapability capability, IForgeRegistry<Title> registry) {
        if (serverPlayer == null || capability == null || registry == null) {
            return false;
        }

        if (serverPlayer.getPersistentData().getBoolean(BackportRegistryState.SERVER_MANAGED_MIGRATION_FLAG)) {
            return false;
        }

        boolean dirty = false;
        for (Title title : registry.getValues()) {
            if (title == null) {
                continue;
            }

            String normalized = BackportRegistryState.normalizePath(title.getName());
            if (normalized == null || !BackportRegistryState.isTitleServerManaged(normalized)) {
                continue;
            }

            boolean effectiveDefault = BackportRegistryState.getEffectiveDefaultUnlocked(title);
            if (capability.getLockTitle(title) != effectiveDefault) {
                capability.setUnlockTitle(title, effectiveDefault);
                dirty = true;
            }
        }

        serverPlayer.getPersistentData().putBoolean(BackportRegistryState.SERVER_MANAGED_MIGRATION_FLAG, true);
        return dirty;
    }

    private static boolean jlforkaddon$checkTitleRequirements(Title title, List<String> conditions, boolean defaultUnlocked, ServerPlayer player) {
        if (title == null || player == null) {
            return false;
        }

        try {
            Class<?> modelClass = Class.forName("com.seniors.justlevelingfork.config.models.TitleModel");
            Method check = modelClass.getMethod("checkRequirements", String.class, List.class, boolean.class, ServerPlayer.class);
            Object result = check.invoke(null, title.getName(), conditions, defaultUnlocked, player);
            if (result instanceof Boolean value) {
                return value;
            }
        } catch (Throwable ignored) {
        }

        try {
            Class<?> modelClass = Class.forName("com.seniors.justlevelingfork.config.models.TitleModel");
            Method check = modelClass.getMethod("CheckRequirements", String.class, List.class, boolean.class, ServerPlayer.class);
            Object result = check.invoke(null, title.getName(), conditions, defaultUnlocked, player);
            if (result instanceof Boolean value) {
                return value;
            }
        } catch (Throwable ignored) {
        }

        try {
            Class<?> modelClass = Class.forName("com.seniors.justlevelingfork.config.models.TitleModel");
            Object model = jlforkaddon$createTitleModel(modelClass, title, conditions, defaultUnlocked);
            if (model != null) {
                Method check = modelClass.getMethod("CheckRequirements", ServerPlayer.class);
                Object result = check.invoke(model, player);
                if (result instanceof Boolean value) {
                    return value;
                }
            }
        } catch (Throwable ignored) {
        }

        return defaultUnlocked;
    }

    private static Object jlforkaddon$createTitleModel(Class<?> modelClass, Title title, List<String> conditions, boolean defaultUnlocked) {
        List<String> safeConditions = conditions != null ? new ArrayList<>(conditions) : List.of();

        try {
            Constructor<?> ctor4 = modelClass.getConstructor(String.class, List.class, boolean.class, boolean.class);
            return ctor4.newInstance(title.getName(), safeConditions, defaultUnlocked, title.HideRequirements);
        } catch (Throwable ignored) {
        }

        try {
            Constructor<?> ctor3 = modelClass.getConstructor(String.class, List.class, boolean.class);
            return ctor3.newInstance(title.getName(), safeConditions, defaultUnlocked);
        } catch (Throwable ignored) {
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static void jlforkaddon$loadConfigTitleConditions(Map<String, List<String>> conditionMap, Map<String, Boolean> defaultMap) {
        try {
            Class<?> handlerClass = Class.forName("com.seniors.justlevelingfork.handler.HandlerTitlesConfig");
            Object holder = handlerClass.getField("HANDLER").get(null);
            if (holder == null) {
                return;
            }

            Method instanceMethod = holder.getClass().getMethod("instance");
            Object instance = instanceMethod.invoke(holder);
            if (instance == null) {
                return;
            }

            Object titleListObj = instance.getClass().getField("titleList").get(instance);
            if (!(titleListObj instanceof Iterable<?> titleModels)) {
                return;
            }

            for (Object model : titleModels) {
                if (model == null) {
                    continue;
                }

                Object titleIdObj = model.getClass().getField("TitleId").get(model);
                String titleId = titleIdObj instanceof String s ? s : null;
                String normalized = BackportRegistryState.normalizePath(titleId);
                if (normalized == null) {
                    continue;
                }

                Object defaultObj = model.getClass().getField("Default").get(model);
                boolean isDefault = defaultObj instanceof Boolean b && b;

                Object conditionsObj = model.getClass().getField("Conditions").get(model);
                List<String> conditions = List.of();
                if (conditionsObj instanceof List<?> rawConditions) {
                    List<String> filtered = new ArrayList<>();
                    for (Object entry : rawConditions) {
                        if (entry instanceof String text) {
                            filtered.add(text);
                        }
                    }
                    conditions = List.copyOf(filtered);
                }

                conditionMap.putIfAbsent(normalized, conditions);
                defaultMap.putIfAbsent(normalized, isDefault);
            }
        } catch (Throwable ignored) {
        }
    }
}
