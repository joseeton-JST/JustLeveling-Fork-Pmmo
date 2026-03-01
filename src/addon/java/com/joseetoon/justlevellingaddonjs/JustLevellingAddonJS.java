package com.joseetoon.justlevellingaddonjs;

import com.joseetoon.justlevellingaddonjs.config.AddonClientConfig;
import com.joseetoon.justlevellingaddonjs.config.AddonClientConfigReloader;
import com.joseetoon.justlevellingaddonjs.config.AddonClothConfigScreen;
import com.joseetoon.justlevellingaddonjs.config.AddonCommonConfig;
import com.joseetoon.justlevellingaddonjs.network.AddonNetworking;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.forgespi.language.IModInfo;
import org.slf4j.Logger;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Objects;
import java.util.Optional;

@Mod(JustLevellingAddonJS.MOD_ID)
public class JustLevellingAddonJS {
    public static final String MOD_ID = "justlevellingaddonjs";
    public static final String BASE_MOD_ID = "justlevelingfork";
    public static final String BASE_EXPECTED_VERSION = "1.2.1";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean legendaryTabRegistered;

    public JustLevellingAddonJS() {
        AddonNetworking.init();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AddonCommonConfig.SPEC, "justlevellingaddonjs-common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, AddonClientConfig.SPEC, "justlevellingaddonjs-client.toml");
        MinecraftForge.EVENT_BUS.addListener(this::jlforkaddon$onPlayerLoggedIn);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ModLoadingContext.get().registerExtensionPoint(
                    ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> AddonClothConfigScreen.create(parent))
            );
            MinecraftForge.EVENT_BUS.addListener(this::jlforkaddon$onClientTick);
            MinecraftForge.EVENT_BUS.addListener(this::jlforkaddon$onConfigLoading);
            MinecraftForge.EVENT_BUS.addListener(this::jlforkaddon$onConfigReloading);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::jlforkaddon$onClientSetup);
        }

        validateBaseCompatibility();
        LOGGER.info("[JustLevellingAddonJS] Legendary-only integration active");
        LOGGER.info("[JustLevellingAddonJS] legendarytabs loaded={}", ModList.get().isLoaded("legendarytabs"));
        LOGGER.info("[JustLevellingAddonJS] Loaded against {} {}", BASE_MOD_ID, BASE_EXPECTED_VERSION);
    }

    private void jlforkaddon$onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AddonNetworking.syncTitleColorsToPlayer(player);
        }
    }

    private static void validateBaseCompatibility() {
        Optional<IModInfo> maybeBase = ModList.get().getMods().stream()
                .filter(modInfo -> Objects.equals(modInfo.getModId(), BASE_MOD_ID))
                .findFirst();

        if (maybeBase.isEmpty()) {
            throw new IllegalStateException("[JustLevellingAddonJS] Missing required base mod: " + BASE_MOD_ID);
        }

        String loadedVersion = maybeBase.get().getVersion().toString();
        if (!Objects.equals(loadedVersion, BASE_EXPECTED_VERSION)) {
            throw new IllegalStateException("[JustLevellingAddonJS] Incompatible base mod version for "
                    + BASE_MOD_ID + ": expected " + BASE_EXPECTED_VERSION + ", found " + loadedVersion
                    + ". This addon version only supports exact base version " + BASE_EXPECTED_VERSION + ".");
        }
    }

    private void jlforkaddon$onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            AddonClientConfigReloader.tick();
        }
    }

    private void jlforkaddon$onConfigLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getType() == ModConfig.Type.CLIENT && MOD_ID.equals(event.getConfig().getModId())) {
            AddonClientConfigReloader.bindConfig(event.getConfig());
            AddonClientConfigReloader.forceReload();
        }
    }

    private void jlforkaddon$onConfigReloading(ModConfigEvent.Reloading event) {
        if (event.getConfig().getType() == ModConfig.Type.CLIENT && MOD_ID.equals(event.getConfig().getModId())) {
            AddonClientConfigReloader.bindConfig(event.getConfig());
            AddonClientConfigReloader.forceReload();
        }
    }

    private void jlforkaddon$onClientSetup(FMLClientSetupEvent event) {
        if (!ModList.get().isLoaded("legendarytabs")) {
            LOGGER.warn("[JustLevellingAddonJS] Required dependency legendarytabs is not loaded.");
            return;
        }

        event.enqueueWork(() -> {
            if (legendaryTabRegistered) {
                return;
            }
            try {
                Class<?> tabClass = Class.forName("com.joseetoon.justlevellingaddonjs.compat.legendary.LegendaryAptitudeTab");
                Object tab = tabClass.getConstructor().newInstance();
                Class<?> tabBaseClass = Class.forName("sfiomn.legendarytabs.api.tabs_menu.TabBase");
                Class<?> tabsMenuClass = Class.forName("sfiomn.legendarytabs.api.tabs_menu.TabsMenu");
                tabsMenuClass.getMethod("register", tabBaseClass).invoke(null, tab);
                legendaryTabRegistered = true;
                LOGGER.info("[JustLevellingAddonJS] Aptitude tab registered in LegendaryTabs");
            } catch (Throwable throwable) {
                LOGGER.error("[JustLevellingAddonJS] Failed to register Aptitude tab in LegendaryTabs", throwable);
            }
        });
    }
}
