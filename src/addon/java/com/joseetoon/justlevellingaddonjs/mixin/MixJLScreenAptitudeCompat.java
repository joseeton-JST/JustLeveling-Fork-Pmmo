package com.joseetoon.justlevellingaddonjs.mixin;

import com.joseetoon.justlevellingaddonjs.compat.AptitudeCompat;
import com.joseetoon.justlevellingaddonjs.compat.base121.Base121Bridge;
import com.joseetoon.justlevellingaddonjs.compat.base121.BackportRegistryState;
import com.joseetoon.justlevellingaddonjs.config.AddonClientRuntimeSettings;
import com.joseetoon.justlevellingaddonjs.kubejs.VisibilityLockAPI;
import com.seniors.justlevelingfork.client.core.Utils;
import com.seniors.justlevelingfork.client.screen.JustLevelingScreen;
import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import com.seniors.justlevelingfork.handler.HandlerResources;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.seniors.justlevelingfork.registry.title.Title;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Mixin(value = JustLevelingScreen.class, remap = false)
public abstract class MixJLScreenAptitudeCompat {
    @Unique
    private static final int APTITUDE_COLUMNS = 2;
    @Unique
    private static final int APTITUDE_VISIBLE_ROWS = 4;
    @Unique
    private static final int APTITUDE_ROW_SPACING = 28;
    @Unique
    private static final int APTITUDE_COLUMN_SPACING = 77;
    @Unique
    private static final int APTITUDE_CELL_WIDTH = 74;
    @Unique
    private static final int APTITUDE_CELL_HEIGHT = 26;
    @Unique
    private static final int APTITUDE_LIST_X_OFFSET = 12;
    @Unique
    private static final int APTITUDE_LIST_Y_OFFSET = 50;
    @Unique
    private static final int APTITUDE_SCROLL_X_OFFSET = 156;
    @Unique
    private static final int APTITUDE_SCROLL_Y_OFFSET = 50;
    @Unique
    private static final int APTITUDE_SCROLL_WIDTH = 12;
    @Unique
    private static final int APTITUDE_SCROLL_HEIGHT = 112;
    @Unique
    private static final int APTITUDE_SCROLL_KNOB_HEIGHT = 15;
    @Unique
    private static final String JLFORKADDON_NONE_LABEL = "(none)";
    @Unique
    private static final int JLFORKADDON_NONE_COLOR = 0x9AA0A6;

    @Shadow
    @Final
    public static Minecraft client;

    @Shadow
    public int selectedPage;

    @Shadow
    public String selectedAptitude;

    @Shadow
    public int skillActualPage;

    @Shadow
    public boolean checkMouse;

    @Shadow
    public boolean isMouseCheck;

    @Shadow
    public boolean b;

    @Shadow
    public int maxTick;

    @Shadow
    public int tick;

    @Shadow
    public int scrollDropDown;

    @Unique
    private int jlforkaddon$aptitudeScrollRows;

    @Unique
    private int jlforkaddon$aptitudeScrollKnobY;

    @Unique
    private boolean jlforkaddon$aptitudeScrolling;

