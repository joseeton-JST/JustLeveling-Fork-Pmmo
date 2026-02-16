package com.seniors.justlevelingfork.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.seniors.justlevelingfork.common.command.arguments.AptitudeArgument;
import com.seniors.justlevelingfork.kubejs.PlayerDataAPI;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class AptitudeLevelCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                (Commands.literal("aptitudes").requires(source -> source.hasPermission(2)))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(
                                Commands.argument("aptitude", AptitudeArgument.getArgument())
                                .then(Commands.literal("get")
                                        .executes(source -> getAptitude(source, EntityArgument.getPlayer(source, "player"), source.getArgument("aptitude", String.class)))
                                )
                                .then(Commands.literal("set")
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                            .executes(source -> setAptitude(source, EntityArgument.getPlayer(source, "player"), source.getArgument("aptitude", String.class), IntegerArgumentType.getInteger(source, "level")))
                                        )
                                )
                                .then(Commands.literal(("add"))
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                                .executes(source -> addAptitude(source, EntityArgument.getPlayer(source, "player"), source.getArgument("aptitude", String.class), IntegerArgumentType.getInteger(source, "level"))))
                                )
                                .then(Commands.literal(("subtract"))
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                                .executes(source -> subtractAptitude(source, EntityArgument.getPlayer(source, "player"), source.getArgument("aptitude", String.class), IntegerArgumentType.getInteger(source, "level")))
                                        )
                                )
                        )
                )
        );
    }


    public static int getAptitude(CommandContext<CommandSourceStack> source, ServerPlayer player, String aptitudeKey) {
        Aptitude aptitude = RegistryAptitudes.getAptitude(aptitudeKey);

        if (player != null && aptitude != null) {
            int currentLevel = PlayerDataAPI.getAptitudeLevel(player, aptitude.getName());
            source.getSource().sendSuccess(() -> Component.translatable("commands.message.aptitude.get", player.getName().copy().withStyle(ChatFormatting.BOLD), Component.literal(String.valueOf(currentLevel)).withStyle(ChatFormatting.BOLD), aptitudeNameComponent(aptitude)), false);

            return Command.SINGLE_SUCCESS;
        }


        return 0;
    }

    public static int setAptitude(CommandContext<CommandSourceStack> source, ServerPlayer player, String aptitudeKey, int setLevel) {
        Aptitude aptitude = RegistryAptitudes.getAptitude(aptitudeKey);

        if (player != null && aptitude != null) {
            PlayerDataAPI.setAptitudeLevel(player, aptitude.getName(), setLevel);
            int currentLevel = PlayerDataAPI.getAptitudeLevel(player, aptitude.getName());
            source.getSource().sendSuccess(() -> Component.translatable("commands.message.aptitude.set", player.getName().copy().withStyle(ChatFormatting.BOLD), Component.literal(String.valueOf(currentLevel)).withStyle(ChatFormatting.BOLD), aptitudeNameComponent(aptitude)), false);


            return Command.SINGLE_SUCCESS;
        }

        return 0;
    }

    public static int addAptitude(CommandContext<CommandSourceStack> source, ServerPlayer player, String aptitudeKey, int addLevel) {
        Aptitude aptitude = RegistryAptitudes.getAptitude(aptitudeKey);

        if (player != null && aptitude != null) {
            PlayerDataAPI.addAptitudeLevel(player, aptitude.getName(), addLevel);
            int currentLevel = PlayerDataAPI.getAptitudeLevel(player, aptitude.getName());
            source.getSource().sendSuccess(() -> Component.translatable("commands.message.aptitude.set", player.getName().copy().withStyle(ChatFormatting.BOLD), Component.literal(String.valueOf(currentLevel)).withStyle(ChatFormatting.BOLD), aptitudeNameComponent(aptitude)), false);


            return Command.SINGLE_SUCCESS;
        }

        return 0;
    }

    public static int subtractAptitude(CommandContext<CommandSourceStack> source, ServerPlayer player, String aptitudeKey, int subtractLevel) {
        Aptitude aptitude = RegistryAptitudes.getAptitude(aptitudeKey);

        if (player != null && aptitude != null) {
            PlayerDataAPI.addAptitudeLevel(player, aptitude.getName(), -subtractLevel);
            int currentLevel = PlayerDataAPI.getAptitudeLevel(player, aptitude.getName());
            source.getSource().sendSuccess(() -> Component.translatable("commands.message.aptitude.set", player.getName().copy().withStyle(ChatFormatting.BOLD), Component.literal(String.valueOf(currentLevel)).withStyle(ChatFormatting.BOLD), aptitudeNameComponent(aptitude)), false);

            return Command.SINGLE_SUCCESS;
        }

        return 0;
    }

    private static Component aptitudeNameComponent(Aptitude aptitude) {
        return Component.literal(aptitude.getDisplayNameOrFallback()).withStyle(ChatFormatting.BOLD);
    }
}


