package com.joseetoon.justlevellingaddonjs.mixin;

import com.joseetoon.justlevellingaddonjs.compat.JLScreenCompatAccess;
import com.seniors.justlevelingfork.client.screen.JustLevelingScreen;
import com.seniors.justlevelingfork.handler.HandlerResources;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = JustLevelingScreen.class, remap = false)
public class MixJLScreenAptitudeThumbRender {
    @Inject(method = "drawAptitudes", at = @At("TAIL"), require = 0)
    private void jlforkaddon$drawAptitudeThumb(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, CallbackInfo ci) {
        if (JLScreenCompatAccess.getInt(this, "selectedPage", -1) != 0) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        int columns = JLScreenCompatAccess.getStaticInt(JustLevelingScreen.class, "APTITUDE_COLUMNS", 2);
        int visibleRows = JLScreenCompatAccess.getStaticInt(JustLevelingScreen.class, "APTITUDE_VISIBLE_ROWS", 4);
        List<Aptitude> aptitudes = JLScreenCompatAccess.getVisibleAptitudes(player);
        int totalRows = (aptitudes.size() + columns - 1) / columns;
        if (totalRows <= visibleRows) {
            return;
        }

        int scrollXOffset = JLScreenCompatAccess.getStaticInt(JustLevelingScreen.class, "APTITUDE_SCROLL_X_OFFSET", 156);
        int scrollYOffset = JLScreenCompatAccess.getStaticInt(JustLevelingScreen.class, "APTITUDE_SCROLL_Y_OFFSET", 50);
        int knobY = JLScreenCompatAccess.getInt(this, "aptitudeScrollKnobY", y + scrollYOffset);

        graphics.blit(HandlerResources.SKILL_PAGE[2], x + scrollXOffset, knobY, 176, 0, 12, 15);
    }
}
