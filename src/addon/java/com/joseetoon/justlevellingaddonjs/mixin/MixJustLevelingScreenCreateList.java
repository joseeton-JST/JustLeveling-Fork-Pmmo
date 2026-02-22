package com.joseetoon.justlevellingaddonjs.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seniors.justlevelingfork.client.core.Utils;
import com.seniors.justlevelingfork.client.screen.JustLevelingScreen;
import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import com.seniors.justlevelingfork.handler.HandlerResources;
import com.seniors.justlevelingfork.network.packet.common.PassiveLevelUpSP;
import com.seniors.justlevelingfork.network.packet.common.ToggleSkillSP;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.seniors.justlevelingfork.registry.passive.Passive;
import com.seniors.justlevelingfork.registry.skills.Skill;
import com.joseetoon.justlevellingaddonjs.kubejs.VisibilityLockAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Mixin(value = JustLevelingScreen.class, remap = false)
public abstract class MixJustLevelingScreenCreateList {
    private static final int TITLE_SCROLL_THUMB_U = 176;
    private static final int TITLE_SCROLL_THUMB_V = 0;
    private static final int TITLE_SCROLL_THUMB_W = 12;
    private static final int TITLE_SCROLL_THUMB_H = 15;

    @Shadow
    @Final
    public static Minecraft client;

    @Shadow
    @Final
    private static int APTITUDE_COLUMNS;

    @Shadow
    @Final
    private static int APTITUDE_VISIBLE_ROWS;

    @Shadow
    @Final
    private static int APTITUDE_ROW_SPACING;

    @Shadow
    @Final
    private static int APTITUDE_COLUMN_SPACING;

    @Shadow
    @Final
    private static int APTITUDE_CELL_WIDTH;

    @Shadow
    @Final
    private static int APTITUDE_CELL_HEIGHT;

    @Shadow
    @Final
    private static int APTITUDE_LIST_X_OFFSET;

    @Shadow
    @Final
    private static int APTITUDE_LIST_Y_OFFSET;

    @Shadow
    @Final
    private static int APTITUDE_SCROLL_X_OFFSET;

    @Shadow
    @Final
    private static int APTITUDE_SCROLL_Y_OFFSET;

    @Shadow
    @Final
    private static int APTITUDE_SCROLL_WIDTH;

    @Shadow
    @Final
    private static int APTITUDE_SCROLL_HEIGHT;

    @Shadow
    @Final
    private static int APTITUDE_SCROLL_KNOB_HEIGHT;

    @Shadow
    public int selectedPage;

    @Shadow
    public String selectedAptitude;

    @Shadow
    public boolean isMouseCheck;

    @Shadow
    public boolean checkMouse;

    @Shadow
    public boolean b;

    @Shadow
    public int maxTick;

    @Shadow
    public int tick;

    @Shadow
    public int aptitudeScrollRows;

    @Shadow
    public int aptitudeScrollKnobY;

    @Shadow
    public boolean aptitudeScrolling;

    @Inject(method = "drawAptitudes", at = @At("TAIL"))
    private void jlforkaddon$drawAptitudeScrollbarSkin(GuiGraphics matrixStack, int x, int y, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.selectedPage != 0 || client.player == null) {
            return;
        }

