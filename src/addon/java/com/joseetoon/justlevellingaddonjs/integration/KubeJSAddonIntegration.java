package com.joseetoon.justlevellingaddonjs.integration;

import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.joseetoon.justlevellingaddonjs.kubejs.events.CustomEvents;
import com.joseetoon.justlevellingaddonjs.kubejs.events.LevelUpEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;

public final class KubeJSAddonIntegration {
    private KubeJSAddonIntegration() {
    }

    public static boolean isKubeJSLoaded() {
        return ModList.get().isLoaded("kubejs");
    }

    public static boolean postLevelUp(Player player, Aptitude aptitude) {
        if (!isKubeJSLoaded()) {
            return false;
        }

        LevelUpEvent event = new LevelUpEvent(player, aptitude);
        CustomEvents.APTITUDE_LEVELUP.post(event);
        return event.getCancelled();
    }

    public static boolean postLevelUpServer(Player player, Aptitude aptitude, int previousLevel, int newLevel) {
        if (!isKubeJSLoaded()) {
            return false;
        }

        LevelUpEvent event = new LevelUpEvent(player, aptitude, previousLevel, newLevel);
        CustomEvents.APTITUDE_LEVELUP_SERVER.post(event);
        return event.getCancelled();
    }
}
