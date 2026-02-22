package com.joseetoon.justlevellingaddonjs.mixin;

import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import com.seniors.justlevelingfork.network.packet.client.SyncAptitudeCapabilityCP;
import com.seniors.justlevelingfork.network.packet.common.PassiveLevelUpSP;
import com.seniors.justlevelingfork.registry.RegistryPassives;
import com.seniors.justlevelingfork.registry.passive.Passive;
import com.joseetoon.justlevellingaddonjs.compat.CapabilityCompat;
import com.joseetoon.justlevellingaddonjs.kubejs.SkillChangeAPI;
import com.joseetoon.justlevellingaddonjs.kubejs.compat.PassiveCompat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(value = PassiveLevelUpSP.class, remap = false)
public abstract class MixPassiveLevelUpSP {
    @Shadow
    @Final
    private String passive;

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

            Passive passiveRef = RegistryPassives.getPassive(this.passive);
            if (passiveRef == null) {
                return;
            }

            int pointCost = PassiveCompat.getPointCost(passiveRef);
            if (!CapabilityCompat.trySpendAptitudePoints(capability, passiveRef.aptitude, pointCost)) {
                return;
            }

            int before = capability.getPassiveLevel(passiveRef);
            capability.addPassiveLevel(passiveRef, 1);
            int after = capability.getPassiveLevel(passiveRef);

            if (after != before) {
                SkillChangeAPI.handlePassiveLevelChanged(player, passiveRef, before, after);
                SyncAptitudeCapabilityCP.send(player);
                return;
            }

            CapabilityCompat.refundAptitudePoints(capability, passiveRef.aptitude, pointCost);
        });
        context.setPacketHandled(true);
        ci.cancel();
    }
}
