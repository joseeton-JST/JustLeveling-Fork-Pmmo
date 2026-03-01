package com.joseetoon.justlevellingaddonjs.mixin;

import com.joseetoon.justlevellingaddonjs.compat.base121.BackportRegistryState;
import com.seniors.justlevelingfork.JustLevelingFork;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import com.seniors.justlevelingfork.registry.skills.Skill;
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

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(value = RegistrySkills.class, remap = false)
public abstract class MixRegistrySkillsBackport {
    @Shadow
    public static ResourceKey<Registry<Skill>> SKILLS_KEY;

    @Shadow
    public static Supplier<IForgeRegistry<Skill>> SKILLS_REGISTRY;

    @Inject(method = "load", at = @At("TAIL"), require = 0)
    private static void jlforkaddon$load(IEventBus eventBus, CallbackInfo ci) {
        eventBus.addListener((RegisterEvent event) -> event.register(SKILLS_KEY, helper ->
                BackportRegistryState.pendingSkillsSnapshot().forEach(helper::register)));
    }

    @Inject(method = "getSkill", at = @At("HEAD"), cancellable = true, require = 0)
    private static void jlforkaddon$getSkill(String skillNameOrId, CallbackInfoReturnable<Skill> cir) {
        if (skillNameOrId == null || skillNameOrId.isBlank()) {
            cir.setReturnValue(null);
            return;
        }

        ResourceLocation parsed = jlforkaddon$parseResourceLocation(skillNameOrId);
        IForgeRegistry<Skill> registry = SKILLS_REGISTRY != null ? SKILLS_REGISTRY.get() : null;

        if (parsed != null) {
            Skill pendingById = BackportRegistryState.findPendingSkill(parsed, null);
            if (pendingById != null) {
                cir.setReturnValue(BackportRegistryState.isSkillBlockedByDeletedAptitude(pendingById) ? null : pendingById);
                return;
            }
            if (registry != null) {
                Skill registeredById = registry.getValue(parsed);
                if (registeredById != null) {
                    cir.setReturnValue(BackportRegistryState.isSkillBlockedByDeletedAptitude(registeredById) ? null : registeredById);
                    return;
                }
            }
        }

        String normalizedPath = parsed != null
                ? parsed.getPath().toLowerCase(Locale.ROOT)
                : skillNameOrId.toLowerCase(Locale.ROOT);

        Skill pendingByPath = BackportRegistryState.findPendingSkill(parsed, normalizedPath);
        if (pendingByPath != null) {
            cir.setReturnValue(BackportRegistryState.isSkillBlockedByDeletedAptitude(pendingByPath) ? null : pendingByPath);
            return;
        }

        if (registry == null) {
            cir.setReturnValue(null);
            return;
        }

        Skill result = registry.getValues().stream()
                .filter(skill -> skill.getName().equalsIgnoreCase(normalizedPath)
                        && !BackportRegistryState.isSkillBlockedByDeletedAptitude(skill))
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
