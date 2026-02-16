package com.seniors.justlevelingfork.kubejs.events;

import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.world.entity.player.Player;

public class LevelUpEvent extends EventJS {
    private final Player player;
    private final Aptitude aptitude;
    private final int previousLevel;
    private final int newLevel;

    private boolean cancelled = false;

    public LevelUpEvent(Player player, Aptitude aptitude) {
        this(player, aptitude, -1, -1);
    }

    public LevelUpEvent(Player player, Aptitude aptitude, int previousLevel, int newLevel) {
        this.player = player;
        this.aptitude = aptitude;
        this.previousLevel = previousLevel;
        this.newLevel = newLevel;
    }

    public Player getPlayer() {
        return player;
    }

    public Aptitude getAptitude(){
        return aptitude;
    }

    public int getPreviousLevel() {
        return previousLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }

    public boolean getCancelled(){
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
