package com.joseetoon.justlevellingaddonjs.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.seniors.justlevelingfork.common.command.AptitudeLevelCommand;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.joseetoon.justlevellingaddonjs.compat.AptitudeCompat;
import com.joseetoon.justlevellingaddonjs.kubejs.PlayerDataAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AptitudeLevelCommand.class, remap = false)
public abstract class MixAptitudeLevelCommand {
    @Inject(method = "setAptitude", at = @At("HEAD"), cancellable = true, require = 0)
    private static void jlforkaddon$set(CommandContext<CommandSourceStack> source, ServerPlayer player, String aptitudeKey, int setLevel, CallbackInfoReturnable<Integer> cir) {
        Aptitude aptitude = RegistryAptitudes.getAptitude(aptitudeKey);
        if (player == null || aptitude == null) {
            cir.setReturnValue(0);
            return;
        }

        String aptitudeName = AptitudeCompat.getName(aptitude);
        PlayerDataAPI.setAptitudeLevel(player, aptitudeName, setLevel);
        int currentLevel = PlayerDataAPI.getAptitudeLevel(player, aptitudeName);
        source.getSource().sendSuccess(() -> Component.translatable(
                "commands.message.aptitude.set",
                player.getName().copy().withStyle(ChatFormatting.BOLD),
                Component.literal(String.valueOf(currentLevel)).withStyle(ChatFormatting.BOLD),
                Component.literal(AptitudeCompat.getDisplayNameOrFallback(aptitude)).withStyle(ChatFormatting.BOLD)
        ), false);

        cir.setReturnValue(1);
    }

    @Inject(method = "addAptitude", at = @At("HEAD"), cancellable = true, require = 0)
    private static void jlforkaddon$add(CommandContext<CommandSourceStack> source, ServerPlayer player, String aptitudeKey, int addLevel, CallbackInfoReturnable<Integer> cir) {
        Aptitude aptitude = RegistryAptitudes.getAptitude(aptitudeKey);
        if (player == null || aptitude == null) {
            cir.setReturnValue(0);
            return;
        }

        String aptitudeName = AptitudeCompat.getName(aptitude);
        PlayerDataAPI.addAptitudeLevel(player, aptitudeName, addLevel);
        int currentLevel = PlayerDataAPI.getAptitudeLevel(player, aptitudeName);
        source.getSource().sendSuccess(() -> Component.translatable(
                "commands.message.aptitude.set",
                player.getName().copy().withStyle(ChatFormatting.BOLD),
                Component.literal(String.valueOf(currentLevel)).withStyle(ChatFormatting.BOLD),
                Component.literal(AptitudeCompat.getDisplayNameOrFallback(aptitude)).withStyle(ChatFormatting.BOLD)
        ), false);

        cir.setReturnValue(1);
    }
}
