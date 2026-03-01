package com.joseetoon.justlevellingaddonjs.mixin;

import com.joseetoon.justlevellingaddonjs.compat.base121.BackportRegistryState;
import com.joseetoon.justlevellingaddonjs.compat.base121.TitleBlockState;
import com.mojang.brigadier.context.CommandContext;
import com.seniors.justlevelingfork.common.command.TitleCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TitleCommand.class, remap = false)
public abstract class MixTitleCommandDeletedGuard {
    @Inject(method = "setTitle", at = @At("HEAD"), cancellable = true, require = 0)
    private static void jlforkaddon$rejectDeletedTitle(
            CommandContext<CommandSourceStack> source,
            ServerPlayer player,
            ResourceLocation titleKey,
            boolean set,
            CallbackInfoReturnable<Integer> cir
    ) {
        if (titleKey != null && BackportRegistryState.isTitleDeleted(titleKey)) {
            source.getSource().sendFailure(Component.literal("Title '" + titleKey + "' is deleted and can't be used."));
            cir.setReturnValue(0);
            return;
        }

        if (titleKey != null && BackportRegistryState.isTitleDisabledByAddonConfig(titleKey.getPath())) {
            source.getSource().sendFailure(Component.literal("Title '" + titleKey + "' is disabled by addon config."));
            cir.setReturnValue(0);
            return;
        }

        if (set && titleKey != null && BackportRegistryState.isTitleServerManaged(titleKey.getPath())) {
            source.getSource().sendFailure(Component.literal("Title '" + titleKey + "' is server-managed. Use server scripts/API to unlock it."));
            cir.setReturnValue(0);
            return;
        }

        if (set && titleKey != null && TitleBlockState.isBlocked(player, titleKey.getPath())) {
            source.getSource().sendFailure(Component.literal("Title '" + titleKey + "' is blocked for this player."));
            cir.setReturnValue(0);
        }
    }
}
