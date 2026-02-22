package com.joseetoon.justlevellingaddonjs;

import com.joseetoon.justlevellingaddonjs.config.AddonClientConfig;
import com.joseetoon.justlevellingaddonjs.config.AddonClientConfigReloader;
import com.joseetoon.justlevellingaddonjs.config.AddonClothConfigScreen;
import com.seniors.justlevelingfork.client.screen.JustLevelingScreen;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Map;

@Mod(JustLevellingAddonJS.MOD_ID)
public class JustLevellingAddonJS {
    public static final String MOD_ID = "justlevellingaddonjs";
    public static final String BASE_MOD_ID = "justlevelingfork";
    public static final String BASE_EXPECTED_VERSION = "1.2.1";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean legendaryBridgeLogged;
    private static boolean legendaryBridgeFailed;
    private static boolean legendaryTabRegistered;
    private static Field legendaryScreenInfoTabsField;

    public JustLevellingAddonJS() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, AddonClientConfig.SPEC, "justlevellingaddonjs-client.toml");

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ModLoadingContext.get().registerExtensionPoint(
                    ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> AddonClothConfigScreen.create(parent))
            );
            MinecraftForge.EVENT_BUS.addListener(this::jlforkaddon$onClientTick);
            MinecraftForge.EVENT_BUS.addListener(this::jlforkaddon$onConfigLoading);
            MinecraftForge.EVENT_BUS.addListener(this::jlforkaddon$onConfigReloading);
            MinecraftForge.EVENT_BUS.addListener(this::jlforkaddon$onScreenInitPost);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::jlforkaddon$onClientSetup);
        }

        validateBaseCompatibility();
        LOGGER.info("[JustLevellingAddonJS] Legendary-only integration active");
        LOGGER.info("[JustLevellingAddonJS] legendarytabs loaded={}", ModList.get().isLoaded("legendarytabs"));
        LOGGER.info("[JustLevellingAddonJS] Loaded against {} {}", BASE_MOD_ID, BASE_EXPECTED_VERSION);
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

    @SuppressWarnings("unchecked")
    private void jlforkaddon$onScreenInitPost(ScreenEvent.Init.Post event) {
        if (!ModList.get().isLoaded("legendarytabs")) {
            return;
        }

        try {
            Class<?> screenClass = event.getScreen().getClass();
            boolean aptitudeScreen = event.getScreen() instanceof JustLevelingScreen;

            Class<?> tabsMenuClass = Class.forName("sfiomn.legendarytabs.api.tabs_menu.TabsMenu");
            java.lang.reflect.Field tabsScreensField = tabsMenuClass.getDeclaredField("tabsScreens");
            tabsScreensField.setAccessible(true);
            Object value = tabsScreensField.get(null);
            if (!(value instanceof Map<?, ?> rawTabsMap)) {
                return;
            }

            Class<?> inventoryScreenClass = Class.forName("net.minecraft.client.gui.screens.inventory.InventoryScreen");
            Object inventoryScreenInfo = rawTabsMap.get(inventoryScreenClass);
            if (inventoryScreenInfo == null) {
                return;
            }

            Map<Class<?>, Object> tabsMap = (Map<Class<?>, Object>) rawTabsMap;
            Object currentScreenInfo = tabsMap.get(screenClass);
            if (aptitudeScreen && currentScreenInfo == null) {
                tabsMap.put(JustLevelingScreen.class, inventoryScreenInfo);
                currentScreenInfo = inventoryScreenInfo;
            }
            if (!aptitudeScreen && currentScreenInfo == null) {
                // Don't force tabs onto unrelated screens that LegendaryTabs doesn't own.
                return;
            }

            jlforkaddon$mergeLegendaryScreenInfo(currentScreenInfo, inventoryScreenInfo);
            if (currentScreenInfo != inventoryScreenInfo) {
                jlforkaddon$mergeLegendaryScreenInfo(inventoryScreenInfo, currentScreenInfo);
            }

            Object aptitudeScreenInfo = tabsMap.get(JustLevelingScreen.class);
            if (aptitudeScreenInfo != null) {
                jlforkaddon$mergeLegendaryScreenInfo(aptitudeScreenInfo, inventoryScreenInfo);
                jlforkaddon$mergeLegendaryScreenInfo(inventoryScreenInfo, aptitudeScreenInfo);
                if (currentScreenInfo != null && aptitudeScreenInfo != currentScreenInfo) {
                    jlforkaddon$mergeLegendaryScreenInfo(aptitudeScreenInfo, currentScreenInfo);
                    jlforkaddon$mergeLegendaryScreenInfo(currentScreenInfo, aptitudeScreenInfo);
                }
            }

            boolean hasLegendaryButtons = event.getScreen().children().stream().anyMatch(child -> {
                String className = child.getClass().getName();
                return "sfiomn.legendarytabs.client.screens.TabButton".equals(className)
                        || "sfiomn.legendarytabs.client.screens.NextTabsButton".equals(className);
            });

            if (!hasLegendaryButtons) {
                java.lang.reflect.Method initScreenButtonsMethod = tabsMenuClass.getMethod("initScreenButtons", ScreenEvent.Init.Post.class);
                initScreenButtonsMethod.invoke(null, event);
                hasLegendaryButtons = event.getScreen().children().stream().anyMatch(child -> {
                    String className = child.getClass().getName();
                    return "sfiomn.legendarytabs.client.screens.TabButton".equals(className)
                            || "sfiomn.legendarytabs.client.screens.NextTabsButton".equals(className);
                });
            }

            if (!legendaryBridgeLogged) {
                LOGGER.info("[JustLevellingAddonJS] LegendaryTabs runtime bridge active (screen={}, buttonsInjected={})", screenClass.getName(), hasLegendaryButtons);
                legendaryBridgeLogged = true;
            }
        } catch (Throwable throwable) {
            if (!legendaryBridgeFailed) {
                LOGGER.warn("[JustLevellingAddonJS] LegendaryTabs runtime bridge failed", throwable);
                legendaryBridgeFailed = true;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void jlforkaddon$mergeLegendaryScreenInfo(Object targetScreenInfo, Object sourceScreenInfo) {
        if (targetScreenInfo == null || sourceScreenInfo == null || targetScreenInfo == sourceScreenInfo) {
            return;
        }
        try {
            Field tabsField = jlforkaddon$getLegendaryTabsField(targetScreenInfo);
            if (tabsField == null) {
                return;
            }

            Object targetTabsObj = tabsField.get(targetScreenInfo);
            Object sourceTabsObj = tabsField.get(sourceScreenInfo);
            if (!(targetTabsObj instanceof Map<?, ?> targetRaw) || !(sourceTabsObj instanceof Map<?, ?> sourceRaw)) {
                return;
            }

            Map<Integer, List<Object>> targetTabs = (Map<Integer, List<Object>>) targetRaw;
            Map<Integer, List<Object>> sourceTabs = (Map<Integer, List<Object>>) sourceRaw;
            for (Map.Entry<Integer, List<Object>> sourceEntry : sourceTabs.entrySet()) {
                Integer priority = sourceEntry.getKey();
                List<Object> sourceList = sourceEntry.getValue();
                if (priority == null || sourceList == null || sourceList.isEmpty()) {
                    continue;
                }

                List<Object> targetList = targetTabs.computeIfAbsent(priority, ignored -> new ArrayList<>());
                for (Object sourceTab : sourceList) {
                    if (sourceTab == null) {
                        continue;
                    }
                    boolean exists = false;
                    for (Object targetTab : targetList) {
                        if (targetTab != null && targetTab.getClass() == sourceTab.getClass()) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        targetList.add(sourceTab);
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static Field jlforkaddon$getLegendaryTabsField(Object screenInfo) {
        if (legendaryScreenInfoTabsField != null) {
            return legendaryScreenInfoTabsField;
        }
        try {
            Field field = screenInfo.getClass().getDeclaredField("tabs");
            field.setAccessible(true);
            legendaryScreenInfoTabsField = field;
            return field;
        } catch (Throwable ignored) {
            return null;
        }
    }
}
