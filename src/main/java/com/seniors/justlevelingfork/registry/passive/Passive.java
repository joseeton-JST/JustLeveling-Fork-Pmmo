package com.seniors.justlevelingfork.registry.passive;

import com.seniors.justlevelingfork.JustLevelingFork;
import com.seniors.justlevelingfork.client.core.Utils;
import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import com.seniors.justlevelingfork.handler.HandlerConfigClient;
import com.seniors.justlevelingfork.handler.HandlerResources;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.RegistryPassives;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Passive {
    public final ResourceLocation key;
    public final Aptitude aptitude;
    public final ResourceLocation texture;
    public final Attribute attribute;
    public final String attributeUuid;
    public final Object attributeValue;
    public final int[] levelsRequired;
    private int pointCost = 1;

    public Passive(ResourceLocation passiveKey, Aptitude aptitude, ResourceLocation passiveTexture, Attribute attribute, String attributeUuid, Object attributeValue, int... levelsRequired) {
        this.key = passiveKey;
        this.aptitude = aptitude;
        this.texture = passiveTexture;
        this.attribute = attribute;
        this.attributeUuid = attributeUuid;
        this.attributeValue = attributeValue;
        this.levelsRequired = levelsRequired;
    }

    // KubeJS support
    public static Passive add(String passiveName, String aptitudeName, String texture, Attribute attribute, String attributeUUID, Object attributeValue, int... levelsRequired){
        String normalizedPassive = passiveName == null ? null : passiveName.toLowerCase(Locale.ROOT);
        ResourceLocation id = parseResourceLocation(normalizedPassive, true);
        if (id == null) {
            throw new IllegalArgumentException("Invalid passive name: " + passiveName);
        }
        return addWithId(id, aptitudeName, texture, attribute, attributeUUID, attributeValue, levelsRequired);
    }

    public static Passive addWithId(String passiveNameOrId, String aptitudeName, String texture, Attribute attribute, String attributeUUID, Object attributeValue, int... levelsRequired) {
        ResourceLocation id = parseResourceLocation(passiveNameOrId, false);
        if (id == null) {
            throw new IllegalArgumentException("Invalid passive id: " + passiveNameOrId);
        }
        return addWithId(id, aptitudeName, texture, attribute, attributeUUID, attributeValue, levelsRequired);
    }

    public static Passive addWithId(ResourceLocation id, String aptitudeName, String texture, Attribute attribute, String attributeUUID, Object attributeValue, int... levelsRequired) {
        if (id == null || id.getPath().isBlank()) {
            throw new IllegalArgumentException("Passive name cannot be empty");
        }
        if (aptitudeName == null || aptitudeName.isBlank()) {
            throw new IllegalArgumentException("Aptitude name cannot be empty for passive: " + id);
        }
        if (attribute == null) {
            throw new IllegalArgumentException("Attribute cannot be null for passive: " + id);
        }
        if (levelsRequired == null || levelsRequired.length == 0) {
            throw new IllegalArgumentException("levelsRequired must contain at least one value for passive: " + id);
        }

        String normalizedAptitude = aptitudeName.toLowerCase(Locale.ROOT);
        Aptitude aptitude = RegistryAptitudes.getAptitude(normalizedAptitude);
        if (aptitude == null){
            throw new IllegalArgumentException("Aptitude name doesn't exist: " + aptitudeName);
        }

        Passive passive = new Passive(id, aptitude, parseTexture(texture), attribute, attributeUUID, attributeValue, levelsRequired);
        RegistryPassives.addPendingPassive(id, passive);
        return passive;
    }

    // KubeJS support overload by attribute id
    public static Passive add(String passiveName, String aptitudeName, String texture, String attributeId, String attributeUUID, Object attributeValue, int... levelsRequired) {
        if (attributeId == null || attributeId.isBlank()) {
            throw new IllegalArgumentException("Attribute id cannot be empty for passive: " + passiveName);
        }

        ResourceLocation attributeLocation;
        try {
            attributeLocation = new ResourceLocation(attributeId);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid attribute id: " + attributeId, exception);
        }

        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(attributeLocation);
        if (attribute == null) {
            throw new IllegalArgumentException("Attribute id doesn't exist: " + attributeId);
        }

        return add(passiveName, aptitudeName, texture, attribute, attributeUUID, attributeValue, levelsRequired);
    }

    // KubeJS-friendly named overload to avoid JS overload ambiguity
    public static Passive addByAttributeId(String passiveName, String aptitudeName, String texture, String attributeId, String attributeUUID, Object attributeValue, int... levelsRequired) {
        return add(passiveName, aptitudeName, texture, attributeId, attributeUUID, attributeValue, levelsRequired);
    }

    public Passive get() {
        return this;
    }

    public String getMod() {
        return this.key.getNamespace();
    }

    public String getName() {
        return this.key.getPath();
    }

    public String getKey() {
        return "passive." + this.key.toLanguageKey();
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

    public Component getDescriptionComponentOrFallback() {
        String translationKey = getDescription();
        MutableComponent translated = Component.translatable(translationKey);
        if (translationKey.equals(translated.getString())) {
            return Component.literal(buildFallbackDescription());
        }
        return translated;
    }

    public double getValue() {
        double newValue = 0.0D;
        if (this.attributeValue != null) {
            Object object = this.attributeValue;
            if (object instanceof ForgeConfigSpec.DoubleValue) {
                ForgeConfigSpec.DoubleValue value = (ForgeConfigSpec.DoubleValue) object;
                newValue = value.get();
            }
            if (object instanceof ForgeConfigSpec.IntValue) {
                ForgeConfigSpec.IntValue value = (ForgeConfigSpec.IntValue) object;
                newValue = value.get();
            }
            if (object instanceof Number) {
                Number value = (Number) object;
                newValue = value.doubleValue();
            }

        }
        return newValue;
    }

    public int getNextLevelUp() {
        int[] requirement = new int[this.levelsRequired.length + 2];
        requirement[0] = 0;
        System.arraycopy(this.levelsRequired, 0, requirement, 1, this.levelsRequired.length);
        return requirement[getLevel() + 1];
    }

    public List<Component> tooltip() {
        DecimalFormat df = new DecimalFormat("0.##");
        String valuePerLevel = df.format(getValue() / this.levelsRequired.length);
        String valueActualLevel = df.format(getValue() / this.levelsRequired.length * getLevel());
        String valueMaxLevel = df.format(getValue());
        Component passiveName = getDisplayNameComponentOrFallback();
        boolean hasNextLevel = getLevel() < this.levelsRequired.length;
        boolean showDetails = Screen.hasShiftDown();

        List<Component> list = new ArrayList<>();
        list.add(Component.translatable("tooltip.passive.title").append(passiveName).withStyle(ChatFormatting.GREEN));
        list.add(Component.translatable("tooltip.passive.description.passive_level", getLevel(), this.levelsRequired.length).withStyle(ChatFormatting.GRAY));
        list.add(Component.empty());
        if (showDetails) {
            list.add(Component.empty()
                    .append(passiveName.copy().withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.UNDERLINE))
                    .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                    .append(getDescriptionComponentOrFallback().copy().withStyle(ChatFormatting.GRAY)));
            list.add(Component.empty());
            list.add(Component.translatable("tooltip.passive.description.other_info").withStyle(ChatFormatting.GRAY));
            list.add(Component.literal(" ").append(Component.translatable("tooltip.passive.description.level", valuePerLevel)).withStyle(ChatFormatting.DARK_GREEN));
            list.add(Component.literal(" ").append(Component.translatable("tooltip.passive.description.actual_level", valueActualLevel)).withStyle(ChatFormatting.DARK_GREEN));
            list.add(Component.literal(" ").append(Component.translatable("tooltip.passive.description.max_level", valueMaxLevel)).withStyle(ChatFormatting.DARK_GREEN));
        } else {
            list.add(Component.translatable("tooltip.general.description.more_information").withStyle(ChatFormatting.YELLOW));
        }
        if (hasNextLevel) {
            list.add(Component.empty());
            list.add(Component.translatable("tooltip.passive.description.level_requirement").withStyle(ChatFormatting.DARK_PURPLE));
            list.add(Component.literal(" ").append(Component.translatable("tooltip.passive.description.passive_required", Component.literal(this.aptitude.getDisplayNameOrFallback()).withStyle(ChatFormatting.GREEN),
                    Component.literal(String.valueOf(getNextLevelUp())).withStyle(ChatFormatting.GREEN),
                    Component.literal(String.valueOf(getLevel() + 1)).withStyle(ChatFormatting.GREEN))).withStyle(ChatFormatting.DARK_AQUA));
            list.add(Component.literal(" ").append(Component.translatable("tooltip.passive.cost.next",
                    Component.literal(String.valueOf(getPointCost())).withStyle(ChatFormatting.GREEN))).withStyle(ChatFormatting.GRAY));
        } else if (showDetails) {
            list.add(Component.empty());
            list.add(Component.translatable("tooltip.passive.description.level_requirement").withStyle(ChatFormatting.DARK_PURPLE));
            list.add(Component.literal(" ").append(Component.translatable("tooltip.passive.description.passive_max_level")).withStyle(ChatFormatting.DARK_AQUA));
        }
        if (HandlerConfigClient.showSkillModName.get()) {
            list.add(Component.literal(Utils.getModName(getMod())).withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.ITALIC));
        }

        return list;
    }

    public int getLevel() {
        return AptitudeCapability.get().getPassiveLevel(this);
    }

    public int getLevel(Player player) {
        return AptitudeCapability.get(player).getPassiveLevel(this);
    }

    public int getMaxLevel() {
        return this.levelsRequired.length;
    }

    public int getPointCost() {
        return Math.max(1, this.pointCost);
    }

    public void setPointCost(int cost) {
        this.pointCost = Math.max(1, cost);
    }

    public int getSpCost() {
        return getPointCost();
    }

    public void setSpCost(int cost) {
        setPointCost(cost);
    }

    public ResourceLocation getTexture() {
        return Objects.requireNonNullElse(this.texture, HandlerResources.NULL_SKILL);
    }

    private String buildFallbackDescription() {
        return getDisplayNameComponentOrFallback().getString();
    }

    private static String buildFallbackName(String id) {
        if (id == null || id.isBlank()) {
            return "Unknown Passive";
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

    private static ResourceLocation parseResourceLocation(String raw, boolean forceDefaultNamespace) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String normalized = raw.toLowerCase(Locale.ROOT);
        try {
            if (!forceDefaultNamespace && normalized.contains(":")) {
                return new ResourceLocation(normalized);
            }
            String path = normalized.contains(":")
                    ? normalized.substring(normalized.indexOf(':') + 1)
                    : normalized;
            return new ResourceLocation(JustLevelingFork.MOD_ID, path);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static ResourceLocation parseTexture(String texture) {
        if (texture == null || texture.isBlank()) {
            throw new IllegalArgumentException("Passive texture cannot be empty");
        }
        String normalized = texture.toLowerCase(Locale.ROOT);
        try {
            return normalized.contains(":")
                    ? new ResourceLocation(normalized)
                    : HandlerResources.create(normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid passive texture: " + texture, exception);
        }
    }
}


