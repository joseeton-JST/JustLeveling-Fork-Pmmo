package com.joseetoon.justlevellingaddonjs.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class AddonCommonConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue disableBaseModTitles;
    public static final ForgeConfigSpec.BooleanValue kubejsTitlesServerManagedByDefault;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("common");

        disableBaseModTitles = builder
                .comment("Disable all titles coming from justlevelingfork titles config list (except titleless).")
                .define("disableBaseModTitles", false);

        kubejsTitlesServerManagedByDefault = builder
                .comment("When true, titles created via KubeJS Title.add(...) are server-managed by default.")
                .define("kubejsTitlesServerManagedByDefault", true);

        builder.pop();
        SPEC = builder.build();
    }

    private AddonCommonConfig() {
    }
}
