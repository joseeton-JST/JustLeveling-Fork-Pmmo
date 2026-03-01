package com.joseetoon.justlevellingaddonjs.network;

import com.joseetoon.justlevellingaddonjs.JustLevellingAddonJS;
import com.joseetoon.justlevellingaddonjs.compat.base121.BackportRegistryState;
import com.joseetoon.justlevellingaddonjs.network.packet.client.TitleColorSyncCP;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class AddonNetworking {
    private static final String PROTOCOL_VERSION = "1";
    private static int packetId;
    private static boolean initialized;
    private static SimpleChannel channel;

    private AddonNetworking() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }

        channel = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(JustLevellingAddonJS.MOD_ID, "network"))
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .simpleChannel();

        channel.registerMessage(
                packetId++,
                TitleColorSyncCP.class,
                TitleColorSyncCP::toBytes,
                TitleColorSyncCP::new,
                TitleColorSyncCP::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        initialized = true;
    }

    public static void syncTitleColorsToPlayer(ServerPlayer player) {
        if (!initialized || player == null) {
            return;
        }
        channel.send(
                PacketDistributor.PLAYER.with(() -> player),
                new TitleColorSyncCP(BackportRegistryState.titleOverheadColorOverridesSnapshot())
        );
    }

    public static void syncTitleColorsToAllPlayers() {
        if (!initialized || ServerLifecycleHooks.getCurrentServer() == null) {
            return;
        }
        channel.send(
                PacketDistributor.ALL.noArg(),
                new TitleColorSyncCP(BackportRegistryState.titleOverheadColorOverridesSnapshot())
        );
    }
}

