package com.joseetoon.justlevellingaddonjs.mixin;

import com.seniors.justlevelingfork.client.core.Utils;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.seniors.justlevelingfork.registry.passive.Passive;
import com.joseetoon.justlevellingaddonjs.compat.AptitudeCompat;
import com.joseetoon.justlevellingaddonjs.config.AddonClientRuntimeSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.text.DecimalFormat;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Mixin(value = Passive.class, remap = false)
public abstract class MixPassiveTooltip {
    @Inject(method = "tooltip", at = @At("HEAD"), cancellable = true, require = 0)
    private void jlforkaddon$tooltip(CallbackInfoReturnable<List<Component>> cir) {
        Passive self = (Passive) (Object) this;
        if (!hasMethod(self, "getDisplayNameComponentOrFallback") || !hasMethod(self, "getDescriptionComponentOrFallback")) {
            return;
        }
        AddonClientRuntimeSettings.Snapshot settings = AddonClientRuntimeSettings.current();

        int[] levelsRequired = readIntArrayField(self, "levelsRequired", new int[0]);
        Aptitude aptitude = readField(self, "aptitude", Aptitude.class, null);
        double value = invokeDouble(self, "getValue", 0.0D);
        int level = Math.max(0, invokeInt(self, "getLevel", 0));
        int pointCost = Math.max(1, invokeInt(self, "getPointCost", 1));
        int nextLevelUp = Math.max(0, invokeInt(self, "getNextLevelUp", 0));
        String modId = invokeString(self, "getMod", "minecraft");
        Component passiveName = invokeComponent(self, "getDisplayNameComponentOrFallback", Component.literal("Passive"));
        Component description = invokeComponent(self, "getDescriptionComponentOrFallback", passiveName.copy());

        int maxLevel = levelsRequired.length;
        if (maxLevel <= 0) {
            maxLevel = 1;
        }

        DecimalFormat df = new DecimalFormat("0.##");
        String valuePerLevel = df.format(value / maxLevel);
        String valueActualLevel = df.format(value / maxLevel * level);
        String valueMaxLevel = df.format(value);
        boolean hasNextLevel = level < maxLevel;
        boolean showDetails = Screen.hasShiftDown();

        List<Component> list = new ArrayList<>();
        list.add(Component.translatable("tooltip.passive.title").append(passiveName).withStyle(ChatFormatting.GREEN));
        list.add(Component.translatable("tooltip.passive.description.passive_level", level, maxLevel).withStyle(ChatFormatting.GRAY));
        list.add(Component.empty());
        if (showDetails) {
            list.add(Component.empty()
                    .append(passiveName.copy().withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.UNDERLINE))
                    .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                    .append(description.copy().withStyle(ChatFormatting.GRAY)));
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
            String aptitudeName = aptitude != null ? AptitudeCompat.getDisplayNameOrFallback(aptitude) : "Aptitude";
            list.add(Component.literal(" ").append(Component.translatable("tooltip.passive.description.passive_required", Component.literal(aptitudeName).withStyle(ChatFormatting.GREEN),
                    Component.literal(String.valueOf(nextLevelUp)).withStyle(ChatFormatting.GREEN),
                    Component.literal(String.valueOf(level + 1)).withStyle(ChatFormatting.GREEN))).withStyle(ChatFormatting.DARK_AQUA));
            if (settings.showPassiveNextCostLine()) {
                list.add(Component.literal(" ").append(Component.translatable("tooltip.passive.cost.next",
                        Component.literal(String.valueOf(pointCost)).withStyle(ChatFormatting.GREEN))).withStyle(ChatFormatting.GRAY));
            }
        } else if (showDetails) {
            list.add(Component.empty());
            list.add(Component.translatable("tooltip.passive.description.level_requirement").withStyle(ChatFormatting.DARK_PURPLE));
            list.add(Component.literal(" ").append(Component.translatable("tooltip.passive.description.passive_max_level")).withStyle(ChatFormatting.DARK_AQUA));
        }
        if (settings.showModNameInTooltips()) {
            list.add(Component.literal(Utils.getModName(modId)).withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.ITALIC));
        }

        cir.setReturnValue(list);
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

    private static double invokeDouble(Object target, String method, double fallback) {
        try {
            Method m = target.getClass().getMethod(method);
            Object value = m.invoke(target);
            return value instanceof Number n ? n.doubleValue() : fallback;
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

    private static int[] readIntArrayField(Object target, String field, int[] fallback) {
        try {
            Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            Object value = f.get(target);
            return value instanceof int[] array ? array : fallback;
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
}
