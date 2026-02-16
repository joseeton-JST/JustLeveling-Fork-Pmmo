package com.seniors.justlevelingfork.network.packet.common;

import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import com.seniors.justlevelingfork.kubejs.SkillChangeAPI;
import com.seniors.justlevelingfork.network.ServerNetworking;
import com.seniors.justlevelingfork.network.packet.client.SyncAptitudeCapabilityCP;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import com.seniors.justlevelingfork.registry.skills.Skill;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class ToggleSkillSP {
    private final String skill;
    private final boolean toggle;

    public ToggleSkillSP(Skill skill, boolean toggle) {
        this.skill = skill.getName();
        this.toggle = toggle;
    }

    public ToggleSkillSP(FriendlyByteBuf buffer) {
        this.skill = buffer.readUtf();
        this.toggle = buffer.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.skill);
        buffer.writeBoolean(this.toggle);
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

                Skill skill = RegistrySkills.getSkill(this.skill);
                if (skill == null) {
                    return;
                }

                boolean beforeToggle = capability.getToggleSkill(skill);
                boolean beforeUnlocked = capability.isSkillUnlocked(skill);

                if (this.toggle) {
                    int aptitudeLevel = capability.getAptitudeLevel(skill.aptitude);
                    if (!skill.aptitude.isEnabled() || skill.getLvl() <= 0 || aptitudeLevel < skill.getLvl()) {
                        return;
                    }

                    if (!capability.isSkillUnlocked(skill) && !capability.tryUnlockSkill(skill)) {
                        return;
                    }

                    capability.setToggleSkill(skill, true);
                } else {
                    capability.setToggleSkill(skill, false);
                }

                boolean afterToggle = capability.getToggleSkill(skill);
                boolean afterUnlocked = capability.isSkillUnlocked(skill);

                SkillChangeAPI.handleSkillUnlockStateChange(player, skill, beforeUnlocked, afterUnlocked);

                if (beforeToggle != afterToggle || beforeUnlocked != afterUnlocked) {
                    SyncAptitudeCapabilityCP.send(player);
                }
            }
        });
        context.setPacketHandled(true);
    }

    public static void send(Skill skill, boolean toggle) {
        ServerNetworking.sendToServer(new ToggleSkillSP(skill, toggle));
    }
}


