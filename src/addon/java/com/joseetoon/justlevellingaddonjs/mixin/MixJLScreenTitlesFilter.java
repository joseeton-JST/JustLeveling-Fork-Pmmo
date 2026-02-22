package com.joseetoon.justlevellingaddonjs.mixin;

import com.joseetoon.justlevellingaddonjs.config.AddonClientRuntimeSettings;
import com.seniors.justlevelingfork.client.screen.JustLevelingScreen;
import com.seniors.justlevelingfork.registry.title.Title;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(value = JustLevelingScreen.class, remap = false)
public abstract class MixJLScreenTitlesFilter {
    @Inject(
            method = "drawTitles",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;sort(Ljava/util/Comparator;)V",
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            require = 0
    )
    private void jlforkaddon$applyHideLockedTitlesFilter(
            GuiGraphics graphics,
            int x,
            int y,
            int mouseX,
            int mouseY,
            float delta,
            CallbackInfo ci,
            List<Title> titleList,
            List<Title> unlockTitles,
            List<Title> lockTitles
    ) {
        if (AddonClientRuntimeSettings.current().hideLockedTitles()) {
            lockTitles.clear();
            return;
        }

        lockTitles.clear();
        for (Title title : titleList) {
            if (!title.getRequirement() && !title.HideRequirements) {
                lockTitles.add(title);
            }
        }
    }
}
