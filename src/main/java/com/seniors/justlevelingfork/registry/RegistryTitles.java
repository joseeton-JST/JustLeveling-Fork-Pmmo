package com.seniors.justlevelingfork.registry;

import com.seniors.justlevelingfork.JustLevelingFork;
import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import com.seniors.justlevelingfork.config.models.TitleModel;
import com.seniors.justlevelingfork.handler.HandlerConditions;
import com.seniors.justlevelingfork.handler.HandlerTitlesConfig;
import com.seniors.justlevelingfork.registry.title.Title;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public class RegistryTitles {
    public static final ResourceKey<Registry<Title>> TITLES_KEY = ResourceKey.createRegistryKey(new ResourceLocation(JustLevelingFork.MOD_ID, "titles"));
    public static final DeferredRegister<Title> TITLES = DeferredRegister.create(TITLES_KEY, JustLevelingFork.MOD_ID);
    public static final Supplier<IForgeRegistry<Title>> TITLES_REGISTRY = TITLES.makeRegistry(() -> new RegistryBuilder<Title>().disableSaving());
    private static final Map<String, Title> PENDING_CUSTOM = new LinkedHashMap<>();
    private static final Map<String, List<String>> KUBEJS_CONDITIONS = new LinkedHashMap<>();

    public static final RegistryObject<Title> TITLELESS = TITLES.register("titleless", () -> register("titleless", true));
    public static final RegistryObject<Title> ADMIN = TITLES.register("administrator", () -> register("administrator", false));

    public static void load(IEventBus eventBus) {
        HandlerTitlesConfig.HANDLER.instance().titleList.forEach(title -> {
            title.registry(TITLES);
        });

        TITLES.register(eventBus);
        eventBus.addListener((RegisterEvent event) -> event.register(TITLES_KEY, helper ->
                PENDING_CUSTOM.forEach((name, title) ->
                        helper.register(new ResourceLocation(JustLevelingFork.MOD_ID, name), title))));

        // Title conditions
        HandlerConditions.registerDefaults();
    }

    private static Title register(String name, boolean requirement) {
        ResourceLocation key = new ResourceLocation(JustLevelingFork.MOD_ID, name);
        return new Title(key, requirement, true);
    }

    public static void addPendingTitle(String name, Title title) {
        if (title == null) {
            return;
        }

        String normalized = normalizeTitleName(name);
        if (normalized == null) {
            return;
        }
        PENDING_CUSTOM.put(normalized, title);
    }

    public static void setKubeJSConditions(String titleName, List<String> conditions) {
        String normalized = normalizeTitleName(titleName);
        if (normalized == null) {
            return;
        }

        List<String> normalizedConditions = new ArrayList<>();
        if (conditions != null) {
            for (String condition : conditions) {
                if (condition != null && !condition.isBlank()) {
                    normalizedConditions.add(condition.trim());
                }
            }
        }
        KUBEJS_CONDITIONS.put(normalized, normalizedConditions);
    }

    public static void clearKubeJSConditions(String titleName) {
        String normalized = normalizeTitleName(titleName);
        if (normalized == null) {
            return;
        }
        KUBEJS_CONDITIONS.remove(normalized);
    }

    public static Map<String, List<String>> getKubeJSConditionsSnapshot() {
        Map<String, List<String>> copy = new LinkedHashMap<>();
        KUBEJS_CONDITIONS.forEach((key, value) -> copy.put(key, List.copyOf(value)));
        return Collections.unmodifiableMap(copy);
    }

    public static Title getTitle(String titleName) {
        String normalized = normalizeTitleName(titleName);
        if (normalized == null) {
            return null;
        }

        Title pending = PENDING_CUSTOM.get(normalized);
        if (pending != null) {
            return pending;
        }

        IForgeRegistry<Title> registry = TITLES_REGISTRY.get();
        if (registry == null) {
            return null;
        }

        ResourceLocation parsedId = ResourceLocation.tryParse(titleName.toLowerCase(Locale.ROOT));
        if (parsedId != null) {
            Title byId = registry.getValue(parsedId);
            if (byId != null) {
                return byId;
            }
        }

        return registry.getValues().stream()
                .filter(title -> title.getName().equalsIgnoreCase(normalized))
                .findFirst()
                .orElse(null);
    }

    public static void syncTitles(ServerPlayer serverPlayer) {
        serverPlayerTitles(serverPlayer);
        serverPlayer.getCapability(RegistryCapabilities.APTITUDE).ifPresent(aptitudeCapability -> {
            Title title = getTitle(AptitudeCapability.get(serverPlayer).getPlayerTitle());
            if (title == null) {
                title = TITLELESS.get();
            }
            serverPlayer.setCustomName(title.getDisplayNameComponentOrFallback());
            serverPlayer.refreshDisplayName();
            serverPlayer.refreshTabListName();
        });
    }

    public static void serverPlayerTitles(ServerPlayer serverPlayer) {
        if (!serverPlayer.isDeadOrDying())
            serverPlayer.getCapability(RegistryCapabilities.APTITUDE).ifPresent(capability -> {
                for (TitleModel titleModel : HandlerTitlesConfig.HANDLER.instance().titleList) {
                    Title title = titleModel.getTitle();
                    if (title == null) {
                        continue;
                    }
                    boolean meetsRequirements = TitleModel.checkRequirements(
                            title.getName(),
                            titleModel.Conditions,
                            titleModel.Default,
                            serverPlayer
                    );
                    title.setRequirement(serverPlayer, meetsRequirements);
                }

                for (Map.Entry<String, List<String>> entry : new ArrayList<>(KUBEJS_CONDITIONS.entrySet())) {
                    Title title = getTitle(entry.getKey());
                    if (title == null) {
                        continue;
                    }

                    boolean meetsRequirements = TitleModel.checkRequirements(
                            title.getName(),
                            entry.getValue(),
                            title.Requirement,
                            serverPlayer
                    );
                    title.setRequirement(serverPlayer, meetsRequirements);
                }
                ADMIN.get().setRequirement(serverPlayer, serverPlayer.hasPermissions(2));
            });
    }

    private static String normalizeTitleName(String titleName) {
        if (titleName == null || titleName.isBlank()) {
            return null;
        }

        ResourceLocation parsed = ResourceLocation.tryParse(titleName.toLowerCase(Locale.ROOT));
        if (parsed != null) {
            return parsed.getPath().toLowerCase(Locale.ROOT);
        }

        return titleName.toLowerCase(Locale.ROOT);
    }
}


