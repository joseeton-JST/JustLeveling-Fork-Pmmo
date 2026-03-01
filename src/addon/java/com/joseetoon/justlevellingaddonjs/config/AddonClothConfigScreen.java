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

        ConfigEntryBuilder entry = builder.entryBuilder();

        // ── General ──────────────────────────────────────────────────────────────
        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));

        general.addEntry(entry.startBooleanToggle(Component.literal("Show Title Button"), AddonClientConfig.showTitleButton.get())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Show the title shortcut button on the aptitude page."))
                .setSaveConsumer(AddonClientConfig.showTitleButton::set)
                .build());
        general.addEntry(entry.startBooleanToggle(Component.literal("Hide Locked Titles"), AddonClientConfig.hideLockedTitles.get())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Hide titles that are not yet unlocked from the title list."))
                .setSaveConsumer(AddonClientConfig.hideLockedTitles::set)
                .build());

        // ── UI ───────────────────────────────────────────────────────────────────
        ConfigCategory ui = builder.getOrCreateCategory(Component.literal("UI"));

        ui.addEntry(entry.startBooleanToggle(Component.literal("Show Aptitude XP Level Button"), AddonClientConfig.showAptitudeXpLevelButton.get())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Show the button that lets you spend vanilla XP levels to level up aptitudes."))
                .setSaveConsumer(AddonClientConfig.showAptitudeXpLevelButton::set)
                .build());
        ui.addEntry(entry.startBooleanToggle(Component.literal("Show Skill Sort Controls"), AddonClientConfig.showSkillSortControls.get())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Show sort and filter controls in the skills/passives screen."))
                .setSaveConsumer(AddonClientConfig.showSkillSortControls::set)
                .build());
        ui.addEntry(entry.startBooleanToggle(Component.literal("Show Title Sort Controls"), AddonClientConfig.showTitleSortControls.get())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Show sort and filter controls in the titles screen."))
                .setSaveConsumer(AddonClientConfig.showTitleSortControls::set)
                .build());
        builder.setSavingRunnable(AddonClientConfigReloader::saveFromSpecAndReload);
        return builder.build();
    }
}
