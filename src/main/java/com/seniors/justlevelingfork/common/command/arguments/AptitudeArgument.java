package com.seniors.justlevelingfork.common.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class AptitudeArgument implements ArgumentType<String> {

    private static final List<String> FALLBACK_EXAMPLES = List.of("strength", "dexterity", "intelligence");
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_TITLE;

    static {
        ERROR_UNKNOWN_TITLE = new DynamicCommandExceptionType(object -> Component.translatable("commands.argument.aptitude.not_found", object));
    }

    public static AptitudeArgument getArgument() {
        return new AptitudeArgument();
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String aptitudeInput = reader.readString();
        Aptitude aptitude = RegistryAptitudes.getAptitude(aptitudeInput);
        if (aptitude == null) {
            throw ERROR_UNKNOWN_TITLE.create(aptitudeInput);
        }
        return aptitude.getName();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (String aptitudeName : getDynamicExamples()) {
            if (remaining.isEmpty() || aptitudeName.toLowerCase(Locale.ROOT).contains(remaining)) {
                builder.suggest(aptitudeName);
            }
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return getDynamicExamples();
    }

    private static List<String> getDynamicExamples() {
        try {
            List<String> examples = new ArrayList<>();
            for (Aptitude aptitude : RegistryAptitudes.APTITUDES_REGISTRY.get().getValues()) {
                examples.add(aptitude.getName());
            }
            if (!examples.isEmpty()) {
                return examples;
            }
        } catch (Exception ignored) {
        }
        return FALLBACK_EXAMPLES;
    }
}
