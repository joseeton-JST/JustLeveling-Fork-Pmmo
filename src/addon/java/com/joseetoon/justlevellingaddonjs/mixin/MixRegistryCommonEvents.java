package com.joseetoon.justlevellingaddonjs.mixin;

import com.seniors.justlevelingfork.registry.RegistryCommonEvents;
import com.joseetoon.justlevellingaddonjs.kubejs.TransmutationAPI;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RegistryCommonEvents.class, remap = false)
public abstract class MixRegistryCommonEvents {
    @Inject(method = "onLeftClickBlock", at = @At("TAIL"), require = 0)
    private void jlforkaddon$transmutationOnLeftClick(PlayerInteractEvent.LeftClickBlock event, CallbackInfo ci) {
        if (event.isCanceled() || event.getLevel().isClientSide()) {
            return;
        }

        Player player = event.getEntity();
        ItemStack handStack = player.getItemInHand(event.getHand());
        if (TransmutationAPI.tryTransmute(player, event.getLevel(), event.getPos(), handStack)) {
            event.setCanceled(true);
        }
    }
}
