package com.joseetoon.justlevellingaddonjs.mixin;

import com.joseetoon.justlevellingaddonjs.compat.base121.BackportRegistryState;
import com.seniors.justlevelingfork.JustLevelingFork;
import com.seniors.justlevelingfork.registry.RegistryPassives;
import com.seniors.justlevelingfork.registry.passive.Passive;
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

@Mixin(value = RegistryPassives.class, remap = false)
public abstract class MixRegistryPassivesBackport {
    @Shadow
    public static ResourceKey<Registry<Passive>> PASSIVES_KEY;

    @Shadow
    public static Supplier<IForgeRegistry<Passive>> PASSIVES_REGISTRY;

    @Inject(method = "load", at = @At("TAIL"), require = 0)
    private static void jlforkaddon$load(IEventBus eventBus, CallbackInfo ci) {
        eventBus.addListener((RegisterEvent event) -> event.register(PASSIVES_KEY, helper ->
                BackportRegistryState.pendingPassivesSnapshot().forEach(helper::register)));
    }

    @Inject(method = "getPassive", at = @At("HEAD"), cancellable = true, require = 0)
    private static void jlforkaddon$getPassive(String passiveNameOrId, CallbackInfoReturnable<Passive> cir) {
        if (passiveNameOrId == null || passiveNameOrId.isBlank()) {
            cir.setReturnValue(null);
            return;
        }

        ResourceLocation parsed = jlforkaddon$parseResourceLocation(passiveNameOrId);
        IForgeRegistry<Passive> registry = PASSIVES_REGISTRY != null ? PASSIVES_REGISTRY.get() : null;

        if (parsed != null) {
            Passive pendingById = BackportRegistryState.findPendingPassive(parsed, null);
            if (pendingById != null) {
                cir.setReturnValue(BackportRegistryState.isPassiveBlockedByDeletedAptitude(pendingById) ? null : pendingById);
                return;
            }
            if (registry != null) {
                Passive registeredById = registry.getValue(parsed);
                if (registeredById != null) {
                    cir.setReturnValue(BackportRegistryState.isPassiveBlockedByDeletedAptitude(registeredById) ? null : registeredById);
                    return;
                }
            }
        }

        String normalizedPath = parsed != null
                ? parsed.getPath().toLowerCase(Locale.ROOT)
                : passiveNameOrId.toLowerCase(Locale.ROOT);

        Passive pendingByPath = BackportRegistryState.findPendingPassive(parsed, normalizedPath);
        if (pendingByPath != null) {
            cir.setReturnValue(BackportRegistryState.isPassiveBlockedByDeletedAptitude(pendingByPath) ? null : pendingByPath);
            return;
        }

        if (registry == null) {
            cir.setReturnValue(null);
            return;
        }

        Passive result = registry.getValues().stream()
                .filter(passive -> passive.getName().equalsIgnoreCase(normalizedPath)
                        && !BackportRegistryState.isPassiveBlockedByDeletedAptitude(passive))
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
