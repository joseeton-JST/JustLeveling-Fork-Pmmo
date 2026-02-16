package com.seniors.justlevelingfork.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seniors.justlevelingfork.client.core.SortPassives;
import com.seniors.justlevelingfork.client.core.SortSkills;
import com.seniors.justlevelingfork.client.core.Utils;
import com.seniors.justlevelingfork.client.gui.DrawTabs;
import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import com.seniors.justlevelingfork.handler.HandlerCommonConfig;
import com.seniors.justlevelingfork.handler.HandlerConfigClient;
import com.seniors.justlevelingfork.handler.HandlerResources;
import com.seniors.justlevelingfork.integration.KubeJSIntegration;
import com.seniors.justlevelingfork.kubejs.VisibilityLockAPI;
import com.seniors.justlevelingfork.network.packet.common.*;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.seniors.justlevelingfork.registry.passive.Passive;
import com.seniors.justlevelingfork.registry.skills.Skill;
import com.seniors.justlevelingfork.registry.title.Title;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@OnlyIn(Dist.CLIENT)
public class JustLevelingScreen extends Screen {
    public static Minecraft client = Minecraft.getInstance();
    private static final int BASE_PANEL_WIDTH = 176;
    private static final int BASE_PANEL_HEIGHT = 166;
    private static final int SKILL_PANEL_WIDTH = 176;
    private static final int SKILL_PANEL_HEIGHT = 178;
    private static final int SKILL_BOTTOM_ROW_Y_OFFSET = 154;
    private static final int SKILL_POINTS_Y_OFFSET = 163;
    private static final int APTITUDE_COLUMNS = 2;
    private static final int APTITUDE_VISIBLE_ROWS = 4;
    private static final int APTITUDE_ROW_SPACING = 28;
    private static final int APTITUDE_COLUMN_SPACING = 77;
    private static final int APTITUDE_CELL_WIDTH = 74;
    private static final int APTITUDE_CELL_HEIGHT = 26;
    private static final int APTITUDE_LIST_X_OFFSET = 12;
    private static final int APTITUDE_LIST_Y_OFFSET = 50;
    private static final int APTITUDE_SCROLL_X_OFFSET = 156;
    private static final int APTITUDE_SCROLL_Y_OFFSET = 50;
    private static final int APTITUDE_SCROLL_WIDTH = 12;
    private static final int APTITUDE_SCROLL_HEIGHT = 112;
    private static final int APTITUDE_SCROLL_KNOB_HEIGHT = 15;
    public int selectedPage = 0;
    public String selectedAptitude = "";

    public boolean checkMouse = false;

    public boolean isMouseCheck = false;
    public boolean b = true;
    public int maxTick = 40;
    public int tick = 0;

    public int skillSize = 0;
    public int skillActualPage = 0;
    public int skillSizePage = 0;
    public int aptitudeActualPage = 0;
    public int aptitudeSizePage = 0;
    public int aptitudeScrollRows = 0;
    public int aptitudeScrollKnobY = 0;
    public boolean aptitudeScrolling = false;

    public int scrollYOff = 0;
    public int scrollDropDown = 0;

    public boolean scrollingDropDown = false;
    public String searchValue = "";
    private EditBox searchTitle;

    public JustLevelingScreen() {
        super(Component.translatable("screen.aptitude.title"));
    }

    protected void init() {
        int x = (this.width - BASE_PANEL_WIDTH) / 2;
        int y = (this.height - BASE_PANEL_HEIGHT) / 2;

        this.scrollYOff = y + 33;
        this.aptitudeScrollKnobY = y + APTITUDE_SCROLL_Y_OFFSET;

        this.searchTitle = new EditBox(this.font, x + 88 - 47, y + 17, 93, 12, Component.translatable("screen.title.search"));
        this.searchTitle.setMaxLength(50);
        this.searchTitle.setBordered(true);
        this.searchTitle.setTextColor(16777215);
        this.searchTitle.setFocused(true);
        this.searchTitle.setValue(this.searchValue);

        super.init();
    }


    public void render(@NotNull GuiGraphics matrixStack, int mouseX, int mouseY, float delta) {
        this.isMouseCheck = false;
        int x = (this.width - BASE_PANEL_WIDTH) / 2;
        int y = (this.height - BASE_PANEL_HEIGHT) / 2;

        drawBackground(matrixStack, x, y, mouseX, mouseY, delta);

        super.render(matrixStack, mouseX, mouseY, delta);
    }

