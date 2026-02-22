package com.joseetoon.justlevellingaddonjs.mixin;

import com.seniors.justlevelingfork.JustLevelingFork;
import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import com.seniors.justlevelingfork.network.packet.client.SyncAptitudeCapabilityCP;
import com.seniors.justlevelingfork.network.packet.common.AptitudeLevelUpSP;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.joseetoon.justlevellingaddonjs.compat.AptitudeCompat;
import com.joseetoon.justlevellingaddonjs.compat.base121.Base121Bridge;
import com.joseetoon.justlevellingaddonjs.integration.KubeJSAddonIntegration;
import com.joseetoon.justlevellingaddonjs.kubejs.LevelLockAPI;
import com.joseetoon.justlevellingaddonjs.kubejs.SkillChangeAPI;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(value = AptitudeLevelUpSP.class, remap = false)
public abstract class MixAptitudeLevelUpSP {
    @Shadow
    @Final
    private String aptitude;

    @Inject(method = "handle", at = @At("HEAD"), cancellable = true, require = 0)
    private void jlforkaddon$handle(Supplier<NetworkEvent.Context> supplier, CallbackInfo ci) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            AptitudeCapability capability = AptitudeCapability.get(player);
            if (capability == null) {
                return;
            }

            Aptitude aptitudePlayer = RegistryAptitudes.getAptitude(this.aptitude);
            if (aptitudePlayer == null || !AptitudeCompat.isEnabled(aptitudePlayer)) {
                return;
            }

            int aptitudeLevel = capability.getAptitudeLevel(aptitudePlayer);
            int levelCap = AptitudeCompat.getLevelCap(aptitudePlayer);
            if (aptitudeLevel >= levelCap) {
                return;
            }

            int requiredLevels = Base121Bridge.requiredExperienceLevels(aptitudeLevel, aptitudePlayer);
            int requiredPoints = Base121Bridge.requiredPoints(aptitudeLevel, aptitudePlayer);
            boolean canLevelUpAptitude = player.isCreative()
                    || requiredPoints <= player.totalExperience
                    || requiredLevels <= player.experienceLevel;

            if (!canLevelUpAptitude) {
                JustLevelingFork.getLOGGER().info("Received level up packet without required EXP, skipping packet...");
                return;
            }

            int previousLevel = aptitudeLevel;
            int nextLevel = Math.min(previousLevel + 1, levelCap);
            if (!LevelLockAPI.canReachLevel(player, AptitudeCompat.getName(aptitudePlayer), nextLevel)) {
                return;
            }

            if (KubeJSAddonIntegration.postLevelUpServer(player, aptitudePlayer, previousLevel, nextLevel)) {
                return;
            }

            capability.addAptitudeLevel(aptitudePlayer, 1);
            int newLevel = capability.getAptitudeLevel(aptitudePlayer);
            SyncAptitudeCapabilityCP.send(player);
            if (!player.isCreative()) {
                ((AptitudeLevelUpSP) (Object) this).addPlayerXP(player, requiredPoints * -1);
            }
            SkillChangeAPI.handleLevelUp(player, aptitudePlayer, previousLevel, newLevel);
        });
        context.setPacketHandled(true);
        ci.cancel();
    }
}
