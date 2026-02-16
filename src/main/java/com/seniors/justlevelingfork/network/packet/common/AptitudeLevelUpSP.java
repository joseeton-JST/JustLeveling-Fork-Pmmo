package com.seniors.justlevelingfork.network.packet.common;

import com.seniors.justlevelingfork.JustLevelingFork;
import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import com.seniors.justlevelingfork.handler.HandlerCommonConfig;
import com.seniors.justlevelingfork.integration.KubeJSIntegration;
import com.seniors.justlevelingfork.kubejs.LevelLockAPI;
import com.seniors.justlevelingfork.kubejs.SkillChangeAPI;
import com.seniors.justlevelingfork.network.ServerNetworking;
import com.seniors.justlevelingfork.network.packet.client.SyncAptitudeCapabilityCP;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AptitudeLevelUpSP {
    private final String aptitude;

    public AptitudeLevelUpSP(Aptitude aptitude) {
        this.aptitude = aptitude.getName();
    }

    public AptitudeLevelUpSP(FriendlyByteBuf buffer) {
        this.aptitude = buffer.readUtf();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.aptitude);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                AptitudeCapability capability = AptitudeCapability.get(player);
                if (capability == null) {
                    return;
                }
                Aptitude aptitudePlayer = RegistryAptitudes.getAptitude(this.aptitude);
                if (aptitudePlayer == null || !aptitudePlayer.isEnabled()) {
                    return;
                }

                int aptitudeLevel = capability.getAptitudeLevel(aptitudePlayer);
                int levelCap = aptitudePlayer.getLevelCap();
                if (aptitudeLevel >= levelCap) {
                    return;
                }

                int requiredLevels = requiredExperienceLevels(aptitudeLevel, aptitudePlayer);
                int requiredPoints = requiredPoints(aptitudeLevel, aptitudePlayer);

                boolean canLevelUpAptitude = (player.isCreative()
                        || requiredPoints <= player.totalExperience
                        || requiredLevels <= player.experienceLevel);

                if (!canLevelUpAptitude){
                    JustLevelingFork.getLOGGER().info("Received level up packet without the required EXP needed to level up, skipping packet...");
                    return;
                }

                int previousLevel = aptitudeLevel;
                int nextLevel = Math.min(previousLevel + 1, levelCap);
                if (!LevelLockAPI.canReachLevel(player, aptitudePlayer.getName(), nextLevel)) {
                    return;
                }
                if (KubeJSIntegration.isModLoaded()) {
                    boolean cancelled = new KubeJSIntegration().postLevelUpServerEvent(player, aptitudePlayer, previousLevel, nextLevel);
                    if (cancelled) {
                        return;
                    }
                }

                capability.addAptitudeLevel(aptitudePlayer, 1);
                int newLevel = capability.getAptitudeLevel(aptitudePlayer);
                SyncAptitudeCapabilityCP.send(player);
                if (!player.isCreative()) {
                    addPlayerXP(player, requiredPoints * -1);
                }
                SkillChangeAPI.handleLevelUp(player, aptitudePlayer, previousLevel, newLevel);
            }
        });
        context.setPacketHandled(true);
    }

    public static int getPlayerXP(Player player) {
        return (int)(getExperienceForLevel(player.experienceLevel) + (player.experienceProgress * player.getXpNeededForNextLevel()));
    }

    public static int xpBarCap(int level) {
        if (level >= 30)
            return 112 + (level - 30) * 9;

        if (level >= 15)
            return 37 + (level - 15) * 5;

        return 7 + level * 2;
    }

    public void addPlayerXP(Player player, int amount) {
        int experience = getPlayerXP(player) + amount;
        player.totalExperience = experience;
        player.experienceLevel = getLevelForExperience(experience);
        int expForLevel = getExperienceForLevel(player.experienceLevel);
        player.experienceProgress = (experience - expForLevel) / (float)player.getXpNeededForNextLevel();
    }

    public static int getLevelForExperience(int targetXp) {
        int level = 0;
        while (true) {
            final int xpToNextLevel = xpBarCap(level);
            if (targetXp < xpToNextLevel) return level;
            level++;
            targetXp -= xpToNextLevel;
        }
    }

    public static int requiredPoints(int aptitudeLevel, int baseLevelCost) {
        return getExperienceForLevel(aptitudeLevel + baseLevelCost - 1);
    }

    public static int requiredPoints(int aptitudeLevel) {
        return requiredPoints(aptitudeLevel, HandlerCommonConfig.HANDLER.instance().aptitudeFirstCostLevel);
    }

    public static int requiredPoints(int aptitudeLevel, Aptitude aptitude) {
        if (aptitude == null) {
            return requiredPoints(aptitudeLevel);
        }
        return aptitude.getLevelUpPointCost(aptitudeLevel);
    }

    public static int requiredExperienceLevels(int aptitudeLevel, int baseLevelCost) {
        return aptitudeLevel + baseLevelCost - 1;
    }

    public static int requiredExperienceLevels(int aptitudeLevel) {
        return requiredExperienceLevels(aptitudeLevel, HandlerCommonConfig.HANDLER.instance().aptitudeFirstCostLevel);
    }

    public static int requiredExperienceLevels(int aptitudeLevel, Aptitude aptitude) {
        if (aptitude == null) {
            return requiredExperienceLevels(aptitudeLevel);
        }
        return aptitude.getLevelUpExperienceLevels(aptitudeLevel);
    }

    public static int getExperienceForLevel(int level) {
        if (level == 0) return 0;
        if (level <= 15) return sum(level, 7, 2);
        if (level <= 30) return 315 + sum(level - 15, 37, 5);
        return 1395 + sum(level - 30, 112, 9);
    }

    private static int sum(int n, int a0, int d) {
        return n * (2 * a0 + (n - 1) * d) / 2;
    }

    public static void send(Aptitude aptitude) {
        ServerNetworking.sendToServer(new AptitudeLevelUpSP(aptitude));
    }
}


