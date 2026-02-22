package com.joseetoon.justlevellingaddonjs.mixin;

import com.joseetoon.justlevellingaddonjs.config.AddonClientRuntimeSettings;
import com.seniors.justlevelingfork.client.core.Utils;
import com.seniors.justlevelingfork.client.screen.JustLevelingScreen;
import com.seniors.justlevelingfork.handler.HandlerResources;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = JustLevelingScreen.class, remap = false)
public abstract class MixJLScreenOptionalControls {
    @Redirect(
            method = "drawSkills",
            at = @At(value = "INVOKE", target = "Lcom/seniors/justlevelingfork/client/core/Utils;checkMouse(IIIIII)Z"),
            require = 0
    )
    private boolean jlforkaddon$hideSkillControlsMouseCheck(int x, int y, int mouseX, int mouseY, int width, int height) {
        AddonClientRuntimeSettings.Snapshot settings = AddonClientRuntimeSettings.current();

        if (!settings.showAptitudeXpLevelButton() && jlforkaddon$isXpButtonHitbox(width, height)) {
            return false;
        }
        if (!settings.showSkillSortControls() && jlforkaddon$isSortButtonHitbox(width, height)) {
            return false;
        }

        return Utils.checkMouse(x, y, mouseX, mouseY, width, height);
    }

    @Redirect(
            method = "drawTitles",
            at = @At(value = "INVOKE", target = "Lcom/seniors/justlevelingfork/client/core/Utils;checkMouse(IIIIII)Z"),
            require = 0
    )
    private boolean jlforkaddon$hideTitleControlsMouseCheck(int x, int y, int mouseX, int mouseY, int width, int height) {
        if (!AddonClientRuntimeSettings.current().showTitleSortControls() && jlforkaddon$isSortButtonHitbox(width, height)) {
            return false;
        }

        return Utils.checkMouse(x, y, mouseX, mouseY, width, height);
    }

    @Redirect(
            method = "drawSkills",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;m_280218_(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"),
            require = 0
    )
    private void jlforkaddon$hideSkillControlsBlitObf(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int u,
            int v,
            int width,
            int height
    ) {
        jlforkaddon$hideSkillControlsBlitInternal(graphics, texture, x, y, u, v, width, height);
    }

    @Redirect(
            method = "drawTitles",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;m_280218_(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"),
            require = 0
    )
    private void jlforkaddon$hideTitleControlsBlitObf(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int u,
            int v,
            int width,
            int height
    ) {
        jlforkaddon$hideTitleControlsBlitInternal(graphics, texture, x, y, u, v, width, height);
    }

    @Redirect(
            method = "drawSkills",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"),
            require = 0
    )
    private void jlforkaddon$hideSkillControlsBlitDeobf(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int u,
            int v,
            int width,
            int height
    ) {
        jlforkaddon$hideSkillControlsBlitInternal(graphics, texture, x, y, u, v, width, height);
    }

    @Redirect(
            method = "drawTitles",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"),
            require = 0
    )
    private void jlforkaddon$hideTitleControlsBlitDeobf(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int u,
            int v,
            int width,
            int height
    ) {
        jlforkaddon$hideTitleControlsBlitInternal(graphics, texture, x, y, u, v, width, height);
    }

    @Redirect(
            method = "drawBackground",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;m_280218_(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"),
            require = 0
    )
    private void jlforkaddon$hideAptitudeXpBarBlitObf(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int u,
            int v,
            int width,
            int height
    ) {
        jlforkaddon$drawBackgroundBlitInternal(graphics, texture, x, y, u, v, width, height);
    }

    @Redirect(
            method = "drawBackground",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"),
            require = 0
    )
    private void jlforkaddon$hideAptitudeXpBarBlitDeobf(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int u,
            int v,
            int width,
            int height
    ) {
        jlforkaddon$drawBackgroundBlitInternal(graphics, texture, x, y, u, v, width, height);
    }

    @Unique
    private static boolean jlforkaddon$isSortButtonHitbox(int width, int height) {
        return width == 11 && height == 11;
    }

    @Unique
    private static boolean jlforkaddon$isXpButtonHitbox(int width, int height) {
        return width == 14 && height == 14;
    }

    @Unique
    private static boolean jlforkaddon$isSortButtonIcon(int u, int v, int width, int height) {
        return width == 11
                && height == 11
                && (u == 30 || u == 42 || u == 54)
                && (v == 167 || v == 179);
    }

    @Unique
    private static boolean jlforkaddon$isTitleSortButtonIcon(int u, int v, int width, int height) {
        return width == 11
                && height == 11
                && (u == 30 || u == 42)
                && (v == 167 || v == 179);
    }

    @Unique
    private static boolean jlforkaddon$isXpButtonIcon(int u, int v, int width, int height) {
        return width == 6
                && height == 6
                && v == 1
                && (u == 177 || u == 183 || u == 189);
    }

    @Unique
    private static boolean jlforkaddon$isAptitudeXpProgressBar(int u, int v, int width, int height) {
        return u == 0
                && v == 166
                && height == 5
                && width >= 0
                && width <= 151;
    }

    @Unique
    private static void jlforkaddon$hideSkillControlsBlitInternal(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int u,
            int v,
            int width,
            int height
    ) {
        AddonClientRuntimeSettings.Snapshot settings = AddonClientRuntimeSettings.current();
        if (!settings.showAptitudeXpLevelButton() && jlforkaddon$isXpButtonIcon(u, v, width, height)) {
            return;
        }
        if (!settings.showSkillSortControls() && jlforkaddon$isSortButtonIcon(u, v, width, height)) {
            return;
        }

        graphics.blit(texture, x, y, u, v, width, height);
    }

    @Unique
    private static void jlforkaddon$hideTitleControlsBlitInternal(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int u,
            int v,
            int width,
            int height
    ) {
        if (!AddonClientRuntimeSettings.current().showTitleSortControls() && jlforkaddon$isTitleSortButtonIcon(u, v, width, height)) {
            return;
        }

        graphics.blit(texture, x, y, u, v, width, height);
    }

    @Unique
    private static void jlforkaddon$drawBackgroundBlitInternal(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int u,
            int v,
            int width,
            int height
    ) {
        if (!AddonClientRuntimeSettings.current().showAptitudeXpLevelButton()
                && jlforkaddon$isAptitudeXpProgressBar(u, v, width, height)) {
            return;
        }

        graphics.blit(texture, x, y, u, v, width, height);
    }

    @Inject(method = "drawSkills", at = @At("TAIL"), require = 0)
    private void jlforkaddon$eraseDisabledXpButton(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, CallbackInfo ci) {
        if (AddonClientRuntimeSettings.current().showAptitudeXpLevelButton()) {
            return;
        }

        // Base 1.2.1 has a static dark XP stamp in the panel texture; copy a clean header patch over it.
        graphics.blit(HandlerResources.SKILL_PAGE[1], x + 149, y + 10, 120, 10, 14, 14);
    }
}
