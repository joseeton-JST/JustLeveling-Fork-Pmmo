package com.joseetoon.justlevellingaddonjs.mixin;

import com.joseetoon.justlevellingaddonjs.compat.base121.BackportRegistryState;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.seniors.justlevelingfork.common.command.arguments.AptitudeArgument;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@Mixin(value = AptitudeArgument.class, remap = false)
public abstract class MixAptitudeArgumentDeletedGuard {
    @Inject(method = "parse", at = @At("RETURN"), cancellable = true, require = 0)
    private void jlforkaddon$rejectDeletedAptitude(
            StringReader reader,
            CallbackInfoReturnable<String> cir
    ) throws CommandSyntaxException {
        String parsed = cir.getReturnValue();
        if (BackportRegistryState.isAptitudeDeleted(parsed)) {
            throw AptitudeArgument.ERROR_UNKNOWN_TITLE.create(parsed);
        }
    }

    @Inject(method = "listSuggestions", at = @At("HEAD"), cancellable = true, require = 0)
    private <S> void jlforkaddon$hideDeletedAptitudes(
            CommandContext<S> context,
            SuggestionsBuilder builder,
            CallbackInfoReturnable<CompletableFuture<Suggestions>> cir
    ) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        if (RegistryAptitudes.APTITUDES_REGISTRY.get() == null) {
            return;
        }

        for (Aptitude aptitude : RegistryAptitudes.APTITUDES_REGISTRY.get().getValues()) {
            if (BackportRegistryState.isAptitudeDeleted(aptitude.getName())) {
                continue;
            }

            String aptitudeName = aptitude.getName();
            if (remaining.isEmpty() || aptitudeName.toLowerCase(Locale.ROOT).contains(remaining)) {
                builder.suggest(aptitudeName);
            }
        }
        cir.setReturnValue(builder.buildFuture());
    }
}
