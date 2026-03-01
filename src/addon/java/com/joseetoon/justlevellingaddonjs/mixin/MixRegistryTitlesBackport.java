package com.joseetoon.justlevellingaddonjs.mixin;

import com.joseetoon.justlevellingaddonjs.compat.base121.BackportRegistryState;
import com.seniors.justlevelingfork.JustLevelingFork;
import com.seniors.justlevelingfork.registry.RegistryCapabilities;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import com.seniors.justlevelingfork.registry.title.Title;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.Supplier;

@Mixin(value = RegistryTitles.class, remap = false)
public abstract class MixRegistryTitlesBackport {
    @Shadow
    public static ResourceKey<Registry<Title>> TITLES_KEY;

    @Shadow
    public static Supplier<IForgeRegistry<Title>> TITLES_REGISTRY;

    @Inject(method = "load", at = @At("TAIL"), require = 0)
    private static void jlforkaddon$load(IEventBus eventBus, CallbackInfo ci) {
        eventBus.addListener((RegisterEvent event) -> event.register(TITLES_KEY, helper ->
                BackportRegistryState.pendingTitlesSnapshot().forEach((name, title) ->
                        helper.register(new ResourceLocation(JustLevelingFork.MOD_ID, name), title))));
    }

    @Inject(method = "getTitle", at = @At("HEAD"), cancellable = true, require = 0)
    private static void jlforkaddon$getTitle(String titleName, CallbackInfoReturnable<Title> cir) {
        if (cir.isCancelled()) {
            return;
        }

        String normalized = BackportRegistryState.normalizePath(titleName);
        if (normalized == null) {
            cir.setReturnValue(null);
            return;
        }

        if (BackportRegistryState.isTitleDeleted(normalized)
                || BackportRegistryState.isTitleDisabledByAddonConfig(normalized)) {
            cir.setReturnValue(null);
            return;
        }

        Title pending = BackportRegistryState.findPendingTitle(normalized);
        if (pending != null) {
            cir.setReturnValue(pending);
            return;
        }

        IForgeRegistry<Title> registry = TITLES_REGISTRY != null ? TITLES_REGISTRY.get() : null;
        if (registry == null) {
            cir.setReturnValue(null);
            return;
        }

        ResourceLocation parsedId = ResourceLocation.tryParse(titleName.toLowerCase(Locale.ROOT));
        if (parsedId != null) {
            Title byId = registry.getValue(parsedId);
            if (byId != null) {
                cir.setReturnValue(byId);
                return;
            }
        }

        Title byPath = registry.getValues().stream()
                .filter(title -> title.getName().equalsIgnoreCase(normalized))
                .findFirst()
                .orElse(null);
        cir.setReturnValue(byPath);
    }

    @Inject(method = "serverPlayerTitles", at = @At("TAIL"), require = 0)
    private static void jlforkaddon$serverPlayerTitles(ServerPlayer serverPlayer, CallbackInfo ci) {
        if (serverPlayer == null || serverPlayer.isDeadOrDying()) {
            return;
        }

        serverPlayer.getCapability(RegistryCapabilities.APTITUDE).ifPresent(capability -> {
            for (var entry : new ArrayList<>(BackportRegistryState.titleConditionsSnapshot().entrySet())) {
                if (BackportRegistryState.isTitleDeleted(entry.getKey())) {
                    continue;
                }
                Title title = RegistryTitles.getTitle(entry.getKey());
                if (title == null) {
                    continue;
                }
                if (BackportRegistryState.isTitleDeleted(title.getName())
                        || BackportRegistryState.isTitleDisabledByAddonConfig(title.getName())
                        || BackportRegistryState.isTitleServerManaged(title.getName())) {
                    continue;
                }

                boolean meetsRequirements = jlforkaddon$checkTitleRequirements(title, entry.getValue(), serverPlayer);
                title.setRequirement(serverPlayer, meetsRequirements);
            }
        });
    }

    @Unique
    private static boolean jlforkaddon$checkTitleRequirements(Title title, List<String> conditions, ServerPlayer player) {
        if (title == null || player == null) {
            return false;
        }

        try {
            Class<?> modelClass = Class.forName("com.seniors.justlevelingfork.config.models.TitleModel");
            Object model = jlforkaddon$createTitleModel(modelClass, title, conditions);
            if (model == null) {
                return title.getRequirement(player);
            }

            Method method = modelClass.getMethod("CheckRequirements", ServerPlayer.class);
            Object result = method.invoke(model, player);
            if (result instanceof Boolean bool) {
                return bool;
            }
        } catch (Throwable ignored) {
        }

        return title.getRequirement(player);
    }

    @Unique
    private static Object jlforkaddon$createTitleModel(Class<?> modelClass, Title title, List<String> conditions) {
        List<String> safeConditions = conditions != null ? new ArrayList<>(conditions) : List.of();

        try {
            Constructor<?> c4 = modelClass.getConstructor(String.class, List.class, boolean.class, boolean.class);
            return c4.newInstance(title.getName(), safeConditions, title.Requirement, title.HideRequirements);
        } catch (Throwable ignored) {
        }

        try {
            Constructor<?> c3 = modelClass.getConstructor(String.class, List.class, boolean.class);
            return c3.newInstance(title.getName(), safeConditions, title.Requirement);
        } catch (Throwable ignored) {
        }

        return null;
    }
}