    public void drawBackground(GuiGraphics matrixStack, int x, int y, int mouseX, int mouseY, float delta) {
        renderBackground(matrixStack);
        assert client.player != null;
        int progress = (int) (client.player.experienceProgress * 151.0F);
        matrixStack.pose().pushPose();
        if (this.selectedPage == 0) {
            RenderSystem.enableBlend();
            matrixStack.blit(HandlerResources.SKILL_PAGE[0], x, y, 0, 0, BASE_PANEL_WIDTH, BASE_PANEL_HEIGHT);
            matrixStack.blit(HandlerResources.SKILL_PAGE[0], x + 12, y + 43, 0, 166, progress, 5);
            drawAptitudes(matrixStack, x, y, mouseX, mouseY);
        }
        if (this.selectedPage == 1) {
            RenderSystem.enableBlend();
            Aptitude selected = RegistryAptitudes.getAptitude(this.selectedAptitude);
            if (selected == null || !selected.isEnabled()) {
                this.selectedPage = 0;
                this.selectedAptitude = "";
            } else {
                drawAptitudeBackground(matrixStack, selected, x, y);
            }
            matrixStack.blit(HandlerResources.SKILL_PAGE_2_EXPANDED, x, y, 0, 0, SKILL_PANEL_WIDTH, SKILL_PANEL_HEIGHT);
            drawSkills(matrixStack, x, y, mouseX, mouseY);
        }
        if (this.selectedPage == 2) {
            RenderSystem.enableBlend();
            matrixStack.blit(HandlerResources.SKILL_PAGE[2], x, y, 0, 0, BASE_PANEL_WIDTH, BASE_PANEL_HEIGHT);
            drawTitles(matrixStack, x, y, mouseX, mouseY, delta);
        }

        DrawTabs.render(matrixStack, mouseX, mouseY, BASE_PANEL_WIDTH, BASE_PANEL_HEIGHT, 0);
        matrixStack.pose().popPose();
    }

    private int getContainerWidth() {
        return BASE_PANEL_WIDTH;
    }

    private int getContainerHeight() {
        return BASE_PANEL_HEIGHT;
    }

