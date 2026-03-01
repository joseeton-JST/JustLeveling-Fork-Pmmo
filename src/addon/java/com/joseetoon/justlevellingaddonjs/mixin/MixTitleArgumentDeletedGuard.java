package com.joseetoon.justlevellingaddonjs.mixin;

import com.joseetoon.justlevellingaddonjs.compat.base121.BackportRegistryState;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.seniors.justlevelingfork.common.command.arguments.TitleArgument;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import com.seniors.justlevelingfork.registry.title.Title;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(value = TitleArgument.class, remap = false)
public abstract class MixTitleArgumentDeletedGuard {
    @Inject(method = "getResource", at = @At("HEAD"), cancellable = true, require = 0)
    private static void jlforkaddon$rejectDeletedTitle(ResourceLocation registryName, CallbackInfoReturnable<ResourceLocation> cir) throws CommandSyntaxException {
        if (registryName != null
                && (BackportRegistryState.isTitleDeleted(registryName)
                || BackportRegistryState.isTitleDisabledByAddonConfig(registryName.getPath()))) {
            throw TitleArgument.ERROR_UNKNOWN_TITLE.create(registryName);
        }
    }

    @Inject(method = "listSuggestions", at = @At("HEAD"), cancellable = true, require = 0)
    private <S> void jlforkaddon$hideDeletedSuggestions(CommandContext<S> context, SuggestionsBuilder builder, CallbackInfoReturnable<CompletableFuture<Suggestions>> cir) {
        if (RegistryTitles.TITLES_REGISTRY.get() == null) {
            return;
        }

        for (Title title : RegistryTitles.TITLES_REGISTRY.get().getValues()) {
            ResourceLocation key = RegistryTitles.TITLES_REGISTRY.get().getKey(title);
            if (key == null
                    || BackportRegistryState.isTitleDeleted(key)
                    || BackportRegistryState.isTitleDisabledByAddonConfig(title.getName())) {
                continue;
            }
            builder.suggest(key.toString());
        }
        cir.setReturnValue(builder.buildFuture());
    }
}
