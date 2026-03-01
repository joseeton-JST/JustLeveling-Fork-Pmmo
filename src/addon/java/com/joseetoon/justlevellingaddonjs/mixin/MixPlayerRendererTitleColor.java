package com.joseetoon.justlevellingaddonjs.mixin;

import com.joseetoon.justlevellingaddonjs.compat.base121.BackportRegistryState;
import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerRenderer.class)
public abstract class MixPlayerRendererTitleColor {
    @Redirect(
            method = "renderNameTag(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/MutableComponent;withStyle(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;",
                    ordinal = 0
            ),
            require = 0
    )
    private MutableComponent jlforkaddon$applyTitleOverheadColor(
            MutableComponent component,
            ChatFormatting formatting,
            AbstractClientPlayer entity
    ) {
        if (component == null) {
            return null;
        }
        if (formatting != ChatFormatting.GOLD || entity == null) {
            return component.withStyle(formatting);
        }

        AptitudeCapability capability = AptitudeCapability.get(entity);
        String titleName = capability == null ? null : BackportRegistryState.normalizePath(capability.getPlayerTitle());
        if (titleName == null || "titleless".equals(titleName)) {
            return component.withStyle(formatting);
        }

        Integer rgb = BackportRegistryState.getTitleOverheadColorOverride(titleName);
        if (rgb == null) {
            return component.withStyle(formatting);
        }

        return component.withStyle(style -> style.withColor(TextColor.fromRgb(rgb & 0xFFFFFF)));
    }
}