        int scrollTrackX = x + APTITUDE_SCROLL_X_OFFSET;
        if (hasAptitudeOverflow(client.player)) {
            matrixStack.blit(
                    HandlerResources.SKILL_PAGE[2],
                    scrollTrackX,
                    this.aptitudeScrollKnobY,
                    TITLE_SCROLL_THUMB_U,
                    TITLE_SCROLL_THUMB_V,
                    TITLE_SCROLL_THUMB_W,
                    TITLE_SCROLL_THUMB_H
            );
        }
    }

    @Inject(method = "drawSkills", at = @At("HEAD"), cancellable = true)
    private void jlforkaddon$drawSkillsGuard(GuiGraphics matrixStack, int x, int y, int mouseX, int mouseY, CallbackInfo ci) {
        if (client.player == null) {
            this.selectedPage = 0;
            this.selectedAptitude = "";
            ci.cancel();
            return;
        }
        Aptitude aptitude = RegistryAptitudes.getAptitude(this.selectedAptitude);
        if (aptitude == null || !aptitude.isEnabled() || !VisibilityLockAPI.isVisible(client.player, aptitude.getName())) {
            this.selectedPage = 0;
            this.selectedAptitude = "";
            ci.cancel();
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void jlforkaddon$mouseScrolled(double mouseX, double mouseY, double amount, CallbackInfoReturnable<Boolean> cir) {
        if (this.selectedPage == 0) {
            int screenWidth = client.getWindow().getGuiScaledWidth();
            int screenHeight = client.getWindow().getGuiScaledHeight();
            int x = (screenWidth - 176) / 2;
            int y = (screenHeight - 166) / 2;

            int listX = x + APTITUDE_LIST_X_OFFSET;
            int listY = y + APTITUDE_LIST_Y_OFFSET;
            int listWidth = APTITUDE_COLUMN_SPACING * (APTITUDE_COLUMNS - 1) + APTITUDE_CELL_WIDTH;
            int scrollTrackX = x + APTITUDE_SCROLL_X_OFFSET;
            int scrollTrackY = y + APTITUDE_SCROLL_Y_OFFSET;

            boolean insideAptitudes = Utils.checkMouse(listX, listY, (int) mouseX, (int) mouseY, listWidth, APTITUDE_SCROLL_HEIGHT)
                    || Utils.checkMouse(scrollTrackX, scrollTrackY, (int) mouseX, (int) mouseY, APTITUDE_SCROLL_WIDTH, APTITUDE_SCROLL_HEIGHT);

            if (insideAptitudes) {
                List<Aptitude> aptitudeList = new ArrayList<>(RegistryAptitudes.APTITUDES_REGISTRY.get().getValues().stream()
                        .filter(aptitude -> aptitude.isEnabled() && !aptitude.isHidden() && VisibilityLockAPI.isVisible(client.player, aptitude.getName()))
                        .toList());
                int totalRows = (aptitudeList.size() + APTITUDE_COLUMNS - 1) / APTITUDE_COLUMNS;
                int maxScrollRows = Math.max(0, totalRows - APTITUDE_VISIBLE_ROWS);
                if (maxScrollRows > 0) {
                    int step = amount > 0 ? -1 : 1;
                    this.aptitudeScrollRows = Mth.clamp(this.aptitudeScrollRows + step, 0, maxScrollRows);
                }
                cir.setReturnValue(true);
                return;
            }
        }

        if (this.selectedPage == 2) {
            int step = amount > 0 ? -1 : 1;
            this.scrollDropDown = Math.max(0, this.scrollDropDown + step);
            cir.setReturnValue(true);
        }
    }

    @Shadow
    public int scrollDropDown;

    private static boolean hasAptitudeOverflow(net.minecraft.world.entity.player.Player player) {
        List<Aptitude> aptitudeList = new ArrayList<>(RegistryAptitudes.APTITUDES_REGISTRY.get().getValues().stream()
                .filter(aptitude -> aptitude.isEnabled() && !aptitude.isHidden() && VisibilityLockAPI.isVisible(player, aptitude.getName()))
                .toList());
        int totalRows = (aptitudeList.size() + APTITUDE_COLUMNS - 1) / APTITUDE_COLUMNS;
        return totalRows > APTITUDE_VISIBLE_ROWS;
    }

    /**
     * @author JLForkAddon
     * @reason Keep passive/skill GUI interaction parity with refork baseline.
     */
    @Overwrite
    public void createList(List<Object> list, AptitudeCapability capability, GuiGraphics matrixStack, int x, int y, int mouseX, int mouseY) {
        for (int i = 0; i < list.size(); i++) {

            int xTexture = x - 10 + 88 + 26 * i - 13 * (list.size() - 1);
            int yTexture = y - 10 + 90 + 13;

            int xIcon = x - 12 + 88 + 26 * i - 13 * (list.size() - 1);
            int yIcon = y - 12 + 90 + 13;

            Object object = list.get(i);
            if (object instanceof Passive passive) {
                int isMax = (passive.getLevel() == passive.getMaxLevel()) ? 24 : 0;
                matrixStack.blit(passive.getTexture(), xTexture, yTexture, 0.0F, 0.0F, 20, 20, 20, 20);
                matrixStack.blit(HandlerResources.SKILL_ICONS, xIcon, yIcon, 0.0F, isMax, 24, 24, 72, 72);
                int centerTextureX = xIcon + 9 - client.font.width(String.valueOf(passive.getLevel())) / 2;
                int passiveCost = passive.getPointCost();
                boolean canAffordPassive = capability.getAptitudeSkillPointsAvailable(passive.aptitude) >= passiveCost;

                int iconAdd = (passive.getLevel() < passive.getMaxLevel()
                        && capability.getAptitudeLevel(passive.aptitude) >= passive.getNextLevelUp()
                        && canAffordPassive) ? 10 : 0;
                int addButtonX = xIcon + 8;

                if (Utils.checkMouse(xIcon, yIcon, mouseX, mouseY, 24, 24)) {
                    if (Utils.checkMouse(addButtonX, yIcon + 2, mouseX, mouseY, 9, 9) &&
                            passive.getLevel() < passive.getMaxLevel()
                            && capability.getAptitudeLevel(passive.aptitude) >= passive.getNextLevelUp()
                            && canAffordPassive) {
                        iconAdd = 20;
                        this.isMouseCheck = true;
                        if (this.checkMouse) {
                            Utils.playSound();
                            PassiveLevelUpSP.send(passive);
                            this.checkMouse = false;
                        }
                    }

                    matrixStack.pose().pushPose();
                    Utils.drawToolTipList(matrixStack, passive.tooltip(), mouseX, mouseY);
                    RenderSystem.enableBlend();
                    matrixStack.blit(HandlerResources.SKILL_ICONS, xIcon, yIcon, 0.0F, 48.0F, 24, 24, 72, 72);
                    matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], addButtonX, yIcon + 2, 11, 167 + iconAdd, 9, 9);
                    matrixStack.pose().popPose();
                }

                matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], centerTextureX, yIcon + 17, 21, 167, 7, 8);
                matrixStack.drawString(client.font, String.valueOf(passive.getLevel()), centerTextureX + 8, yIcon + 18, Color.BLACK.getRGB(), false);
                matrixStack.drawString(client.font, String.valueOf(passive.getLevel()), centerTextureX + 7, yIcon + 17, Color.WHITE.getRGB(), false);
            }


            object = list.get(i);
            if (object instanceof Skill skill) {
                boolean levelUnlocked = skill.getToggle();
                boolean purchased = capability.isSkillUnlocked(skill);
                boolean active = skill.canSkill();
                boolean enoughPoints = capability.getAptitudeSkillPointsAvailable(skill.aptitude) >= skill.getPointCost();
                int isToggle = active ? 24 : 0;
                matrixStack.blit(skill.getTexture(), xTexture, yTexture, 0.0F, 0.0F, 20, 20, 20, 20);
                matrixStack.blit(HandlerResources.SKILL_ICONS, xIcon, yIcon, 24.0F, isToggle, 24, 24, 72, 72);
                if (!levelUnlocked || !purchased) {
                    matrixStack.pose().pushPose();
                    RenderSystem.enableBlend();
                    matrixStack.blit(HandlerResources.SKILL_ICONS, xIcon, yIcon, 24.0F, 48.0F, 24, 24, 72, 72);
                    matrixStack.pose().popPose();
                }
                if (levelUnlocked && !purchased) {
                    int centerCostX = xIcon + 18 - client.font.width(String.valueOf(skill.getPointCost())) / 2;
                    int costColor = enoughPoints ? 0xFFE3B341 : 0xFFD65C5C;
                    matrixStack.drawString(client.font, String.valueOf(skill.getPointCost()), centerCostX, yIcon + 17, costColor, false);
                } else if (purchased && !active) {
                    matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], xIcon + 2, yIcon + 2, 1, 167, 9, 9);
                }
                if (Utils.checkMouse(xIcon, yIcon, mouseX, mouseY, 24, 24)) {
                    Utils.drawToolTipList(matrixStack, skill.tooltip(), mouseX, mouseY);
                    if (levelUnlocked) {
                        this.isMouseCheck = true;
                        if (this.checkMouse) {
                            if (purchased || enoughPoints) {
                                boolean targetToggle = purchased ? !capability.getToggleSkill(skill) : true;
                                Utils.playSound();
                                ToggleSkillSP.send(skill, targetToggle);
                            }
                            this.checkMouse = false;
                        }
                    }
                }
            }
        }
    }
}
