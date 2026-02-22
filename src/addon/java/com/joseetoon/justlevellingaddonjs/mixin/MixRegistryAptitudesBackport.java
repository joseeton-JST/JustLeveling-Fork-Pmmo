package com.joseetoon.justlevellingaddonjs.mixin;

import com.joseetoon.justlevellingaddonjs.compat.base121.BackportRegistryState;
import com.seniors.justlevelingfork.JustLevelingFork;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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

import java.util.Locale;
import java.util.function.Supplier;

@Mixin(value = RegistryAptitudes.class, remap = false)
public abstract class MixRegistryAptitudesBackport {
    @Shadow
    private static ResourceKey<Registry<Aptitude>> APTITUDES_KEY;

    @Shadow
    public static Supplier<IForgeRegistry<Aptitude>> APTITUDES_REGISTRY;

    @Inject(method = "load", at = @At("TAIL"), require = 0)
    private static void jlforkaddon$load(IEventBus eventBus, CallbackInfo ci) {
        eventBus.addListener((RegisterEvent event) -> event.register(APTITUDES_KEY, helper ->
                BackportRegistryState.pendingAptitudesSnapshot().forEach(helper::register)));
    }

    @Inject(method = "getAptitude", at = @At("HEAD"), cancellable = true, require = 0)
    private static void jlforkaddon$getAptitude(String aptitudeNameOrId, CallbackInfoReturnable<Aptitude> cir) {
        if (aptitudeNameOrId == null || aptitudeNameOrId.isBlank()) {
            cir.setReturnValue(null);
            return;
        }

        ResourceLocation parsed = jlforkaddon$parseResourceLocation(aptitudeNameOrId);
        IForgeRegistry<Aptitude> registry = APTITUDES_REGISTRY != null ? APTITUDES_REGISTRY.get() : null;

        if (parsed != null) {
            Aptitude pendingById = BackportRegistryState.findPendingAptitude(parsed, null);
            if (pendingById != null) {
                cir.setReturnValue(pendingById);
                return;
            }
            if (registry != null) {
                Aptitude registeredById = registry.getValue(parsed);
                if (registeredById != null) {
                    cir.setReturnValue(registeredById);
                    return;
                }
            }
        }

        String normalizedPath = parsed != null
                ? parsed.getPath().toLowerCase(Locale.ROOT)
                : aptitudeNameOrId.toLowerCase(Locale.ROOT);

        Aptitude pendingByPath = BackportRegistryState.findPendingAptitude(parsed, normalizedPath);
        if (pendingByPath != null) {
            cir.setReturnValue(pendingByPath);
            return;
        }

        if (registry == null) {
            cir.setReturnValue(null);
            return;
        }

        Aptitude result = registry.getValues().stream()
                .filter(aptitude -> aptitude.getName().equalsIgnoreCase(normalizedPath))
                .findFirst()
                .orElse(null);
        cir.setReturnValue(result);
    }

    @Unique
    private static ResourceLocation jlforkaddon$parseResourceLocation(String raw) {
        String normalized = raw.toLowerCase(Locale.ROOT);
        try {
            return normalized.contains(":")
                    ? new ResourceLocation(normalized)
                    : new ResourceLocation(JustLevelingFork.MOD_ID, normalized);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
