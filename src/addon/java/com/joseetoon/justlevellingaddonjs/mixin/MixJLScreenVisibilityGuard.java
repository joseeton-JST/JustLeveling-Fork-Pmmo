package com.joseetoon.justlevellingaddonjs.mixin;

import com.joseetoon.justlevellingaddonjs.compat.AptitudeCompat;
import com.joseetoon.justlevellingaddonjs.compat.JLScreenCompatAccess;
import com.joseetoon.justlevellingaddonjs.config.AddonClientRuntimeSettings;
import com.joseetoon.justlevellingaddonjs.kubejs.VisibilityLockAPI;
import com.seniors.justlevelingfork.client.core.Utils;
import com.seniors.justlevelingfork.client.screen.JustLevelingScreen;
import com.seniors.justlevelingfork.handler.HandlerResources;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = JustLevelingScreen.class, remap = false)
public class MixJLScreenVisibilityGuard {
    @Inject(method = "drawSkills", at = @At("HEAD"), cancellable = true, require = 0)
    private void jlforkaddon$drawSkillsGuard(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            resetToAptitudesPage();
            ci.cancel();
            return;
        }

        String aptitudeKey = JLScreenCompatAccess.getString(this, "selectedAptitude", "");
        Aptitude aptitude = RegistryAptitudes.getAptitude(aptitudeKey);
        if (aptitude == null
                || !AptitudeCompat.isEnabled(aptitude)
                || !VisibilityLockAPI.isVisible(player, AptitudeCompat.getName(aptitude))) {
            resetToAptitudesPage();
            ci.cancel();
            return;
        }

        int entries = jlforkaddon$countEntries(aptitude);
        if (entries <= 0) {
            JLScreenCompatAccess.setInt(this, "selectedPage", 1);
            JLScreenCompatAccess.setInt(this, "skillSize", 0);
            JLScreenCompatAccess.setInt(this, "skillSizePage", 0);
            JLScreenCompatAccess.setInt(this, "skillActualPage", 0);

            if (AddonClientRuntimeSettings.current().showEmptyAptitudeOverlay()) {
                jlforkaddon$renderEmptyState(graphics, x, y, mouseX, mouseY, aptitude);
            }
            ci.cancel();
            return;
        }

        int groupedRows = (entries + 4) / 5;
        int pageCount = (groupedRows + 3) / 4;
        int safeSkillPage = Mth.clamp(JLScreenCompatAccess.getInt(this, "skillActualPage", 0), 0, Math.max(0, pageCount - 1));

        JLScreenCompatAccess.setInt(this, "skillSize", Math.max(0, groupedRows - 1));
        JLScreenCompatAccess.setInt(this, "skillSizePage", Math.max(0, pageCount - 1));
        JLScreenCompatAccess.setInt(this, "skillActualPage", safeSkillPage);
    }

    private void jlforkaddon$renderEmptyState(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, Aptitude aptitude) {
        Minecraft client = Minecraft.getInstance();
        if (client.font == null) {
            return;
        }

        String aptitudeName = AptitudeCompat.getDisplayNameOrFallback(aptitude);
        Component title = Component.literal(aptitudeName);
        Component message = Component.literal("No skills or passives available");
        int titleWidth = client.font.width(title);
        int messageWidth = client.font.width(message);

        graphics.drawString(client.font, title, x + 88 - titleWidth / 2, y + 62, 0xEDEDED, false);
        graphics.drawString(client.font, message, x + 88 - messageWidth / 2, y + 76, 0xBDBDBD, false);

        int backIconX = x + 141;
        int backIconY = y + 144;
        graphics.blit(HandlerResources.SKILL_PAGE[1], backIconX, backIconY, 204, 0, 18, 10);
        if (Utils.checkMouse(backIconX, backIconY, mouseX, mouseY, 18, 10)) {
            graphics.blit(HandlerResources.SKILL_PAGE[1], backIconX, backIconY, 222, 0, 18, 10);
            Utils.drawToolTip(graphics, Component.translatable("tooltip.title.back"), mouseX, mouseY);
            JLScreenCompatAccess.setBoolean(this, "isMouseCheck", true);
            if (JLScreenCompatAccess.getBoolean(this, "checkMouse", false)) {
                JLScreenCompatAccess.setInt(this, "skillActualPage", 0);
                JLScreenCompatAccess.setInt(this, "selectedPage", 0);
                JLScreenCompatAccess.setString(this, "selectedAptitude", "");
                JLScreenCompatAccess.setBoolean(this, "checkMouse", false);
                Utils.playSound();
            }
        }
    }

    private void resetToAptitudesPage() {
        JLScreenCompatAccess.setInt(this, "skillSize", 0);
        JLScreenCompatAccess.setInt(this, "skillSizePage", 0);
        JLScreenCompatAccess.setInt(this, "skillActualPage", 0);
        JLScreenCompatAccess.setInt(this, "selectedPage", 0);
        JLScreenCompatAccess.setString(this, "selectedAptitude", "");
    }

    private static int jlforkaddon$countEntries(Aptitude aptitude) {
        List<?> passives = aptitude.getPassives(aptitude);
        List<?> skills = aptitude.getSkills(aptitude);
        int passiveSize = passives == null ? 0 : passives.size();
        int skillSize = skills == null ? 0 : skills.size();
        return passiveSize + skillSize;
    }

    @Redirect(
            method = "drawSkills",
            at = @At(value = "INVOKE", target = "Ljava/util/List;subList(II)Ljava/util/List;"),
            require = 0
    )
    private List<?> jlforkaddon$safeSubList(List<?> list, int fromIndex, int toIndex) {
        int size = list == null ? 0 : list.size();
        int safeFrom = Mth.clamp(fromIndex, 0, size);
        int safeTo = Mth.clamp(toIndex, safeFrom, size);
        return list.subList(safeFrom, safeTo);
    }
}
