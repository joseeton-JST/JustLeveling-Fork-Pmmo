package com.joseetoon.justlevellingaddonjs.mixin;

import com.seniors.justlevelingfork.client.core.Utils;
import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.seniors.justlevelingfork.registry.skills.Skill;
import com.joseetoon.justlevellingaddonjs.compat.CapabilityCompat;
import com.joseetoon.justlevellingaddonjs.config.AddonClientRuntimeSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Mixin(value = Skill.class, remap = false)
public abstract class MixSkillTooltip {
    @Inject(method = "tooltip", at = @At("HEAD"), cancellable = true, require = 0)
    private void jlforkaddon$tooltip(CallbackInfoReturnable<List<Component>> cir) {
        Skill self = (Skill) (Object) this;
        if (!hasMethod(self, "getDisplayNameComponentOrFallback") || !hasMethod(self, "getDescriptionComponentOrFallback")) {
            return;
        }
        AddonClientRuntimeSettings.Snapshot settings = AddonClientRuntimeSettings.current();

        Aptitude aptitude = readField(self, "aptitude", Aptitude.class, null);
        int requiredLevel = readIntField(self, "requiredLevel", 0);

        Component skillName = invokeComponent(self, "getDisplayNameComponentOrFallback", Component.literal("Skill"));
        Component description = invokeComponent(self, "getDescriptionComponentOrFallback", skillName.copy());
        boolean enabled = invokeBoolean(self, "canSkill", false);
        String modId = invokeString(self, "getMod", "minecraft");
        int pointCost = Math.max(1, invokeInt(self, "getPointCost", 1));
        int levelRequirement = Math.max(0, invokeInt(self, "getLvl", requiredLevel));

        AptitudeCapability capability = AptitudeCapability.get();
        boolean unlocked = capability != null && CapabilityCompat.isSkillUnlocked(capability, self);
        int availablePoints = capability != null && aptitude != null ? CapabilityCompat.getAptitudeSkillPointsAvailable(capability, aptitude) : 0;
        boolean enoughPoints = availablePoints >= pointCost;
        boolean showDetails = Screen.hasShiftDown();
        boolean canUnlock = !unlocked && requiredLevel > 0;
        List<Component> list = new ArrayList<>();
        list.add(Component.translatable("tooltip.skill.title").append(skillName).withStyle(ChatFormatting.AQUA));
        list.add(Component.translatable("tooltip.skill.description." + (enabled ? "on" : "off")).withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));
        if (unlocked) {
            list.add(Component.translatable("tooltip.skill.purchased").withStyle(ChatFormatting.GREEN));
        }
        list.add(Component.empty());
        if (showDetails) {
            list.add(Component.empty()
                    .append(skillName.copy().withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.UNDERLINE))
                    .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                    .append(description.copy().withStyle(ChatFormatting.GRAY)));
        } else {
            list.add(Component.translatable("tooltip.general.description.more_information").withStyle(ChatFormatting.YELLOW));
        }
        if (canUnlock) {
            list.add(Component.empty());
            list.add(Component.translatable("tooltip.skill.description.level_requirement").withStyle(ChatFormatting.DARK_PURPLE));
            list.add(Component.literal(" ").append(Component.translatable("tooltip.skill.description.available", Component.literal(String.valueOf(levelRequirement)).withStyle(ChatFormatting.GREEN))).withStyle(ChatFormatting.DARK_AQUA));
            if (settings.showSkillCostLine()) {
                Component costLine = Component.translatable(
                        "tooltip.skill.cost",
                        Component.literal(String.valueOf(pointCost)).withStyle(enoughPoints ? ChatFormatting.GREEN : ChatFormatting.RED)
                ).withStyle(ChatFormatting.GRAY);
                if (!jlforkaddon$isMissingTranslation(costLine, "tooltip.skill.cost")) {
                    list.add(costLine);
                }

                if (!enoughPoints) {
                    Component notEnoughLine = Component.translatable("tooltip.skill.cost.not_enough").withStyle(ChatFormatting.RED);
                    if (!jlforkaddon$isMissingTranslation(notEnoughLine, "tooltip.skill.cost.not_enough")) {
                        list.add(notEnoughLine);
                    }
                }
            }
        } else if (showDetails && requiredLevel <= 0) {
            list.add(Component.empty());
            list.add(Component.translatable("tooltip.skill.description.level_requirement").withStyle(ChatFormatting.DARK_PURPLE));
            list.add(Component.translatable("tooltip.skill.description.off").withStyle(ChatFormatting.RED));
        }
        if (settings.showModNameInTooltips()) {
            list.add(Component.literal(Utils.getModName(modId)).withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.ITALIC));
        }

        cir.setReturnValue(list);
    }

    private static boolean invokeBoolean(Object target, String method, boolean fallback) {
        try {
            Method m = target.getClass().getMethod(method);
            Object value = m.invoke(target);
            return value instanceof Boolean b ? b : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static int invokeInt(Object target, String method, int fallback) {
        try {
            Method m = target.getClass().getMethod(method);
            Object value = m.invoke(target);
            return value instanceof Number n ? n.intValue() : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static String invokeString(Object target, String method, String fallback) {
        try {
            Method m = target.getClass().getMethod(method);
            Object value = m.invoke(target);
            return value instanceof String s ? s : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static Component invokeComponent(Object target, String method, Component fallback) {
        try {
            Method m = target.getClass().getMethod(method);
            Object value = m.invoke(target);
            return value instanceof Component c ? c : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static boolean hasMethod(Object target, String methodName) {
        try {
            target.getClass().getMethod(methodName);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static int readIntField(Object target, String field, int fallback) {
        try {
            Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            Object value = f.get(target);
            return value instanceof Number n ? n.intValue() : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static <T> T readField(Object target, String field, Class<T> type, T fallback) {
        try {
            Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            Object value = f.get(target);
            return type.isInstance(value) ? type.cast(value) : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static boolean jlforkaddon$isMissingTranslation(Component component, String key) {
        return component != null && key.equals(component.getString());
    }
}
