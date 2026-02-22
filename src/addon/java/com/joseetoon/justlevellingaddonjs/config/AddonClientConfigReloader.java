package com.joseetoon.justlevellingaddonjs.config;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

public final class AddonClientConfigReloader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int CHECK_EVERY_TICKS = 20;

    private static int tickCounter = 0;
    private static boolean bootstrapped = false;
    private static ModConfig clientConfig;

    private AddonClientConfigReloader() {
    }

    public static synchronized void bindConfig(ModConfig config) {
        if (config == null) {
            return;
        }
        if (config.getType() != ModConfig.Type.CLIENT) {
            return;
        }
        if (!"justlevellingaddonjs".equals(config.getModId())) {
            return;
        }
        clientConfig = config;
    }

    public static synchronized void bootstrap() {
        if (bootstrapped) {
            return;
        }
        AddonClientRuntimeSettings.apply(AddonClientRuntimeSettings.fromSpec());
        bootstrapped = true;
    }

    public static synchronized void tick() {
        if (!bootstrapped) {
            bootstrap();
        }

        tickCounter++;
        if (tickCounter < CHECK_EVERY_TICKS) {
            return;
        }
        tickCounter = 0;
        refreshFromSpec(false);
    }

    public static synchronized void forceReload() {
        if (!bootstrapped) {
            bootstrap();
        }
        refreshFromSpec(true);
    }

    public static synchronized void saveFromSpecAndReload() {
        if (!bootstrapped) {
            bootstrap();
        }

        refreshFromSpec(true);

        if (clientConfig != null) {
            try {
                clientConfig.save();
                LOGGER.info("[JustLevellingAddonJS] Saved client config {}", clientConfig.getFileName());
            } catch (Throwable t) {
                LOGGER.warn("[JustLevellingAddonJS] Failed to save client config.", t);
            }
        }
    }

    private static void refreshFromSpec(boolean logWhenChanged) {
        AddonClientRuntimeSettings.Snapshot previous = AddonClientRuntimeSettings.current();
        AddonClientRuntimeSettings.Snapshot next = AddonClientRuntimeSettings.fromSpec();
        AddonClientRuntimeSettings.apply(next);
        if (logWhenChanged && !next.equals(previous)) {
            LOGGER.info("[JustLevellingAddonJS] Reloaded client config from Forge runtime state.");
        }
    }
}
