package com.joseetoon.justlevellingaddonjs.mixin;

import com.seniors.justlevelingfork.client.screen.JustLevelingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.client.event.ScreenEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Pseudo
@Mixin(targets = "sfiomn.legendarytabs.api.tabs_menu.TabsMenu", remap = false)
public abstract class MixLegendaryTabsAddTabMirror {
    @Unique
    private static final Logger JLFORKADDON$LOGGER = LogManager.getLogger("JustLevellingAddonJS-LegendaryTabs");
    @Unique
    private static boolean jlforkaddon$mirroring;
    @Unique
    private static boolean jlforkaddon$loggedMirror;
    @Unique
    private static boolean jlforkaddon$mergeFailed;
    @Unique
    private static Field jlforkaddon$screenInfoTabsField;

    @Shadow
    @Final
    private static Map<Class<? extends Screen>, Object> tabsScreens;

    @Inject(method = "addTabToScreen", at = @At("TAIL"), require = 0)
    private static void jlforkaddon$mirrorAfterTabRegistration(CallbackInfo ci) {
        jlforkaddon$mirrorInventoryTabsToAptitudes();
    }

    @Inject(method = "initScreenButtons", at = @At("HEAD"), require = 0)
    private static void jlforkaddon$mirrorBeforeButtonInit(ScreenEvent.Init.Post event, CallbackInfo ci) {
        Screen screen = event.getScreen();
        jlforkaddon$mirrorInventoryTabsToAptitudes(screen.getClass());
    }

    @Unique
    private static void jlforkaddon$mirrorInventoryTabsToAptitudes() {
        jlforkaddon$mirrorInventoryTabsToAptitudes(null);
    }

    @Unique
    @SuppressWarnings("unchecked")
    private static void jlforkaddon$mirrorInventoryTabsToAptitudes(Class<? extends Screen> currentScreenClass) {
        if (jlforkaddon$mirroring) {
            return;
        }

        Object inventoryTabs = tabsScreens.get(InventoryScreen.class);
        if (inventoryTabs == null) {
            return;
        }

        jlforkaddon$mirroring = true;
        try {
            Object aptitudeTabs = tabsScreens.get(JustLevelingScreen.class);
            if (aptitudeTabs == null) {
                tabsScreens.put(JustLevelingScreen.class, inventoryTabs);
                aptitudeTabs = inventoryTabs;
            }

            // Keep both maps in sync, preserving extra third-party tabs that may only exist
            // on one side depending on registration order.
            jlforkaddon$mergeScreenInfoTabs(aptitudeTabs, inventoryTabs);
            if (aptitudeTabs != inventoryTabs) {
                jlforkaddon$mergeScreenInfoTabs(inventoryTabs, aptitudeTabs);
            }

            // Also sync the currently opened screen with aptitude/inventory tabs so that
            // moving to another mod tab doesn't drop entries after screen switches.
            if (currentScreenClass != null) {
                Object currentTabs = tabsScreens.get(currentScreenClass);
                if (currentTabs != null) {
                    jlforkaddon$mergeScreenInfoTabs(currentTabs, inventoryTabs);
                    jlforkaddon$mergeScreenInfoTabs(currentTabs, aptitudeTabs);
                    if (currentTabs != aptitudeTabs) {
                        jlforkaddon$mergeScreenInfoTabs(aptitudeTabs, currentTabs);
                    }
                }
            }

            if (!jlforkaddon$loggedMirror) {
                JLFORKADDON$LOGGER.info("[compat] Mirroring Inventory tabs to JustLevelingScreen active");
                jlforkaddon$loggedMirror = true;
            }
        } finally {
            jlforkaddon$mirroring = false;
        }
    }

    @Unique
    @SuppressWarnings("unchecked")
    private static void jlforkaddon$mergeScreenInfoTabs(Object targetScreenInfo, Object sourceScreenInfo) {
        if (targetScreenInfo == null || sourceScreenInfo == null || targetScreenInfo == sourceScreenInfo) {
            return;
        }
        try {
            Field tabsField = jlforkaddon$getTabsField(targetScreenInfo);
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
        } catch (Throwable throwable) {
            if (!jlforkaddon$mergeFailed) {
                JLFORKADDON$LOGGER.warn("[compat] Failed merging LegendaryTabs screen maps; keeping best-effort mirror", throwable);
                jlforkaddon$mergeFailed = true;
            }
        }
    }

    @Unique
    private static Field jlforkaddon$getTabsField(Object screenInfo) {
        if (jlforkaddon$screenInfoTabsField != null) {
            return jlforkaddon$screenInfoTabsField;
        }
        try {
            Field field = screenInfo.getClass().getDeclaredField("tabs");
            field.setAccessible(true);
            jlforkaddon$screenInfoTabsField = field;
            return field;
        } catch (Throwable ignored) {
            return null;
        }
    }
}