    /**
     * @author JLForkAddon
     * @reason Backport aptitude scrolling/visibility UX to base 1.2.1 without changing gameplay logic.
     */
    @Overwrite
    public void drawAptitudes(GuiGraphics matrixStack, int x, int y, int mouseX, int mouseY) {
        AddonClientRuntimeSettings.Snapshot settings = AddonClientRuntimeSettings.current();

        if (client.player == null) {
            return;
        }

        // Failsafe: if release event was missed, stop dragging once LMB is no longer pressed.
        if (this.jlforkaddon$aptitudeScrolling) {
            long windowHandle = client.getWindow().getWindow();
            if (GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_PRESS) {
                this.jlforkaddon$aptitudeScrolling = false;
            }
        }

        List<Aptitude> aptitudeList = new ArrayList<>(RegistryAptitudes.APTITUDES_REGISTRY.get().getValues().stream()
                .filter(aptitude -> AptitudeCompat.isEnabled(aptitude)
                        && !AptitudeCompat.isHidden(aptitude)
                        && !BackportRegistryState.isAptitudeDeleted(AptitudeCompat.getName(aptitude))
                        && VisibilityLockAPI.isVisible(client.player, AptitudeCompat.getName(aptitude)))
                .toList());
        aptitudeList.sort(Comparator.comparingInt(a -> a.index));

        Utils.drawCenter(matrixStack, client.player.getName(), x + 88, y + 7);
        double averageAptitudeLevel = aptitudeList.stream().mapToInt(Aptitude::getLevel).average().orElse(0.0D);
        int displayLevel = Mth.floor(averageAptitudeLevel);
        Utils.drawCenter(matrixStack, Component.literal("Lvl: " + displayLevel), x + 88, y + 17);

        if (settings.showTitleButton()) {
            AptitudeCapability capability = AptitudeCapability.get();
            Title titleKey = capability != null ? RegistryTitles.getTitle(capability.getPlayerTitle()) : null;
            boolean titlelessSelected = titleKey != null && "titleless".equalsIgnoreCase(titleKey.getName());
            String title = titlelessSelected ? JLFORKADDON_NONE_LABEL : Base121Bridge.titleDisplayNameOrFallback(titleKey);
            int titleWidth = client.font.width(title) + 15;
            boolean overTitleButton = Utils.checkMouse(x + 88 - titleWidth / 2 - 2, y + 27, mouseX, mouseY, titleWidth + 2, 14);

            matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], x + 88 - titleWidth / 2 - 2, y + 27, overTitleButton ? 4 : 0, 214, 2, 14);
            matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], x + 88 - titleWidth / 2, y + 27, 0, overTitleButton ? 228 : 242, titleWidth, 14);
            matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], x + 88 + titleWidth / 2, y + 27, overTitleButton ? 6 : 2, 214, 2, 14);
            matrixStack.drawString(client.font, title, x + 88 - titleWidth / 2 + 2, y + 30, titlelessSelected ? JLFORKADDON_NONE_COLOR : Color.WHITE.getRGB());
            matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], x + 88 + titleWidth / 2 - 10, y + 30, 8, 218, 8, 8);

            if (overTitleButton) {
                Utils.drawToolTip(matrixStack, Component.literal("Edit Title"), mouseX, mouseY);
                this.isMouseCheck = true;
                if (this.checkMouse) {
                    this.selectedPage = 2;
                    Utils.playSound();
                    this.checkMouse = false;
                }
            }
        }

        int totalRows = (aptitudeList.size() + APTITUDE_COLUMNS - 1) / APTITUDE_COLUMNS;
        int maxScrollRows = Math.max(0, totalRows - APTITUDE_VISIBLE_ROWS);
        this.jlforkaddon$aptitudeScrollRows = Mth.clamp(this.jlforkaddon$aptitudeScrollRows, 0, maxScrollRows);

        int listX = x + APTITUDE_LIST_X_OFFSET;
        int listY = y + APTITUDE_LIST_Y_OFFSET;
        int scrollTrackX = x + APTITUDE_SCROLL_X_OFFSET;
        int scrollTrackY = y + APTITUDE_SCROLL_Y_OFFSET;
        boolean overTrackArea = Utils.checkMouse(scrollTrackX, scrollTrackY, mouseX, mouseY, APTITUDE_SCROLL_WIDTH, APTITUDE_SCROLL_HEIGHT);

        for (int localRow = 0; localRow < APTITUDE_VISIBLE_ROWS; localRow++) {
            int globalRow = this.jlforkaddon$aptitudeScrollRows + localRow;
            for (int column = 0; column < APTITUDE_COLUMNS; column++) {
                int index = globalRow * APTITUDE_COLUMNS + column;
                if (index >= aptitudeList.size()) {
                    continue;
                }

                Aptitude aptitude = aptitudeList.get(index);
                int aptitudeLevel = aptitude.getLevel();
                String aptitudeName = AptitudeCompat.getDisplayNameOrFallback(aptitude);
                String aptitudeAbbreviation = AptitudeCompat.getAbbreviationOrFallback(aptitude);

                int xPos = listX + column * APTITUDE_COLUMN_SPACING;
                int yPos = listY + localRow * APTITUDE_ROW_SPACING;
                boolean overCell = Utils.checkMouse(xPos, yPos, mouseX, mouseY, APTITUDE_CELL_WIDTH, APTITUDE_CELL_HEIGHT) && !overTrackArea;

                if (overCell) {
                    matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], xPos, yPos, 176, 0, 73, 26);
                }

                matrixStack.blit(aptitude.getLockedTexture(), xPos + 5, yPos + 5, 0.0F, 0.0F, 16, 16, 16, 16);
                matrixStack.drawString(client.font, Component.literal(aptitudeAbbreviation).withStyle(ChatFormatting.BOLD), xPos + 24, yPos + 5, new Color(240, 240, 240).getRGB(), false);
                matrixStack.drawString(
                        client.font,
                        Component.translatable("screen.aptitude.experience", Utils.numberFormat(aptitudeLevel), AptitudeCompat.getLevelCap(aptitude)),
                        xPos + 24,
                        yPos + 14,
                        new Color(170, 170, 170).getRGB(),
                        false
                );

                if (overCell) {
                    Utils.drawToolTip(matrixStack, Component.literal(aptitudeName), mouseX, mouseY);
                    this.isMouseCheck = true;
                    if (this.checkMouse) {
                        this.tick = this.maxTick / 2;
                        this.b = true;
                        this.selectedAptitude = AptitudeCompat.getName(aptitude);
                        this.skillActualPage = 0;
                        this.selectedPage = 1;
                        Utils.playSound();
                        this.checkMouse = false;
                    }
                }
            }
        }

        int knobTravel = APTITUDE_SCROLL_HEIGHT - APTITUDE_SCROLL_KNOB_HEIGHT;
        if (maxScrollRows > 0) {
            if (this.jlforkaddon$aptitudeScrolling) {
                int draggedKnobY = mouseY - APTITUDE_SCROLL_KNOB_HEIGHT / 2;
                this.jlforkaddon$aptitudeScrollKnobY = Mth.clamp(draggedKnobY, scrollTrackY, scrollTrackY + knobTravel);
                float progress = (float) (this.jlforkaddon$aptitudeScrollKnobY - scrollTrackY) / knobTravel;
                this.jlforkaddon$aptitudeScrollRows = Mth.clamp(Math.round(progress * maxScrollRows), 0, maxScrollRows);
            }

            float scrollProgress = (float) this.jlforkaddon$aptitudeScrollRows / maxScrollRows;
            this.jlforkaddon$aptitudeScrollKnobY = scrollTrackY + Math.round(scrollProgress * knobTravel);

            boolean overTrack = Utils.checkMouse(scrollTrackX, scrollTrackY, mouseX, mouseY, APTITUDE_SCROLL_WIDTH, APTITUDE_SCROLL_HEIGHT);
            boolean overKnob = Utils.checkMouse(scrollTrackX, this.jlforkaddon$aptitudeScrollKnobY, mouseX, mouseY, APTITUDE_SCROLL_WIDTH, APTITUDE_SCROLL_KNOB_HEIGHT);
            if (overTrack) {
                this.isMouseCheck = true;
                if (this.checkMouse) {
                    this.jlforkaddon$aptitudeScrolling = true;
                    if (!overKnob) {
                        int clickedKnobY = mouseY - APTITUDE_SCROLL_KNOB_HEIGHT / 2;
                        this.jlforkaddon$aptitudeScrollKnobY = Mth.clamp(clickedKnobY, scrollTrackY, scrollTrackY + knobTravel);
                        float progress = (float) (this.jlforkaddon$aptitudeScrollKnobY - scrollTrackY) / knobTravel;
                        this.jlforkaddon$aptitudeScrollRows = Mth.clamp(Math.round(progress * maxScrollRows), 0, maxScrollRows);
                    }
                    Utils.playSound();
                    this.checkMouse = false;
                }
            }

            matrixStack.blit(HandlerResources.SKILL_PAGE[2], scrollTrackX, this.jlforkaddon$aptitudeScrollKnobY, 176, 0, 12, 15);
        } else {
            this.jlforkaddon$aptitudeScrolling = false;
            this.jlforkaddon$aptitudeScrollKnobY = scrollTrackY;
        }
    }

    @Inject(method = {"mouseScrolled", "m_6050_"}, at = @At("HEAD"), cancellable = true, require = 0)
    private void jlforkaddon$mouseScrolled(double mouseX, double mouseY, double amount, CallbackInfoReturnable<Boolean> cir) {
        if (!AddonClientRuntimeSettings.current().enableMouseWheelScroll()) {
            return;
        }

        if (this.selectedPage == 0 && client.player != null) {
            int x = (client.getWindow().getGuiScaledWidth() - 176) / 2;
            int y = (client.getWindow().getGuiScaledHeight() - 166) / 2;
            int listX = x + APTITUDE_LIST_X_OFFSET;
            int listY = y + APTITUDE_LIST_Y_OFFSET;
            int listWidth = APTITUDE_COLUMN_SPACING * (APTITUDE_COLUMNS - 1) + APTITUDE_CELL_WIDTH;
            int scrollTrackX = x + APTITUDE_SCROLL_X_OFFSET;
            int scrollTrackY = y + APTITUDE_SCROLL_Y_OFFSET;

            boolean insideAptitudes = Utils.checkMouse(listX, listY, (int) mouseX, (int) mouseY, listWidth, APTITUDE_SCROLL_HEIGHT)
                    || Utils.checkMouse(scrollTrackX, scrollTrackY, (int) mouseX, (int) mouseY, APTITUDE_SCROLL_WIDTH, APTITUDE_SCROLL_HEIGHT);
            if (insideAptitudes) {
                List<Aptitude> aptitudeList = new ArrayList<>(RegistryAptitudes.APTITUDES_REGISTRY.get().getValues().stream()
                        .filter(aptitude -> AptitudeCompat.isEnabled(aptitude)
                                && !AptitudeCompat.isHidden(aptitude)
                                && !BackportRegistryState.isAptitudeDeleted(AptitudeCompat.getName(aptitude))
                                && VisibilityLockAPI.isVisible(client.player, AptitudeCompat.getName(aptitude)))
                        .toList());
                int totalRows = (aptitudeList.size() + APTITUDE_COLUMNS - 1) / APTITUDE_COLUMNS;
                int maxScrollRows = Math.max(0, totalRows - APTITUDE_VISIBLE_ROWS);
                if (maxScrollRows > 0) {
                    int step = amount > 0 ? -1 : 1;
                    this.jlforkaddon$aptitudeScrollRows = Mth.clamp(this.jlforkaddon$aptitudeScrollRows + step, 0, maxScrollRows);
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

    @Inject(method = {"mouseReleased", "m_6348_"}, at = @At("HEAD"), require = 0)
    private void jlforkaddon$mouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button == 0) {
            this.jlforkaddon$aptitudeScrolling = false;
        }
    }

    @Inject(method = "m_7379_", at = @At("HEAD"), require = 0)
    private void jlforkaddon$resetScrollState(CallbackInfo ci) {
        this.jlforkaddon$aptitudeScrollRows = 0;
        this.jlforkaddon$aptitudeScrolling = false;
        this.jlforkaddon$aptitudeScrollKnobY = 0;
    }

}
