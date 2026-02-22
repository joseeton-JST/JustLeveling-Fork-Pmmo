package com.joseetoon.justlevellingaddonjs.mixin;

import com.seniors.justlevelingfork.handler.HandlerAptitude;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = HandlerAptitude.class, remap = false)
public abstract class MixHandlerAptitudeBackportApi {
    public static void invalidateCache() {
        HandlerAptitude.ForceRefresh();
    }
}
