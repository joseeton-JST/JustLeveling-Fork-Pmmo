package com.joseetoon.justlevellingaddonjs.mixin;

import com.joseetoon.justlevellingaddonjs.compat.base121.BackportRegistryState;
import com.seniors.justlevelingfork.client.core.TitleQueue;
import com.seniors.justlevelingfork.client.gui.OverlayTitleGui;
import com.seniors.justlevelingfork.network.packet.client.TitleOverlayCP;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import com.seniors.justlevelingfork.registry.title.Title;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Mixin(value = TitleOverlayCP.class, remap = false)
public abstract class MixTitleOverlayCPDedup {
    @org.spongepowered.asm.mixin.Unique
    private static final long JLFORKADDON_DEDUP_WINDOW_MS = 2500L;
    @org.spongepowered.asm.mixin.Unique
    private static final Map<String, Long> JLFORKADDON_LAST_ENQUEUED = new ConcurrentHashMap<>();

    @Shadow
    @Final
    private String title;

    @Inject(method = "handle", at = @At("HEAD"), cancellable = true, require = 0)
    private void jlforkaddon$handleWithoutDuplicateSpam(Supplier<NetworkEvent.Context> supplier, CallbackInfo ci) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Title resolved = RegistryTitles.getTitle(this.title);
            if (resolved == null
                    || BackportRegistryState.isTitleDeleted(resolved.getName())
                    || BackportRegistryState.isTitleDisabledByAddonConfig(resolved.getName())) {
                return;
            }

            String normalizedTitle = resolved.getName().toLowerCase(Locale.ROOT);
            long now = System.currentTimeMillis();
            Long lastEnqueued = JLFORKADDON_LAST_ENQUEUED.get(normalizedTitle);
            if (lastEnqueued != null && (now - lastEnqueued) < JLFORKADDON_DEDUP_WINDOW_MS) {
                return;
            }

            TitleQueue queue = OverlayTitleGui.list;
            if (queue == null) {
                return;
            }

            if (queue.count() > 0) {
                Title current = queue.peek();
                if (current != null && current.getName().equalsIgnoreCase(resolved.getName())) {
                    return;
                }
            }

            queue.enqueue(resolved);
            JLFORKADDON_LAST_ENQUEUED.put(normalizedTitle, now);
            OverlayTitleGui.showWarning();
        });
        context.setPacketHandled(true);
        ci.cancel();
    }
}
