package com.seniors.justlevelingfork.registry.skills;

import com.seniors.justlevelingfork.JustLevelingFork;
import com.seniors.justlevelingfork.client.core.Utils;
import com.seniors.justlevelingfork.client.core.Value;
import com.seniors.justlevelingfork.client.core.ValueType;
import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import com.seniors.justlevelingfork.handler.HandlerConfigClient;
import com.seniors.justlevelingfork.handler.HandlerResources;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.RegistryCapabilities;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class Skill {
    public final ResourceLocation key;
    public final Aptitude aptitude;
    public final int requiredLevel;
    public final ResourceLocation texture;
    private final Value[] configValues;
    private int pointCost = 1;

    public Skill(ResourceLocation skillKey, Aptitude aptitude, int levelRequirement, ResourceLocation skillTexture, Value... skillValues) {
        this.key = skillKey;
        this.aptitude = aptitude;
        this.requiredLevel = levelRequirement;
        this.texture = skillTexture;
        this.configValues = skillValues;
    }

    // KubeJS support
    public static Skill add(String skillName, String aptitudeName, int levelRequirement, String texture, Value... skillValues){
        String normalizedSkill = skillName == null ? null : skillName.toLowerCase(Locale.ROOT);
        ResourceLocation id = parseResourceLocation(normalizedSkill, true);
        if (id == null) {
            throw new IllegalArgumentException("Invalid skill name: " + skillName);
        }
        return addWithId(id, aptitudeName, levelRequirement, texture, skillValues);
    }

    public static Skill addWithId(String skillNameOrId, String aptitudeName, int levelRequirement, String texture, Value... skillValues) {
        ResourceLocation id = parseResourceLocation(skillNameOrId, false);
        if (id == null) {
            throw new IllegalArgumentException("Invalid skill id: " + skillNameOrId);
        }
        return addWithId(id, aptitudeName, levelRequirement, texture, skillValues);
    }

    public static Skill addWithId(ResourceLocation id, String aptitudeName, int levelRequirement, String texture, Value... skillValues) {
        if (id == null || id.getPath().isBlank()) {
            throw new IllegalArgumentException("Skill name cannot be empty");
        }
        if (aptitudeName == null || aptitudeName.isBlank()) {
            throw new IllegalArgumentException("Aptitude name cannot be empty for skill: " + id);
        }

        String normalizedAptitude = aptitudeName.toLowerCase(Locale.ROOT);
        Aptitude aptitude = RegistryAptitudes.getAptitude(normalizedAptitude);
        if (aptitude == null){
            throw new IllegalArgumentException("Aptitude name doesn't exist: " + aptitudeName);
        }

        Skill skill = new Skill(id, aptitude, levelRequirement, parseTexture(texture), skillValues);
        RegistrySkills.addPendingSkill(id, skill);
        return skill;
    }

    public Skill get() {
        return this;
    }

    public String getMod() {
        return this.key.getNamespace();
    }

    public String getName() {
        return this.key.getPath();
    }

    public String getKey() {
        return "skill." + this.key.toLanguageKey();
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
        MutableComponent translated = getMutableDescription(translationKey);
        if (translationKey.equals(translated.getString())) {
            return Component.literal(buildFallbackDescription());
        }
        return translated;
    }

    public int getLvl() {
        return this.requiredLevel;
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

    public double[] getValue() {
        double[] newValue = new double[this.configValues.length];
        for (int i = 0; i < newValue.length; i++) {
            newValue[i] = 0.0D;
            if (this.configValues[i] != null) {
                Object object = (this.configValues[i]).value;
                if (object instanceof Double) {
                    newValue[i] = (Double) object;
                }
                object = (this.configValues[i]).value;
                if (object instanceof Integer) {
                    newValue[i] = (Integer) object;
                }
                object = (this.configValues[i]).value;
                if (object instanceof Number value) {
                    newValue[i] = value.doubleValue();
                }
            }
        }
        return newValue;
    }

    public MutableComponent getMutableDescription(String description) {
        Object[] newValue = new Object[this.configValues.length];
        for (int i = 0; i < newValue.length; i++) {
            if (this.configValues[i] != null) {
                newValue[i] = getParameter((this.configValues[i]).type, getValue()[i]);
            }
        }
        return Component.translatable(description, newValue);
    }

    public String getParameter(ValueType type, double parameterValue) {
        DecimalFormat df = new DecimalFormat("0.##");
        String probabilityValue = Utils.periodValue(1.0D / parameterValue * 100.0D);
        String parameter = df.format(parameterValue);
        if (type.equals(ValueType.MODIFIER)) parameter = "§cx" + parameter;
        if (type.equals(ValueType.DURATION)) parameter = "§9" + parameter + "s";
        if (type.equals(ValueType.AMPLIFIER)) parameter = "§6+" + parameter;
        if (type.equals(ValueType.PERCENT)) parameter = "§2" + parameter + "%";
        if (type.equals(ValueType.BOOST)) parameter = "§d" + Utils.intToRoman(Integer.parseInt(parameter));
        if (type.equals(ValueType.PROBABILITY))
            parameter = "§e1/" + parameter + "§r§7 (§2" + probabilityValue + "%§7§r)";
        return parameter + "§r§7";
    }

    public List<Component> tooltip() {
        Component skillName = getDisplayNameComponentOrFallback();
        AptitudeCapability capability = AptitudeCapability.get();
        boolean unlocked = capability != null && capability.isSkillUnlocked(this);
        int availablePoints = capability != null ? capability.getAptitudeSkillPointsAvailable(this.aptitude) : 0;
        boolean enoughPoints = availablePoints >= getPointCost();
        boolean showDetails = Screen.hasShiftDown();
        boolean canUnlock = !unlocked && this.requiredLevel > 0;
        List<Component> list = new ArrayList<>();
        list.add(Component.translatable("tooltip.skill.title").append(skillName).withStyle(ChatFormatting.AQUA));
        list.add(Component.translatable("tooltip.skill.description." + (canSkill() ? "on" : "off")).withStyle(canSkill() ? ChatFormatting.GREEN : ChatFormatting.RED));
        if (unlocked) {
            list.add(Component.translatable("tooltip.skill.purchased").withStyle(ChatFormatting.GREEN));
        }
        list.add(Component.empty());
        if (showDetails) {
            list.add(Component.empty()
                    .append(skillName.copy().withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.UNDERLINE))
                    .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                    .append(getDescriptionComponentOrFallback().copy().withStyle(ChatFormatting.GRAY)));
        } else {
            list.add(Component.translatable("tooltip.general.description.more_information").withStyle(ChatFormatting.YELLOW));
        }
        if (canUnlock) {
            list.add(Component.empty());
            list.add(Component.translatable("tooltip.skill.description.level_requirement").withStyle(ChatFormatting.DARK_PURPLE));
            list.add(Component.literal(" ").append(Component.translatable("tooltip.skill.description.available", Component.literal(String.valueOf(getLvl())).withStyle(ChatFormatting.GREEN))).withStyle(ChatFormatting.DARK_AQUA));
            list.add(Component.translatable("tooltip.skill.cost", Component.literal(String.valueOf(getPointCost())).withStyle(enoughPoints ? ChatFormatting.GREEN : ChatFormatting.RED)).withStyle(ChatFormatting.GRAY));
            if (!enoughPoints) {
                list.add(Component.translatable("tooltip.skill.cost.not_enough").withStyle(ChatFormatting.RED));
            }
        } else if (showDetails && this.requiredLevel <= 0) {
            list.add(Component.empty());
            list.add(Component.translatable("tooltip.skill.description.level_requirement").withStyle(ChatFormatting.DARK_PURPLE));
            list.add(Component.translatable("tooltip.skill.description.off").withStyle(ChatFormatting.RED));
        }
        if (HandlerConfigClient.showSkillModName.get()) {
            list.add(Component.literal(Utils.getModName(getMod())).withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.ITALIC));
        }
        return list;
    }

    public boolean canSkill() {
        AptitudeCapability capability = AptitudeCapability.get();
        return capability != null
                && this.aptitude.isEnabled()
                && requiredLevel > 0
                && capability.getAptitudeLevel(this.aptitude) >= this.requiredLevel
                && capability.isSkillUnlocked(this)
                && capability.getToggleSkill(this);
    }

    public boolean canSkill(Player player) {
        AptitudeCapability capability = AptitudeCapability.get(player);
        return capability != null
                && this.aptitude.isEnabled()
                && requiredLevel > 0
                && capability.getAptitudeLevel(this.aptitude) >= this.requiredLevel
                && capability.isSkillUnlocked(this)
                && capability.getToggleSkill(this);
    }

    public boolean getToggle() {
        return this.aptitude.isEnabled() && this.requiredLevel > 0 && AptitudeCapability.get().getAptitudeLevel(this.aptitude) >= this.requiredLevel;
    }

    public boolean getToggle(Player player) {
        return this.aptitude.isEnabled() && this.requiredLevel > 0 && AptitudeCapability.get(player).getAptitudeLevel(this.aptitude) >= this.requiredLevel;
    }

    public boolean isEnabled() {
        if (!this.aptitude.isEnabled()) return false;
        if (this.requiredLevel < 1) return true;
        if(AptitudeCapability.get() == null) return false;

        return (AptitudeCapability.get().getAptitudeLevel(this.aptitude) >= this.requiredLevel
                && AptitudeCapability.get().isSkillUnlocked(this)
                && AptitudeCapability.get().getToggleSkill(this));
    }

    public boolean isEnabled(Player player) {
        AtomicBoolean b = new AtomicBoolean(false);
        if (player != null &&
                !player.isDeadOrDying()) {
            player.getCapability(RegistryCapabilities.APTITUDE).ifPresent(aptitudeCapability -> b.set(
                    this.aptitude.isEnabled()
                            && this.requiredLevel > 0
                            && AptitudeCapability.get(player).getAptitudeLevel(this.aptitude) >= this.requiredLevel
                            && AptitudeCapability.get(player).isSkillUnlocked(this)
                            && AptitudeCapability.get(player).getToggleSkill(this)));
        }

        return b.get();
    }

    public ResourceLocation getTexture() {
        return Objects.requireNonNullElse(this.texture, HandlerResources.NULL_SKILL);
    }

    private String buildFallbackDescription() {
        return getDisplayNameComponentOrFallback().getString();
    }

    private static String buildFallbackName(String id) {
        if (id == null || id.isBlank()) {
            return "Unknown Skill";
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
            throw new IllegalArgumentException("Skill texture cannot be empty");
        }
        String normalized = texture.toLowerCase(Locale.ROOT);
        try {
            return normalized.contains(":")
                    ? new ResourceLocation(normalized)
                    : HandlerResources.create(normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid skill texture: " + texture, exception);
        }
    }
}


