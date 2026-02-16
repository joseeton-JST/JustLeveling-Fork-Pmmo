package com.seniors.justlevelingfork.registry.title;

import com.seniors.justlevelingfork.JustLevelingFork;
import com.seniors.justlevelingfork.client.core.Utils;
import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import com.seniors.justlevelingfork.handler.HandlerConfigClient;
import com.seniors.justlevelingfork.network.packet.client.SyncAptitudeCapabilityCP;
import com.seniors.justlevelingfork.network.packet.client.TitleOverlayCP;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Title {
    private final ResourceLocation key;
    public final boolean Requirement;
    public final boolean HideRequirements;

    public Title(ResourceLocation key, boolean requirement, boolean hideRequirements) {
        this.key = key;
        this.Requirement = requirement;
        this.HideRequirements = hideRequirements;
    }

    public Title get() {
        return this;
    }

    public static Title add(String name) {
        return add(name, false, false);
    }

    public static Title add(String name, boolean defaultUnlocked, boolean hideRequirements) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Title name can't be null or empty");
        }

        String normalized = name.toLowerCase(Locale.ROOT);
        Title title = new Title(new ResourceLocation(JustLevelingFork.MOD_ID, normalized), defaultUnlocked, hideRequirements);
        RegistryTitles.addPendingTitle(normalized, title);
        return title;
    }

    public static Title getByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return RegistryTitles.getTitle(name.toLowerCase(Locale.ROOT));
    }

    public String getMod() {
        return this.key.getNamespace();
    }

    public String getName() {
        return this.key.getPath();
    }

    public String getKey() {
        return "title." + this.key.toLanguageKey();
    }

    public String getDescription() {
        return getKey() + ".description";
    }

    public Component getDisplayNameComponentOrFallback() {
        String translationKey = getKey();
        MutableComponent translated = Component.translatable(translationKey);
        if (translationKey.equals(translated.getString())) {
            return Component.literal(buildFallbackName(getName()));
        }
        return translated;
    }

    public String getDisplayNameOrFallback() {
        return getDisplayNameComponentOrFallback().getString();
    }

    public Component getDescriptionComponentOrFallback() {
        String translationKey = getDescription();
        MutableComponent translated = Component.translatable(translationKey);
        if (translationKey.equals(translated.getString())) {
            return Component.literal(getDisplayNameOrFallback());
        }
        return translated;
    }

    public boolean getRequirement() {
        return AptitudeCapability.get().getLockTitle(this);
    }

    public boolean getRequirement(Player player) {
        return AptitudeCapability.get(player).getLockTitle(this);
    }

    public void setRequirement(ServerPlayer serverPlayer, boolean check) {
        if (!getRequirement(serverPlayer) && check) {
            TitleOverlayCP.send(serverPlayer, this);
            AptitudeCapability.get(serverPlayer).setUnlockTitle(this, true);
            SyncAptitudeCapabilityCP.send(serverPlayer);
        }
    }

    public List<Component> tooltip() {
        List<Component> list = new ArrayList<>();
        list.add(Component.empty()
                .append(Component.translatable("title.justlevelingfork.requirement_description").withStyle(ChatFormatting.GOLD))
                .append(getDescriptionComponentOrFallback().copy().withStyle(ChatFormatting.GRAY)));
        if (HandlerConfigClient.showTitleModName.get())
            list.add(Component.literal(Utils.getModName(getMod())).withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.ITALIC));
        return list;
    }

    private static String buildFallbackName(String id) {
        if (id == null || id.isBlank()) {
            return "Unknown Title";
        }

        String[] parts = id.split("[_\\-\\s]+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }

            if (builder.length() > 0) {
                builder.append(' ');
            }

            String normalized = part.toLowerCase(Locale.ROOT);
            builder.append(Character.toUpperCase(normalized.charAt(0)));
            if (normalized.length() > 1) {
                builder.append(normalized.substring(1));
            }
        }

        return builder.isEmpty() ? id : builder.toString();
    }
}


