package com.joseetoon.justlevellingaddonjs.mixin;

import com.joseetoon.justlevellingaddonjs.kubejs.compat.AbilityCreator;
import com.joseetoon.justlevellingaddonjs.kubejs.compat.AptitudeAPI;
import com.joseetoon.justlevellingaddonjs.kubejs.compat.LegacySkill;
import com.joseetoon.justlevellingaddonjs.kubejs.compat.PassiveCreator;
import com.joseetoon.justlevellingaddonjs.kubejs.compat.SkillCreator;
import com.joseetoon.justlevellingaddonjs.kubejs.compat.TitleCompat;
import com.joseetoon.justlevellingaddonjs.kubejs.TitleAPI;
import com.joseetoon.justlevellingaddonjs.kubejs.events.LevelUpEvent;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = com.seniors.justlevelingfork.kubejs.Plugin.class, remap = false)
public class MixKubeJSPluginBindings {
    @Inject(method = "registerBindings", at = @At("RETURN"), require = 0)
    private void jlforkaddon$overrideCompatBindings(BindingsEvent event, CallbackInfo ci) {
        event.add("Aptitude", AptitudeAPI.class);
        event.add("SkillCreator", SkillCreator.class);
        event.add("AbilityCreator", AbilityCreator.class);
        event.add("PassiveCreator", PassiveCreator.class);
        event.add("LegacySkill", LegacySkill.class);
        event.add("Title", TitleCompat.class);
        event.add("TitleAPI", TitleAPI.class);
        event.add("TitleComparator", TitleAPI.Comparator.class);
    }

    @Inject(method = "registerEvents", at = @At("HEAD"), require = 0)
    private void jlforkaddon$ensureServerLevelUpEvent(CallbackInfo ci) {
        try {
            com.seniors.justlevelingfork.kubejs.events.CustomEvents.GROUP.server("aptitudeLevelUpServer", () -> LevelUpEvent.class);
        } catch (Throwable ignored) {
        }
    }
}
