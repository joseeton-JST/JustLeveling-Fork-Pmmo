package com.seniors.justlevelingfork.integration;

import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;

public class KubeJSIntegration {

    public static boolean isModLoaded() {
        return ModList.get().isLoaded("kubejs");
    }

    public boolean postLevelUpEvent(Player player, Aptitude aptitude) {
        return postLevelUp(player, aptitude, -1, -1, "APTITUDE_LEVELUP", false);
    }

    public boolean postLevelUpServerEvent(Player player, Aptitude aptitude, int previousLevel, int newLevel) {
        return postLevelUp(player, aptitude, previousLevel, newLevel, "APTITUDE_LEVELUP_SERVER", true);
    }

    private boolean postLevelUp(Player player, Aptitude aptitude, int previousLevel, int newLevel, String eventFieldName, boolean includeLevels) {
        // Required in case KubeJS is not present
        // In a future I should move this into a different mod
        try {
            Class<?> eventClass = Class.forName("com.seniors.justlevelingfork.kubejs.events.LevelUpEvent");
            Object eventInstance;
            if (includeLevels) {
                eventInstance = eventClass.getConstructor(Player.class, Aptitude.class, int.class, int.class)
                        .newInstance(player, aptitude, previousLevel, newLevel);
            } else {
                eventInstance = eventClass.getConstructor(Player.class, Aptitude.class).newInstance(player, aptitude);
            }

            Class<?> customEventsClass = Class.forName("com.seniors.justlevelingfork.kubejs.events.CustomEvents");
            Object aptitudeLevelUpField = customEventsClass.getField(eventFieldName).get(null);
            Method postMethod = aptitudeLevelUpField.getClass().getMethod("post", Class.forName("dev.latvian.mods.kubejs.event.EventJS"));

            postMethod.invoke(aptitudeLevelUpField, eventInstance);

            return (boolean) eventClass.getMethod("getCancelled").invoke(eventInstance);
        } catch (Exception e) {
            return false;
        }
    }

}
