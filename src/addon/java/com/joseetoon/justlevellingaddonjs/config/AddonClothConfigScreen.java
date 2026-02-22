package com.joseetoon.justlevellingaddonjs.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class AddonClothConfigScreen {
    private AddonClothConfigScreen() {
    }

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("JustLevellingAddonJS - Client Config"));

        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
        ConfigEntryBuilder entry = builder.entryBuilder();

        general.addEntry(entry.startBooleanToggle(Component.literal("Show Title Button"), AddonClientConfig.showTitleButton.get())
                .setDefaultValue(true)
                .setSaveConsumer(AddonClientConfig.showTitleButton::set)
                .build());
        general.addEntry(entry.startBooleanToggle(Component.literal("Hide Locked Titles"), AddonClientConfig.hideLockedTitles.get())
                .setDefaultValue(true)
                .setSaveConsumer(AddonClientConfig.hideLockedTitles::set)
                .build());

        ConfigCategory ui = builder.getOrCreateCategory(Component.literal("UI"));
        ui.addEntry(entry.startBooleanToggle(Component.literal("Show Aptitude XP Level Button"), AddonClientConfig.showAptitudeXpLevelButton.get())
                .setDefaultValue(true)
                .setSaveConsumer(AddonClientConfig.showAptitudeXpLevelButton::set)
                .build());
        ui.addEntry(entry.startBooleanToggle(Component.literal("Show Skill Sort Controls"), AddonClientConfig.showSkillSortControls.get())
                .setDefaultValue(true)
                .setSaveConsumer(AddonClientConfig.showSkillSortControls::set)
                .build());
        ui.addEntry(entry.startBooleanToggle(Component.literal("Show Title Sort Controls"), AddonClientConfig.showTitleSortControls.get())
                .setDefaultValue(true)
                .setSaveConsumer(AddonClientConfig.showTitleSortControls::set)
                .build());

        builder.setSavingRunnable(AddonClientConfigReloader::saveFromSpecAndReload);
        return builder.build();
    }
}