    public void drawTitles(GuiGraphics matrixStack, int x, int y, int mouseX, int mouseY, float delta) {
        Utils.drawCenter(matrixStack, Component.translatable("screen.title.choose_your_title"), x + 88, y + 7);

        this.searchTitle.setVisible(true);
        this.searchTitle.render(matrixStack, mouseX, mouseY, delta);


        List<Title> titleList = RegistryTitles.TITLES_REGISTRY.get().getValues().stream().toList();
        List<Title> unlockTitles = new ArrayList<>();
        List<Title> lockTitles = new ArrayList<>();
        boolean showLockedTitles = HandlerConfigClient.showLockedTitles.get();
        for (Title title : titleList) {
            if (title.getRequirement()) {
                unlockTitles.add(title);
                continue;
            }
            if (showLockedTitles && !title.HideRequirements)
                lockTitles.add(title);
        }

        unlockTitles.sort(new SortTitleByName());
        lockTitles.sort(new SortTitleByName());
        List<Title> sorted = new ArrayList<>();
        sorted.addAll(unlockTitles);
        sorted.addAll(lockTitles);
        List<Title> searchTitleList = this.searchTitle.getValue().isEmpty() ? sorted : new ArrayList<>();
        if (!this.searchTitle.getValue().isEmpty() && !this.searchTitle.getValue().equals(" ")) {
            for (Title title : sorted) {
                if (title.getDisplayNameOrFallback().toLowerCase().contains(this.searchTitle.getValue().toLowerCase())) {
                    searchTitleList.add(title);
                }
            }
        }

        int maxSize = 9;
        int size = Math.min(searchTitleList.size(), maxSize);
        int overflow = Math.max(0, searchTitleList.size() - maxSize);
        if (this.scrollDropDown > overflow)
            this.scrollDropDown = overflow;
        if (this.scrollDropDown < 0) this.scrollDropDown = 0;

        matrixStack.pose().pushPose();

        int scrollX;
        int scrollY;
        for (scrollX = this.scrollDropDown; scrollX < this.scrollDropDown + size; ++scrollX) {
            Title title = (Title) searchTitleList.get(scrollX);
            boolean checkTitle = title == RegistryTitles.getTitle(AptitudeCapability.get().getPlayerTitle());
            scrollY = title.getRequirement() ? (checkTitle ? Color.GREEN.getRGB() : Color.WHITE.getRGB()) : Color.DARK_GRAY.getRGB();
            matrixStack.drawString(client.font, title.getDisplayNameOrFallback(), x + 10, y + 34 + 12 * (scrollX - this.scrollDropDown), scrollY, false);
            if (Utils.checkMouse(x + 8, y + 33 + 12 * (scrollX - this.scrollDropDown), mouseX, mouseY, 142, 10) && !this.scrollingDropDown) {
                RenderSystem.enableBlend();
                matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], x + 8, y + 33 + 12 * (scrollX - this.scrollDropDown), 0, 166, 142, 10);
                Utils.drawToolTipList(matrixStack, title.tooltip(), mouseX, mouseY);
                this.isMouseCheck = true;
                if (this.checkMouse) {
                    if (title.getRequirement()) {
                        SetPlayerTitleSP.send(title);
                        Utils.playSound();
                    }

                    this.checkMouse = false;
                }
            }
        }

        if (Utils.checkMouse(x + 156, y + 33, mouseX, mouseY, 12, 106)) {
            this.isMouseCheck = true;
            if (this.checkMouse) {
                this.scrollingDropDown = overflow > 0;
                Utils.playSound();
                this.checkMouse = false;
            }
        }

        if (this.scrollingDropDown && overflow > 0) {
            this.scrollYOff = mouseY - 8;
            this.scrollYOff = Mth.clamp(this.scrollYOff, y + 33, y + 33 + 106 - 15);
            this.scrollDropDown = Math.round((float) overflow / 91.0F * (float) (this.scrollYOff - (y + 33)));
        } else if (overflow == 0) {
            this.scrollDropDown = 0;
            this.scrollYOff = y + 33;
            this.scrollingDropDown = false;
        }

        scrollX = x + 156;
        double scrollYF = overflow > 0 ? (91.0F / (float) overflow * (float) this.scrollDropDown) : 0.0D;
        scrollY = (int) ((double) (y + 33) + scrollYF);
        matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], scrollX, scrollY, 176, 0, 12, 15);
        matrixStack.blit(HandlerResources.SKILL_PAGE[1], x + 16, y + 144, 30, 167, 11, 11);
        if (Utils.checkMouse(x + 16, y + 144, mouseX, mouseY, 11, 11) && !this.scrollingDropDown) {
            matrixStack.blit(HandlerResources.SKILL_PAGE[1], x + 16, y + 144, 30, 179, 11, 11);
            List<Component> tooltipList = new ArrayList<>();
            tooltipList.add(Component.translatable("tooltip.sort.button.mod_names").withStyle(ChatFormatting.DARK_AQUA));
            tooltipList.add(Component.translatable("tooltip.sort.button.true").withStyle((Boolean) HandlerConfigClient.showTitleModName.get() ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
            tooltipList.add(Component.translatable("tooltip.sort.button.false").withStyle(!(Boolean) HandlerConfigClient.showTitleModName.get() ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
            Utils.drawToolTipList(matrixStack, tooltipList, mouseX, mouseY);
            this.isMouseCheck = true;
            if (this.checkMouse) {
                HandlerConfigClient.showTitleModName.set(!(Boolean) HandlerConfigClient.showTitleModName.get());
                Utils.playSound();
                this.checkMouse = false;
            }
        }
        matrixStack.blit(HandlerResources.SKILL_PAGE[1], x + 28, y + 144, 42, 167, 11, 11);
        if (Utils.checkMouse(x + 28, y + 144, mouseX, mouseY, 11, 11) && !this.scrollingDropDown) {
            matrixStack.blit(HandlerResources.SKILL_PAGE[1], x + 28, y + 144, 42, 179, 11, 11);
            List<Component> tooltipList = new ArrayList<>();
            tooltipList.add(Component.translatable("tooltip.sort.button.locked_titles").withStyle(ChatFormatting.DARK_AQUA));
            tooltipList.add(Component.translatable("tooltip.sort.button.true").withStyle((Boolean) HandlerConfigClient.showLockedTitles.get() ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
            tooltipList.add(Component.translatable("tooltip.sort.button.false").withStyle(!(Boolean) HandlerConfigClient.showLockedTitles.get() ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
            Utils.drawToolTipList(matrixStack, tooltipList, mouseX, mouseY);
            this.isMouseCheck = true;
            if (this.checkMouse) {
                HandlerConfigClient.showLockedTitles.set(!(Boolean) HandlerConfigClient.showLockedTitles.get());
                this.scrollDropDown = 0;
                this.scrollingDropDown = false;
                Utils.playSound();
                this.checkMouse = false;
            }
        }

        int backIconX = x + 141;
        int backIconY = y + 144;
        matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], backIconX, backIconY, 204, 0, 18, 10);
        if (Utils.checkMouse(backIconX, backIconY, mouseX, mouseY, 18, 10) && !this.scrollingDropDown) {
            matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], backIconX, backIconY, 222, 0, 18, 10);
            Utils.drawToolTip(matrixStack, Component.translatable("tooltip.title.back"), mouseX, mouseY);
            this.isMouseCheck = true;
            if (this.checkMouse) {
                this.skillActualPage = 0;
                this.selectedPage = 0;
                Utils.playSound();
                this.checkMouse = false;
            }
        }

        matrixStack.pose().popPose();
    }

    public void drawAptitudes(GuiGraphics matrixStack, int x, int y, int mouseX, int mouseY) {
        assert client.player != null;
        Utils.drawCenter(matrixStack, client.player.getName(), x + 88, y + 7);

        Utils.drawCenter(matrixStack, Component.translatable("screen.aptitude.level", client.player.experienceLevel, Utils.getPlayerXP(client.player)), x + 88, y + 17);

        Title titleKey = RegistryTitles.getTitle(AptitudeCapability.get().getPlayerTitle());
        String title = (titleKey != null) ? titleKey.getDisplayNameOrFallback() : "";
        int titleWidth = client.font.width(title) + 15;
        boolean checkButton = Utils.checkMouse(x + 88 - titleWidth / 2 - 2, y + 27, mouseX, mouseY, titleWidth + 2, 14);

        matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], x + 88 - titleWidth / 2 - 2, y + 27, checkButton ? 4 : 0, 214, 2, 14);
        matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], x + 88 - titleWidth / 2, y + 27, 0, checkButton ? 228 : 242, titleWidth, 14);
        matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], x + 88 + titleWidth / 2, y + 27, checkButton ? 6 : 2, 214, 2, 14);
        matrixStack.drawString(client.font, title, x + 88 - titleWidth / 2 + 2, y + 30, Color.WHITE.getRGB());
        matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], x + 88 + titleWidth / 2 - 10, y + 30, 8, 218, 8, 8);

        if (checkButton) {
            Utils.drawToolTip(matrixStack, Component.literal("Edit Title"), mouseX, mouseY);
            this.isMouseCheck = true;
            if (this.checkMouse) {
                this.selectedPage = 2;
                Utils.playSound();
                this.checkMouse = false;
            }
        }

        List<Aptitude> aptitudeList = new ArrayList<>(RegistryAptitudes.APTITUDES_REGISTRY.get().getValues().stream()
                .filter(aptitude -> aptitude.isEnabled() && !aptitude.isHidden() && VisibilityLockAPI.isVisible(client.player, aptitude.getName()))
                .toList());
        aptitudeList.sort(new SortAptitudeByDateCreated());
        int totalRows = (aptitudeList.size() + APTITUDE_COLUMNS - 1) / APTITUDE_COLUMNS;
        int maxScrollRows = Math.max(0, totalRows - APTITUDE_VISIBLE_ROWS);
        this.aptitudeScrollRows = Mth.clamp(this.aptitudeScrollRows, 0, maxScrollRows);

        int listX = x + APTITUDE_LIST_X_OFFSET;
        int listY = y + APTITUDE_LIST_Y_OFFSET;
        int scrollTrackX = x + APTITUDE_SCROLL_X_OFFSET;
        int scrollTrackY = y + APTITUDE_SCROLL_Y_OFFSET;
        boolean overTrackArea = Utils.checkMouse(scrollTrackX, scrollTrackY, mouseX, mouseY, APTITUDE_SCROLL_WIDTH, APTITUDE_SCROLL_HEIGHT);

        for (int localRow = 0; localRow < APTITUDE_VISIBLE_ROWS; localRow++) {
            int globalRow = this.aptitudeScrollRows + localRow;
            for (int column = 0; column < APTITUDE_COLUMNS; column++) {
                int index = globalRow * APTITUDE_COLUMNS + column;
                if (index >= aptitudeList.size()) {
                    continue;
                }

                Aptitude aptitude = aptitudeList.get(index);
                int aptitudeLevel = aptitude.getLevel();
                String aptitudeName = getAptitudeDisplayName(aptitude);
                String aptitudeAbbreviation = getAptitudeAbbreviation(aptitude);

                int xPos = listX + column * APTITUDE_COLUMN_SPACING;
                int yPos = listY + localRow * APTITUDE_ROW_SPACING;
                boolean overCell = Utils.checkMouse(xPos, yPos, mouseX, mouseY, APTITUDE_CELL_WIDTH, APTITUDE_CELL_HEIGHT) && !overTrackArea;

                if (overCell)
                    matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], xPos, yPos, 176, 0, 73, 26);

                matrixStack.blit(aptitude.getLockedTexture(), xPos + 5, yPos + 5, 0.0F, 0.0F, 16, 16, 16, 16);
                matrixStack.drawString(client.font, Component.literal(aptitudeAbbreviation).withStyle(ChatFormatting.BOLD), xPos + 24, yPos + 5, (new Color(240, 240, 240)).getRGB(), false);
                matrixStack.drawString(client.font, Component.translatable("screen.aptitude.experience", Utils.numberFormat(aptitudeLevel), aptitude.getLevelCap()), xPos + 24, yPos + 14, (new Color(170, 170, 170)).getRGB(), false);

                if (overCell) {
                    Utils.drawToolTip(matrixStack, Component.literal(aptitudeName), mouseX, mouseY);
                    this.isMouseCheck = true;
                    if (this.checkMouse) {
                        this.tick = this.maxTick / 2;
                        this.b = true;
                        this.selectedAptitude = aptitude.getName();
                        this.selectedPage = 1;
                        Utils.playSound();
                        this.checkMouse = false;
                    }
                }
            }
        }

        int knobTravel = APTITUDE_SCROLL_HEIGHT - APTITUDE_SCROLL_KNOB_HEIGHT;
        if (maxScrollRows > 0) {
            if (this.aptitudeScrolling) {
                int draggedKnobY = mouseY - APTITUDE_SCROLL_KNOB_HEIGHT / 2;
                this.aptitudeScrollKnobY = Mth.clamp(draggedKnobY, scrollTrackY, scrollTrackY + knobTravel);
                float progress = (float) (this.aptitudeScrollKnobY - scrollTrackY) / knobTravel;
                this.aptitudeScrollRows = Mth.clamp(Math.round(progress * maxScrollRows), 0, maxScrollRows);
            }

            float scrollProgress = (float) this.aptitudeScrollRows / maxScrollRows;
            this.aptitudeScrollKnobY = scrollTrackY + Math.round(scrollProgress * knobTravel);

            boolean overTrack = Utils.checkMouse(scrollTrackX, scrollTrackY, mouseX, mouseY, APTITUDE_SCROLL_WIDTH, APTITUDE_SCROLL_HEIGHT);
            boolean overKnob = Utils.checkMouse(scrollTrackX, this.aptitudeScrollKnobY, mouseX, mouseY, APTITUDE_SCROLL_WIDTH, APTITUDE_SCROLL_KNOB_HEIGHT);

            if (overTrack) {
                this.isMouseCheck = true;
                if (this.checkMouse) {
                    this.aptitudeScrolling = true;
                    if (!overKnob) {
                        int clickedKnobY = mouseY - APTITUDE_SCROLL_KNOB_HEIGHT / 2;
                        this.aptitudeScrollKnobY = Mth.clamp(clickedKnobY, scrollTrackY, scrollTrackY + knobTravel);
                        float progress = (float) (this.aptitudeScrollKnobY - scrollTrackY) / knobTravel;
                        this.aptitudeScrollRows = Mth.clamp(Math.round(progress * maxScrollRows), 0, maxScrollRows);
                    }
                    Utils.playSound();
                    this.checkMouse = false;
                }
            }

            matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], scrollTrackX, this.aptitudeScrollKnobY, 176, 0, 12, 15);
        } else {
            this.aptitudeScrolling = false;
            this.aptitudeScrollKnobY = scrollTrackY;
        }
    }

    public void drawSkills(GuiGraphics matrixStack, int x, int y, int mouseX, int mouseY) {
        assert client.player != null;
        AptitudeCapability capability = AptitudeCapability.get();
        Aptitude aptitude = RegistryAptitudes.getAptitude(this.selectedAptitude);
        if (aptitude == null || !aptitude.isEnabled() || !VisibilityLockAPI.isVisible(client.player, aptitude.getName())) {
            this.selectedPage = 0;
            this.selectedAptitude = "";
            return;
        }
        int aptitudeLevel = aptitude.getLevel();
        int maxAptitudeLevel = aptitude.getLevelCap();
        String aptitudeName = getAptitudeDisplayName(aptitude);

        String rank = aptitude.getRank(aptitudeLevel).getString();

        matrixStack.blit(aptitude.getLockedTexture(), x + 12, y + 9, 0.0F, 0.0F, 16, 16, 16, 16);

        matrixStack.drawString(client.font, Component.literal(aptitudeName).withStyle(ChatFormatting.BOLD), x + 34, y + 8, Utils.FONT_COLOR, false);
        matrixStack.drawString(client.font, Component.translatable("screen.skill.level_and_rank", Utils.numberFormat(aptitudeLevel), maxAptitudeLevel, rank), x + 34, y + 18, Utils.FONT_COLOR, false);
        int spAvailable = capability.getAptitudeSkillPointsAvailable(aptitude);
        int spTotal = capability.getAptitudeSkillPointsTotal(aptitude);
        int spSpent = capability.getAptitudeSkillPointsSpent(aptitude);
        Component spFooter = Component.translatable("tooltip.sp.footer", spAvailable);
        int spFooterX = x + 12;
        int spFooterY = y + SKILL_POINTS_Y_OFFSET;
        int spFooterWidth = client.font.width(spFooter);
        int spFooterColor = spAvailable > 0 ? 0xC2F0C2 : 0xF3B4B4;
        matrixStack.drawString(client.font, spFooter, spFooterX + 1, spFooterY + 1, 0x1A1A1A, false);
        matrixStack.drawString(client.font, spFooter, spFooterX, spFooterY, spFooterColor, false);

        if (Utils.checkMouse(spFooterX - 1, spFooterY - 1, mouseX, mouseY, spFooterWidth + 2, 10)) {
            Utils.drawToolTip(matrixStack, Component.translatable("tooltip.sp.badge", spAvailable, spTotal, spSpent), mouseX, mouseY);
            this.isMouseCheck = true;
        }

        int sortX = x + 112;
        int sortY = y + 33;
        int sortStep = 12;

        matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], sortX, sortY, 30, 167, 11, 11);
        if (Utils.checkMouse(sortX, sortY, mouseX, mouseY, 11, 11)) {
            matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], sortX, sortY, 30, 179, 11, 11);
            List<Component> tooltipList = new ArrayList<>();
            tooltipList.add(Component.translatable("tooltip.sort.button.mod_names").withStyle(ChatFormatting.DARK_AQUA));
            tooltipList.add(Component.translatable("tooltip.sort.button.true").withStyle(HandlerConfigClient.showSkillModName.get() ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
            tooltipList.add(Component.translatable("tooltip.sort.button.false").withStyle(!HandlerConfigClient.showSkillModName.get() ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
            Utils.drawToolTipList(matrixStack, tooltipList, mouseX, mouseY);
            this.isMouseCheck = true;
            if (this.checkMouse) {
                HandlerConfigClient.showSkillModName.set(!HandlerConfigClient.showSkillModName.get());
                Utils.playSound();
                this.checkMouse = false;
            }
        }

        matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], sortX + sortStep, sortY, 42, 167, 11, 11);
        if (Utils.checkMouse(sortX + sortStep, sortY, mouseX, mouseY, 11, 11)) {
            matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], sortX + sortStep, sortY, 42, 179, 11, 11);
            List<Component> tooltipList = new ArrayList<>();
            tooltipList.add(Component.translatable("tooltip.sort.button.passives").withStyle(ChatFormatting.DARK_AQUA));
            for (int m = 0; m < (SortPassives.values()).length; m++) {
                ChatFormatting color = (SortPassives.values()[m] == HandlerConfigClient.sortPassive.get()) ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY;
                tooltipList.add(Component.translatable((SortPassives.values()[m]).order).withStyle(color));
            }
            Utils.drawToolTipList(matrixStack, tooltipList, mouseX, mouseY);
            this.isMouseCheck = true;
            if (this.checkMouse) {
                HandlerConfigClient.sortPassive.set(SortPassives.fromIndex(HandlerConfigClient.sortPassive.get().index + 1));
                Utils.playSound();
                this.checkMouse = false;
            }
        }

        matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], sortX + sortStep * 2, sortY, 54, 167, 11, 11);
        if (Utils.checkMouse(sortX + sortStep * 2, sortY, mouseX, mouseY, 11, 11)) {
            matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], sortX + sortStep * 2, sortY, 54, 179, 11, 11);
            List<Component> tooltipList = new ArrayList<>();
            tooltipList.add(Component.translatable("tooltip.sort.button.skills").withStyle(ChatFormatting.DARK_AQUA));
            for (int m = 0; m < (SortSkills.values()).length; m++) {
                ChatFormatting color = (SortSkills.values()[m] == HandlerConfigClient.sortSkill.get()) ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY;
                tooltipList.add(Component.translatable((SortSkills.values()[m]).order).withStyle(color));
            }
            Utils.drawToolTipList(matrixStack, tooltipList, mouseX, mouseY);
            this.isMouseCheck = true;
            if (this.checkMouse) {
                HandlerConfigClient.sortSkill.set(SortSkills.fromIndex(HandlerConfigClient.sortSkill.get().index + 1));
                Utils.playSound();
                this.checkMouse = false;
            }
        }

        List<Passive> listPassives = new ArrayList<>(aptitude.getPassives(aptitude));
        List<Skill> listSkills = new ArrayList<>(aptitude.getSkills(aptitude));

        switch (HandlerConfigClient.sortPassive.get()) {
            case ByName:
                listPassives.sort(new SortPassiveByName());
                break;
            case ByReverseName:
                listPassives.sort((new SortPassiveByName()).reversed());
                break;
        }

        switch (HandlerConfigClient.sortSkill.get()) {
            case ByName:
                listSkills.sort(new SortSkillByName());
                break;
            case ByReverseName:
                listSkills.sort((new SortSkillByName()).reversed());
                break;
            case ByLevel:
                listSkills.sort(new SortSkillList());
                break;
        }

        List<Object> sorted = new ArrayList<>();
        sorted.addAll(listPassives);
        sorted.addAll(listSkills);
        List<List<Object>> listSorted = new ArrayList<>();
        for (int start = 0; start < sorted.size(); start += 5) {
            int end = Math.min(start + 5, sorted.size());
            listSorted.add(new ArrayList<>(sorted.subList(start, end)));
        }

        this.skillSize = Math.max(0, listSorted.size() - 1);
        this.skillSizePage = Math.max(0, (listSorted.size() - 1) / 4);
        this.skillActualPage = Mth.clamp(this.skillActualPage, 0, this.skillSizePage);

        int pageStart = this.skillActualPage * 4;
        int pageEnd = Math.min(pageStart + 4, listSorted.size());
        List<List<Object>> newPage = (pageStart < pageEnd) ? listSorted.subList(pageStart, pageEnd) : List.of();
        AtomicInteger in = new AtomicInteger(-1);
        newPage.forEach(list -> {
            in.addAndGet(1);

            createList(list, capability, matrixStack, x, y + in.get() * 26 - newPage.size() * 13, mouseX, mouseY);
        });
        if (this.tick >= this.maxTick) {
            this.b = !this.b;
            this.tick = 0;
        }
        int j = (aptitudeLevel < maxAptitudeLevel) ? (this.b ? 6 : 0) : 12;

        matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], x + 153, y + 14, 177 + j, 1, 6, 6);

        int requiredLevels = AptitudeLevelUpSP.requiredExperienceLevels(aptitudeLevel, aptitude);
        int requiredPoints = AptitudeLevelUpSP.requiredPoints(aptitudeLevel, aptitude);
        boolean canLevelUpAptitude = (client.player.isCreative()
                || Utils.getExperienceForLevel(requiredLevels) <= Utils.getPlayerXP(client.player)
                || requiredLevels <= client.player.experienceLevel);

        if (Utils.checkMouse(x + 149, y + 10, mouseX, mouseY, 14, 14)) {
            if (AptitudeCapability.get(client.player).getGlobalLevel() >= HandlerCommonConfig.HANDLER.instance().playersMaxGlobalLevel) {
                Utils.drawToolTip(matrixStack,
                        Component.translatable("tooltip.aptitude.global_max_level", HandlerCommonConfig.HANDLER.instance().playersMaxGlobalLevel)
                                .withStyle(ChatFormatting.RED),
                        mouseX,
                        mouseY);
            } else if (aptitudeLevel < maxAptitudeLevel) {
                ChatFormatting color = canLevelUpAptitude ? ChatFormatting.GREEN : ChatFormatting.RED;
                Utils.drawToolTip(matrixStack, Component.translatable("tooltip.aptitude.level_up", Component.literal(String.valueOf(requiredLevels)).withStyle(color),
                        Component.literal(String.valueOf(requiredPoints)).withStyle(color),
                        Component.literal(aptitudeName).withStyle(color)).withStyle(ChatFormatting.GRAY), mouseX, mouseY);
                this.tick = this.maxTick - 5;
                if (canLevelUpAptitude) {
                    this.b = true;
                    this.isMouseCheck = true;
                    if (this.checkMouse) {
                        Utils.playSound();
                        if (KubeJSIntegration.isModLoaded()) {
                            boolean cancelled = new KubeJSIntegration().postLevelUpEvent(client.player, aptitude);

                            if (!cancelled) {
                                AptitudeLevelUpSP.send(aptitude);
                            }
                        }
                        else {
                            AptitudeLevelUpSP.send(aptitude);
                        }

                        this.checkMouse = false;
                    }
                } else {
                    this.b = false;
                }
            } else {
                Utils.drawToolTip(matrixStack, Component.translatable("tooltip.aptitude.max_level", Component.literal(aptitudeName).withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.GRAY), mouseX, mouseY);
            }

        } else if (canLevelUpAptitude) {
            this.tick++;
        } else {
            this.b = false;
        }


        int backIconX = x + 141;
        int backIconY = y + SKILL_BOTTOM_ROW_Y_OFFSET;

        matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], backIconX, backIconY, 204, 0, 18, 10);
        if (Utils.checkMouse(backIconX, backIconY, mouseX, mouseY, 18, 10)) {
            matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], backIconX, backIconY, 222, 0, 18, 10);
            Utils.drawToolTip(matrixStack, Component.translatable("tooltip.skill.back"), mouseX, mouseY);
            this.isMouseCheck = true;
            if (this.checkMouse) {
                this.skillActualPage = 0;
                this.selectedPage = 0;
                Utils.playSound();
                this.checkMouse = false;
            }
        }

        if (this.skillSizePage > 0) {
            String pageNumber = (this.skillActualPage + 1) + "/" + (this.skillSizePage + 1);
            int pageIconX = x + 88 - client.font.width(pageNumber) / 2;
            int pageIconY = y + SKILL_BOTTOM_ROW_Y_OFFSET;

            matrixStack.drawString(client.font, pageNumber, pageIconX, pageIconY + 2, Color.WHITE.getRGB(), false);

            if (this.skillActualPage > 0) {
                boolean select = Utils.checkMouse(pageIconX - 12, pageIconY, mouseX, mouseY, 7, 11);
                matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], pageIconX - 12, pageIconY, 241, select ? 12 : 0, 7, 11);
                if (select) {
                    Utils.drawToolTip(matrixStack, Component.translatable("tooltip.skill.previous"), mouseX, mouseY);
                    this.isMouseCheck = true;
                    if (this.checkMouse) {
                        this.skillActualPage--;
                        Utils.playSound();
                        this.checkMouse = false;
                    }
                }
            }

            if (this.skillActualPage < this.skillSizePage) {
                boolean select = Utils.checkMouse(pageIconX + client.font.width(pageNumber) + 5, pageIconY, mouseX, mouseY, 7, 11);
                matrixStack.blit(HandlerResources.SKILL_PAGE[this.selectedPage], pageIconX + client.font.width(pageNumber) + 5, pageIconY, 249, select ? 12 : 0, 7, 11);
                if (select) {
                    Utils.drawToolTip(matrixStack, Component.translatable("tooltip.skill.next"), mouseX, mouseY);
                    this.isMouseCheck = true;
                    if (this.checkMouse) {
                        this.skillActualPage++;
                        Utils.playSound();
                        this.checkMouse = false;
                    }
                }
            }
        }
    }

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


    public boolean isPauseScreen() {
        return false;
    }


    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.scrollingDropDown) this.scrollingDropDown = false;
        if (this.aptitudeScrolling) this.aptitudeScrolling = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }


    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (this.selectedPage == 0) {
            int x = (this.width - 176) / 2;
            int y = (this.height - 166) / 2;

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
                return true;
            }
        }

        if (this.selectedPage == 2) {
            int step = amount > 0 ? -1 : 1;
            this.scrollDropDown = Math.max(0, this.scrollDropDown + step);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, amount);
    }


    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.isMouseCheck) this.checkMouse = true;
        DrawTabs.mouseClicked(button);
        return super.mouseClicked(mouseX, mouseY, button);
    }


    public boolean charTyped(char chr, int modifiers) {
        if (this.selectedPage == 2) {
            boolean b = this.searchTitle.charTyped(chr, modifiers);
            this.searchValue = this.searchTitle.getValue();
            return b;
        }
        return super.charTyped(chr, modifiers);
    }


    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.selectedPage == 2) {
            this.searchTitle.keyPressed(keyCode, scanCode, modifiers);
            boolean b = (keyCode != 256 || super.keyPressed(keyCode, scanCode, modifiers));
            this.searchValue = this.searchTitle.getValue();
            return b;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }


    public void onClose() {
        this.checkMouse = false;
        this.skillActualPage = 0;
        this.aptitudeActualPage = 0;
        this.aptitudeScrollRows = 0;
        this.aptitudeScrolling = false;
        this.selectedPage = 0;
        this.searchValue = "";
        this.searchTitle.setValue("");
        DrawTabs.onClose();
        super.onClose();
    }

    private static String getAptitudeDisplayName(Aptitude aptitude) {
        return aptitude.getDisplayNameOrFallback();
    }

    private static String getAptitudeAbbreviation(Aptitude aptitude) {
        return aptitude.getAbbreviationOrFallback();
    }

    private void drawAptitudeBackground(GuiGraphics matrixStack, Aptitude aptitude, int x, int y) {
        if (aptitude.background == null) {
            return;
        }

        int bgX = x + 7;
        int bgY = y + 30;
        int bgWidth = 160;
        int bgHeight = 128;
        int repeat = aptitude.getBackgroundRepeat();

        if (repeat <= 0) {
            matrixStack.blit(aptitude.background, bgX, bgY, 0.0F, 0.0F, bgWidth, bgHeight, 16, 16);
            return;
        }

        if (repeat == 1) {
            matrixStack.blit(aptitude.background, bgX, bgY, 0.0F, 0.0F, bgWidth, bgHeight, bgWidth, bgHeight);
            return;
        }

        int clampedRepeat = Mth.clamp(repeat, 1, 64);
        for (int row = 0; row < clampedRepeat; row++) {
            int startY = bgY + Math.round((float) (row * bgHeight) / clampedRepeat);
            int endY = bgY + Math.round((float) ((row + 1) * bgHeight) / clampedRepeat);
            int cellHeight = Math.max(1, endY - startY);

            for (int col = 0; col < clampedRepeat; col++) {
                int startX = bgX + Math.round((float) (col * bgWidth) / clampedRepeat);
                int endX = bgX + Math.round((float) ((col + 1) * bgWidth) / clampedRepeat);
                int cellWidth = Math.max(1, endX - startX);
                matrixStack.blit(aptitude.background, startX, startY, 0.0F, 0.0F, cellWidth, cellHeight, cellWidth, cellHeight);
            }
        }
    }

    public static class SortAptitudeByDateCreated
            implements Comparator<Aptitude> {
        public int compare(Aptitude date1, Aptitude date2) {
            return date1.index - date2.index;
        }
    }

    public static class SortPassiveByName
            implements Comparator<Passive> {
        public int compare(Passive name1, Passive name2) {
            return name1.getName().compareTo(name2.getName());
        }
    }

    public static class SortSkillByName
            implements Comparator<Skill> {
        public int compare(Skill name1, Skill name2) {
            return name1.getName().compareTo(name2.getName());
        }
    }

    public static class SortSkillList
            implements Comparator<Skill> {
        public int compare(Skill lvl1, Skill lvl2) {
            return lvl1.requiredLevel - lvl2.requiredLevel;
        }
    }

    public static class SortTitleByName
            implements Comparator<Title> {
        public int compare(Title name1, Title name2) {
            return name1.getName().compareTo(name2.getName());
        }
    }
}


