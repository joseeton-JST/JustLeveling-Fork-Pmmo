package com.joseetoon.justlevellingaddonjs.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class AddonClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue showTitleButton;
    public static final ForgeConfigSpec.BooleanValue hideLockedTitles;
    public static final ForgeConfigSpec.BooleanValue showAptitudeXpLevelButton;
    public static final ForgeConfigSpec.BooleanValue showSkillSortControls;
    public static final ForgeConfigSpec.BooleanValue showTitleSortControls;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("client");

        showTitleButton = builder
                .comment("Show title button on aptitude page (set false to hide title entry from this screen).")
                .define("showTitleButton", true);

        hideLockedTitles = builder
                .comment("Hide locked titles from the title list UI.")
                .define("hideLockedTitles", true);

        showAptitudeXpLevelButton = builder
                .comment("Show aptitude XP level-up button in skills screen.")
                .define("showAptitudeXpLevelButton", true);

        showSkillSortControls = builder
                .comment("Show sort/mod controls in skills/passives screen.")
                .define("showSkillSortControls", true);

        showTitleSortControls = builder
                .comment("Show sort/mod controls in titles screen.")
                .define("showTitleSortControls", true);

        builder.pop();
        SPEC = builder.build();
    }

    private AddonClientConfig() {
    }
}
