package com.joseetoon.justlevellingaddonjs.network.packet.client;

import com.joseetoon.justlevellingaddonjs.compat.base121.BackportRegistryState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class TitleColorSyncCP {
    private final Map<String, Integer> colors;

    public TitleColorSyncCP(Map<String, Integer> colors) {
        this.colors = colors == null ? Map.of() : Map.copyOf(colors);
    }

    public TitleColorSyncCP(FriendlyByteBuf buffer) {
        int size = Math.max(0, buffer.readVarInt());
        Map<String, Integer> decoded = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            String titleName = buffer.readUtf(256);
            int rgb = buffer.readInt();
            decoded.put(titleName, rgb);
        }
        this.colors = decoded;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.colors.size());
        this.colors.forEach((titleName, rgb) -> {
            buffer.writeUtf(titleName);
            buffer.writeInt(rgb);
        });
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> BackportRegistryState.replaceTitleOverheadColorOverrides(this.colors));
        context.setPacketHandled(true);
    }
}

