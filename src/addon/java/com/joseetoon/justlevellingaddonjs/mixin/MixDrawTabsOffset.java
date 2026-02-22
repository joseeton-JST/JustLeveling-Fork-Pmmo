package com.joseetoon.justlevellingaddonjs.mixin;

import com.seniors.justlevelingfork.client.gui.DrawTabs;
import com.seniors.justlevelingfork.client.screen.JustLevelingScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DrawTabs.class, remap = false)
public abstract class MixDrawTabsOffset {
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, require = 0)
    private static void jlforkaddon$disableLegacyClickDispatchWhenLegendaryActive(int button, CallbackInfo ci) {
        Screen screen = DrawTabs.client.screen;
        if (!(screen instanceof InventoryScreen) && !(screen instanceof JustLevelingScreen)) {
            return;
        }

        if (ModList.get().isLoaded("legendarytabs")) {
            DrawTabs.isMouseCheck = false;
            DrawTabs.checkMouse = false;
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
    private static void jlforkaddon$renderWithInventoryOffset(
            GuiGraphics matrixStack,
            int mouseX,
            int mouseY,
            int textureWidth,
            int textureHeight,
            int recipe,
            CallbackInfo ci
    ) {
        Minecraft client = DrawTabs.client;
        Screen screen = client.screen;
        if (!(screen instanceof InventoryScreen) && !(screen instanceof JustLevelingScreen)) {
            return;
        }
        if (ModList.get().isLoaded("legendarytabs")) {
            // LegendaryTabs-only policy: never render legacy DrawTabs when LegendaryTabs is present.
            ci.cancel();
        }
    }
}
