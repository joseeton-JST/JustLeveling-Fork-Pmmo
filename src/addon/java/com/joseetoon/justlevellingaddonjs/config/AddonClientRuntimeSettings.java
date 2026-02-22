package com.joseetoon.justlevellingaddonjs.config;

import java.util.concurrent.atomic.AtomicReference;

public final class AddonClientRuntimeSettings {
    private static final AtomicReference<Snapshot> CURRENT = new AtomicReference<>(Snapshot.defaults());

    private AddonClientRuntimeSettings() {
    }

    public static Snapshot current() {
        return CURRENT.get();
    }

    public static void apply(Snapshot snapshot) {
        if (snapshot != null) {
            CURRENT.set(snapshot);
        }
    }

    public static Snapshot fromSpec() {
        try {
            return new Snapshot(
                    true,
                    AddonClientConfig.showTitleButton.get(),
                    AddonClientConfig.hideLockedTitles.get(),
                    false,
                    false,
                    false,
                    AddonClientConfig.showAptitudeXpLevelButton.get(),
                    AddonClientConfig.showSkillSortControls.get(),
                    AddonClientConfig.showTitleSortControls.get(),
                    false
            );
        } catch (Throwable ignored) {
            return current();
        }
    }

    public record Snapshot(
            boolean enableMouseWheelScroll,
            boolean showTitleButton,
            boolean hideLockedTitles,
            boolean showPassiveNextCostLine,
            boolean showSkillCostLine,
            boolean showEmptyAptitudeOverlay,
            boolean showAptitudeXpLevelButton,
            boolean showSkillSortControls,
            boolean showTitleSortControls,
            boolean showModNameInTooltips
    ) {
        public static Snapshot defaults() {
            return new Snapshot(
                    true,
                    true,
                    true,
                    false,
                    false,
                    false,
                    true,
                    true,
                    true,
                    false
            );
        }
    }
}
