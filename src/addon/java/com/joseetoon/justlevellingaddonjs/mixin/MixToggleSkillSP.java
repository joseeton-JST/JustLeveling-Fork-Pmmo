package com.joseetoon.justlevellingaddonjs.mixin;

import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import com.seniors.justlevelingfork.network.packet.client.SyncAptitudeCapabilityCP;
import com.seniors.justlevelingfork.network.packet.common.ToggleSkillSP;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import com.seniors.justlevelingfork.registry.skills.Skill;
import com.joseetoon.justlevellingaddonjs.compat.AptitudeCompat;
import com.joseetoon.justlevellingaddonjs.compat.CapabilityCompat;
import com.joseetoon.justlevellingaddonjs.kubejs.SkillChangeAPI;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(value = ToggleSkillSP.class, remap = false)
public abstract class MixToggleSkillSP {
    @Shadow
    @Final
    private String skill;

    @Shadow
    @Final
    private boolean toggle;

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

            Skill skillRef = RegistrySkills.getSkill(this.skill);
            if (skillRef == null) {
                return;
            }

            boolean beforeToggle = capability.getToggleSkill(skillRef);
            boolean beforeUnlocked = CapabilityCompat.isSkillUnlocked(capability, skillRef);

            if (this.toggle) {
                int aptitudeLevel = capability.getAptitudeLevel(skillRef.aptitude);
                if (!AptitudeCompat.isEnabled(skillRef.aptitude) || skillRef.getLvl() <= 0 || aptitudeLevel < skillRef.getLvl()) {
                    return;
                }

                if (!CapabilityCompat.isSkillUnlocked(capability, skillRef) && !CapabilityCompat.tryUnlockSkill(capability, skillRef)) {
                    return;
                }
                capability.setToggleSkill(skillRef, true);
            } else {
                capability.setToggleSkill(skillRef, false);
            }

            boolean afterToggle = capability.getToggleSkill(skillRef);
            boolean afterUnlocked = CapabilityCompat.isSkillUnlocked(capability, skillRef);
            SkillChangeAPI.handleSkillUnlockStateChange(player, skillRef, beforeUnlocked, afterUnlocked);

            if (beforeToggle != afterToggle || beforeUnlocked != afterUnlocked) {
                SyncAptitudeCapabilityCP.send(player);
            }
        });
        context.setPacketHandled(true);
        ci.cancel();
    }
}
