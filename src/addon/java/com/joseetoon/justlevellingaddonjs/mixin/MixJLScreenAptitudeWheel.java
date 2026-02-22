package com.joseetoon.justlevellingaddonjs.mixin;

import com.joseetoon.justlevellingaddonjs.compat.JLScreenCompatAccess;
import com.seniors.justlevelingfork.client.core.Utils;
import com.seniors.justlevelingfork.client.screen.JustLevelingScreen;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = JustLevelingScreen.class, remap = false)
public class MixJLScreenAptitudeWheel {
    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true, require = 0)
    private void jlforkaddon$mouseScrolled(double mouseX, double mouseY, double amount, CallbackInfoReturnable<Boolean> cir) {
        if (JLScreenCompatAccess.getInt(this, "selectedPage", -1) != 0) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (player == null) {
            return;
        }

        int panelWidth = JLScreenCompatAccess.getStaticInt(JustLevelingScreen.class, "BASE_PANEL_WIDTH", 176);
        int panelHeight = JLScreenCompatAccess.getStaticInt(JustLevelingScreen.class, "BASE_PANEL_HEIGHT", 166);
        int columns = JLScreenCompatAccess.getStaticInt(JustLevelingScreen.class, "APTITUDE_COLUMNS", 2);
        int visibleRows = JLScreenCompatAccess.getStaticInt(JustLevelingScreen.class, "APTITUDE_VISIBLE_ROWS", 4);
        int columnSpacing = JLScreenCompatAccess.getStaticInt(JustLevelingScreen.class, "APTITUDE_COLUMN_SPACING", 77);
        int cellWidth = JLScreenCompatAccess.getStaticInt(JustLevelingScreen.class, "APTITUDE_CELL_WIDTH", 74);
        int listXOffset = JLScreenCompatAccess.getStaticInt(JustLevelingScreen.class, "APTITUDE_LIST_X_OFFSET", 12);
        int listYOffset = JLScreenCompatAccess.getStaticInt(JustLevelingScreen.class, "APTITUDE_LIST_Y_OFFSET", 50);
        int scrollXOffset = JLScreenCompatAccess.getStaticInt(JustLevelingScreen.class, "APTITUDE_SCROLL_X_OFFSET", 156);
        int scrollYOffset = JLScreenCompatAccess.getStaticInt(JustLevelingScreen.class, "APTITUDE_SCROLL_Y_OFFSET", 50);
        int scrollWidth = JLScreenCompatAccess.getStaticInt(JustLevelingScreen.class, "APTITUDE_SCROLL_WIDTH", 12);
        int scrollHeight = JLScreenCompatAccess.getStaticInt(JustLevelingScreen.class, "APTITUDE_SCROLL_HEIGHT", 112);

        int x = (client.getWindow().getGuiScaledWidth() - panelWidth) / 2;
        int y = (client.getWindow().getGuiScaledHeight() - panelHeight) / 2;
        int listX = x + listXOffset;
        int listY = y + listYOffset;
        int listWidth = columnSpacing * (columns - 1) + cellWidth;
        int scrollTrackX = x + scrollXOffset;
        int scrollTrackY = y + scrollYOffset;

        boolean insideAptitudes = Utils.checkMouse(listX, listY, (int) mouseX, (int) mouseY, listWidth, scrollHeight)
                || Utils.checkMouse(scrollTrackX, scrollTrackY, (int) mouseX, (int) mouseY, scrollWidth, scrollHeight);
        if (!insideAptitudes) {
            return;
        }

        List<Aptitude> aptitudes = JLScreenCompatAccess.getVisibleAptitudes(player);
        int totalRows = (aptitudes.size() + columns - 1) / columns;
        int maxScrollRows = Math.max(0, totalRows - visibleRows);
        if (maxScrollRows > 0) {
            int currentRows = JLScreenCompatAccess.getInt(this, "aptitudeScrollRows", 0);
            int step = amount > 0 ? -1 : 1;
            int nextRows = Mth.clamp(currentRows + step, 0, maxScrollRows);
            JLScreenCompatAccess.setInt(this, "aptitudeScrollRows", nextRows);
        }

        cir.setReturnValue(true);
    }
}
