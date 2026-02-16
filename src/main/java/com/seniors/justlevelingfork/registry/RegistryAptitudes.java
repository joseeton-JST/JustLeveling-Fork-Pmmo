package com.seniors.justlevelingfork.registry;

import com.seniors.justlevelingfork.JustLevelingFork;
import com.seniors.justlevelingfork.handler.HandlerResources;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class RegistryAptitudes {

    private static final ResourceKey<Registry<Aptitude>> APTITUDES_KEY = ResourceKey.createRegistryKey(new ResourceLocation(JustLevelingFork.MOD_ID, "aptitudes"));
    private static final DeferredRegister<Aptitude> APTITUDES = DeferredRegister.create(APTITUDES_KEY, JustLevelingFork.MOD_ID);
    private static final Map<ResourceLocation, Aptitude> PENDING_CUSTOM = new LinkedHashMap<>();
    private static int nextIndex = 8;
    public static Supplier<IForgeRegistry<Aptitude>> APTITUDES_REGISTRY = APTITUDES.makeRegistry(() -> new RegistryBuilder<Aptitude>().disableSaving());

    public static final RegistryObject<Aptitude> STRENGTH = APTITUDES.register("strength", () -> register(0, "strength", HandlerResources.STRENGTH_LOCKED_ICON, new ResourceLocation("minecraft:textures/block/yellow_terracotta.png")));
    public static final RegistryObject<Aptitude> CONSTITUTION = APTITUDES.register("constitution", () -> register(1, "constitution", HandlerResources.CONSTITUTION_LOCKED_ICON, new ResourceLocation("minecraft:textures/block/red_terracotta.png")));
    public static final RegistryObject<Aptitude> DEXTERITY = APTITUDES.register("dexterity", () -> register(2, "dexterity", HandlerResources.DEXTERITY_LOCKED_ICON, new ResourceLocation("minecraft:textures/block/blue_terracotta.png")));
    public static final RegistryObject<Aptitude> DEFENSE = APTITUDES.register("defense", () -> register(3, "defense", HandlerResources.DEFENSE_LOCKED_ICON, new ResourceLocation("minecraft:textures/block/cyan_terracotta.png")));
    public static final RegistryObject<Aptitude> INTELLIGENCE = APTITUDES.register("intelligence", () -> register(4, "intelligence", HandlerResources.INTELLIGENCE_LOCKED_ICON, new ResourceLocation("minecraft:textures/block/orange_terracotta.png")));
    public static final RegistryObject<Aptitude> BUILDING = APTITUDES.register("building", () -> register(5, "building", HandlerResources.BUILDING_LOCKED_ICON, new ResourceLocation("minecraft:textures/block/brown_terracotta.png")));
    public static final RegistryObject<Aptitude> MAGIC = APTITUDES.register("magic", () -> register(6, "magic", HandlerResources.MAGIC_LOCKED_ICON, new ResourceLocation("minecraft:textures/block/purple_terracotta.png")));
    public static final RegistryObject<Aptitude> LUCK = APTITUDES.register("luck", () -> register(7, "luck", HandlerResources.LUCK_LOCKED_ICON, new ResourceLocation("minecraft:textures/block/lime_terracotta.png")));

    public static void load(IEventBus eventBus) {
        APTITUDES.register(eventBus);
        eventBus.addListener((RegisterEvent event) -> event.register(APTITUDES_KEY, helper ->
                PENDING_CUSTOM.forEach(helper::register)));
    }

    private static Aptitude register(int index, String name, ResourceLocation[] lockedTexture, ResourceLocation background) {
        ResourceLocation key = new ResourceLocation(JustLevelingFork.MOD_ID, name);
        return new Aptitude(index, key, lockedTexture, background);
    }

    public static int getNextIndex() {
        return nextIndex++;
    }

    public static void addPendingAptitude(String name, Aptitude aptitude) {
        if (name == null || aptitude == null) {
            return;
        }

        String normalized = name.toLowerCase(Locale.ROOT);
        addPendingAptitude(new ResourceLocation(JustLevelingFork.MOD_ID, normalized), aptitude);
    }

    public static void addPendingAptitude(ResourceLocation id, Aptitude aptitude) {
        if (id == null || aptitude == null) {
            return;
        }
        PENDING_CUSTOM.put(id, aptitude);
    }

    public static Aptitude getAptitude(String aptitudeNameOrId) {
        if (aptitudeNameOrId == null || aptitudeNameOrId.isEmpty()) {
            return null;
        }

        ResourceLocation parsed = parseResourceLocation(aptitudeNameOrId);
        IForgeRegistry<Aptitude> registry = APTITUDES_REGISTRY.get();

        if (parsed != null) {
            Aptitude pendingById = PENDING_CUSTOM.get(parsed);
            if (pendingById != null) {
                return pendingById;
            }
            if (registry != null) {
                Aptitude registeredById = registry.getValue(parsed);
                if (registeredById != null) {
                    return registeredById;
                }
            }
        }

        String normalizedPath = parsed != null
                ? parsed.getPath().toLowerCase(Locale.ROOT)
                : aptitudeNameOrId.toLowerCase(Locale.ROOT);

        Aptitude pendingByPath = PENDING_CUSTOM.entrySet().stream()
                .filter(entry -> entry.getKey().getPath().equalsIgnoreCase(normalizedPath))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
        if (pendingByPath != null) {
            return pendingByPath;
        }

        if (registry == null) {
            return null;
        }

        return registry.getValues().stream()
                .filter(aptitude -> aptitude.getName().equalsIgnoreCase(normalizedPath))
                .findFirst()
                .orElse(null);
    }

    public static List<String> getPendingAptitudeNames() {
        return PENDING_CUSTOM.keySet().stream().map(Objects::toString).toList();
    }

    private static ResourceLocation parseResourceLocation(String raw) {
        String normalized = raw.toLowerCase(Locale.ROOT);
        try {
            return normalized.contains(":")
                    ? new ResourceLocation(normalized)
                    : new ResourceLocation(JustLevelingFork.MOD_ID, normalized);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}


