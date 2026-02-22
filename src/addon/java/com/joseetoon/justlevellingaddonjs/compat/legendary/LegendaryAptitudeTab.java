package com.joseetoon.justlevellingaddonjs.compat.legendary;

import com.seniors.justlevelingfork.client.screen.JustLevelingScreen;
import com.seniors.justlevelingfork.registry.RegistryItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import sfiomn.legendarytabs.api.tabs_menu.TabBase;
import sfiomn.legendarytabs.api.tabs_menu.TabsMenu;

public class LegendaryAptitudeTab extends TabBase {
    private static final ResourceLocation TAB_ICONS = new ResourceLocation("legendarytabs", "textures/gui/tab_menu_buttons.png");

    @Override
    public void openTargetScreen(Player player) {
        Minecraft.getInstance().setScreen(new JustLevelingScreen());
    }

    @Override
    public boolean isEnabled(Player player) {
        return true;
    }

    @Override
    public void initTabOnScreens() {
        TabsMenu.addTabToScreen(this, InventoryScreen.class, p -> 176, p -> 166, 15);
    }

    @Override
    public void render(GuiGraphics gui, int x, int y, boolean hover) {
        int texOffsetX = hover ? 54 : 0;
        gui.blit(TAB_ICONS, x, y, texOffsetX, 0, TAB_WIDTH, TAB_HEIGHT);
        ItemStack icon = RegistryItems.LEVELING_BOOK.get().getDefaultInstance();
        gui.renderItem(icon, x + 5, y + 3);
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        return currentScreen instanceof JustLevelingScreen;
    }

    @Override
    public Component getTooltip() {
        return Component.translatable("screen.aptitude.title");
    }
}
