package com.seniors.justlevelingfork.network.packet.common;

import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import com.seniors.justlevelingfork.kubejs.SkillChangeAPI;
import com.seniors.justlevelingfork.network.ServerNetworking;
import com.seniors.justlevelingfork.network.packet.client.SyncAptitudeCapabilityCP;
import com.seniors.justlevelingfork.registry.RegistryPassives;
import com.seniors.justlevelingfork.registry.passive.Passive;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class PassiveLevelUpSP {
    private final String passive;

    public PassiveLevelUpSP(Passive passive) {
        this.passive = passive.getName();
    }

    public PassiveLevelUpSP(FriendlyByteBuf buffer) {
        this.passive = buffer.readUtf();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.passive);
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

                Passive passive = RegistryPassives.getPassive(this.passive);
                if (passive == null) {
                    return;
                }

                int pointCost = passive.getPointCost();
                if (!capability.trySpendAptitudePoints(passive.aptitude, pointCost)) {
                    return;
                }

                int before = capability.getPassiveLevel(passive);
                capability.addPassiveLevel(passive, 1);
                int after = capability.getPassiveLevel(passive);

                if (after != before) {
                    SkillChangeAPI.handlePassiveLevelChanged(player, passive, before, after);
                    SyncAptitudeCapabilityCP.send(player);
                    return;
                }

                // No passive level change happened (requirements not met), refund spent point.
                capability.refundAptitudePoints(passive.aptitude, pointCost);
            }
        });
        context.setPacketHandled(true);
    }

    public static void send(Passive passive) {
        ServerNetworking.sendToServer(new PassiveLevelUpSP(passive));
    }
}


