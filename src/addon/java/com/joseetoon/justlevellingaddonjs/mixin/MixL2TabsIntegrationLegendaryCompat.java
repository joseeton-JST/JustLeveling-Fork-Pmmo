package com.joseetoon.justlevellingaddonjs.mixin;

import com.seniors.justlevelingfork.integration.L2TabsIntegration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = L2TabsIntegration.class, remap = false)
public abstract class MixL2TabsIntegrationLegendaryCompat {
    @Inject(method = "isModLoaded", at = @At("HEAD"), cancellable = true, require = 0)
    private static void jlforkaddon$disableL2TabsForAddon(CallbackInfoReturnable<Boolean> cir) {
        // Addon policy: when this addon is present, tabs are handled by LegendaryTabs only.
        cir.setReturnValue(false);
    }
}
