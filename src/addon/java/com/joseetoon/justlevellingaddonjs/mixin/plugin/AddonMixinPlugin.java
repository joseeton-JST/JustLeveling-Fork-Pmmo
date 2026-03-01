package com.joseetoon.justlevellingaddonjs.mixin.plugin;

import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AddonMixinPlugin implements IMixinConfigPlugin {
    private static final Logger LOGGER = LogManager.getLogger("JustLevellingAddonJS-MixinPlugin");
    private static final String EXPECTED_BASE_VERSION = "1.2.1";
    private static volatile boolean fingerprintValidated = false;

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        ensureBase121Fingerprint();
        String mixinSimple = mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1);
        if ("MixLegendaryTabsAddTabMirror".equals(mixinSimple)) {
            boolean legendaryLoaded = isLegendaryTabsLoaded();
            ClassNode legendaryNode = loadClassNode(targetClassName);
            if (!legendaryLoaded && legendaryNode == null && !classExists(targetClassName)) {
                return false;
            }
            if (legendaryNode == null) {
                LOGGER.debug("[compat] Applying '{}' for '{}' using fallback gate (modLoaded={}, classNodeAvailable=false).",
                        mixinClassName, targetClassName, legendaryLoaded);
                return true;
            }

            return hasMethod(legendaryNode, "addTabToScreen") && hasField(legendaryNode, "tabsScreens");
        }

        ClassNode node = loadClassNode(targetClassName);
        if (node == null) {
            if (isOptionalThirdPartyMixin(mixinSimple)) {
                if (!classExists(targetClassName)) {
                    LOGGER.debug("[compat] Skipping optional mixin '{}' because target '{}' is not present.", mixinClassName, targetClassName);
                    return false;
                }
                warnUnknown(mixinClassName, targetClassName, "target class bytecode unavailable during discovery");
                return true;
            }
            warnUnknown(mixinClassName, targetClassName, "target class bytecode unavailable during discovery");
            return true;
        }

        return switch (mixinSimple) {
            case "MixRegistryAptitudesBackport" -> !hasMethodQuiet(node, "addPendingAptitude") || !hasMethodQuiet(node, "getNextIndex");
            case "MixRegistrySkillsBackport" -> !hasMethodQuiet(node, "addPendingSkill");
            case "MixRegistryPassivesBackport" -> !hasMethodQuiet(node, "addPendingPassive");
            case "MixRegistryTitlesBackport" -> !hasMethodQuiet(node, "setKubeJSConditions") || !hasMethodQuiet(node, "addPendingTitle");
            case "MixRegistryTitlesCrudControls" -> hasMethod(node, "getTitle") && hasMethod(node, "serverPlayerTitles");
            case "MixAptitudeBackportApi" -> !hasMethodQuiet(node, "getLevelCap") || !hasMethodQuiet(node, "addWithId");
            case "MixSkillBackportApi" -> !hasMethodQuiet(node, "addWithId") || !hasMethodQuiet(node, "getPointCost");
            case "MixPassiveBackportApi" -> !hasMethodQuiet(node, "addWithId") || !hasMethodQuiet(node, "getPointCost");
            case "MixTitleBackportApi" -> !hasMethodQuiet(node, "getDisplayNameComponentOrFallback") || !hasMethodQuiet(node, "add");
            case "MixTitleRuntimeGuards" -> hasMethod(node, "setRequirement")
                    && hasMethod(node, "getDisplayNameComponentOrFallback")
                    && hasMethod(node, "getDescriptionComponentOrFallback");
            case "MixTitleOverlayCPDedup" -> hasMethod(node, "handle") && hasField(node, "title");
            case "MixAptitudeCapabilityParity" -> !hasMethodQuiet(node, "isSkillUnlocked")
                    || !hasMethodQuiet(node, "tryUnlockSkill")
                    || !hasMethodQuiet(node, "getAptitudeSkillPointsAvailable");
            case "MixAptitudeBackgroundStringAlias" -> true;
            case "MixKubeJSPluginBindings" -> hasMethod(node, "registerBindings");
            case "MixAptitudeLevelUpSP" -> hasMethod(node, "handle") && hasField(node, "aptitude");
            case "MixToggleSkillSP" -> hasMethod(node, "handle") && hasField(node, "skill") && hasField(node, "toggle");
            case "MixPassiveLevelUpSP", "MixPassiveLevelDownSP" -> hasMethod(node, "handle") && hasField(node, "passive");
            case "MixAptitudeLevelCommand" -> hasMethod(node, "setAptitude") && hasMethod(node, "addAptitude");
            case "MixAptitudeArgumentDeletedGuard" -> hasMethod(node, "parse") && hasMethod(node, "listSuggestions");
            case "MixTitleArgumentDeletedGuard" -> hasMethod(node, "getResource") && hasMethod(node, "listSuggestions");
            case "MixTitleCommandDeletedGuard" -> hasMethod(node, "setTitle");
            case "MixRegistryCommonEvents" -> hasMethod(node, "onLeftClickBlock");
            case "MixL2TabsIntegrationLegendaryCompat" -> hasMethod(node, "isModLoaded");
            case "MixDrawTabsOffset" -> hasMethod(node, "render") && hasMethod(node, "renderWidget");
            case "MixLegendaryTabsAddTabMirror" -> hasMethod(node, "addTabToScreen") && hasField(node, "tabsScreens");
            case "MixSkillTooltip", "MixPassiveTooltip" -> hasMethod(node, "tooltip");
            case "MixJLScreenVisibilityGuard" -> hasMethod(node, "drawSkills");
            case "MixJLScreenAptitudeCompat" -> hasMethod(node, "drawAptitudes") && hasMethod(node, "mouseScrolled");
            case "MixJLScreenAptitudeNameFallback" -> hasMethod(node, "drawSkills");
            case "MixJLScreenOptionalControls" -> hasMethod(node, "drawSkills") && hasMethod(node, "drawTitles");
            case "MixJLScreenTitlesFilter" -> hasMethod(node, "drawTitles");
            case "MixPlayerRendererTitleColor" -> true;
            default -> true;
        };
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    private static ClassNode loadClassNode(String className) {
        try {
            return MixinService.getService().getBytecodeProvider().getClassNode(className, false);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean classExists(String className) {
        try {
            Class.forName(className, false, AddonMixinPlugin.class.getClassLoader());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean isLegendaryTabsLoaded() {
        try {
            return ModList.get().isLoaded("legendarytabs");
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static synchronized void ensureBase121Fingerprint() {
        if (fingerprintValidated) {
            return;
        }

        ClassNode aptitudeNode = loadClassNode("com.seniors.justlevelingfork.registry.aptitude.Aptitude");
        ClassNode capabilityNode = loadClassNode("com.seniors.justlevelingfork.common.capability.AptitudeCapability");
        ClassNode levelUpNode = loadClassNode("com.seniors.justlevelingfork.network.packet.common.AptitudeLevelUpSP");
        ClassNode handlerNode = loadClassNode("com.seniors.justlevelingfork.handler.HandlerAptitude");

        if (aptitudeNode == null || capabilityNode == null || levelUpNode == null || handlerNode == null) {
            LOGGER.debug("[compat] Deferred base ABI fingerprint check: class nodes not yet available");
            return;
        }

        List<String> failures = new ArrayList<>();
        if (!hasFieldQuiet(aptitudeNode, "index")) failures.add("Aptitude.index");
        if (!hasFieldQuiet(aptitudeNode, "key")) failures.add("Aptitude.key");
        if (!hasFieldQuiet(aptitudeNode, "lockedTexture")) failures.add("Aptitude.lockedTexture");
        if (!hasFieldQuiet(aptitudeNode, "background")) failures.add("Aptitude.background");
        if (!hasMethodQuiet(aptitudeNode, "getName", "()Ljava/lang/String;")) failures.add("Aptitude.getName()");
        if (!hasMethodQuiet(aptitudeNode, "getLevel", "()I")) failures.add("Aptitude.getLevel()");
        if (!hasMethodQuiet(aptitudeNode, "getLockedTexture", "(I)Lnet/minecraft/resources/ResourceLocation;")) failures.add("Aptitude.getLockedTexture(int)");

        if (!hasFieldQuiet(capabilityNode, "aptitudeLevel")) failures.add("AptitudeCapability.aptitudeLevel");
        if (!hasFieldQuiet(capabilityNode, "passiveLevel")) failures.add("AptitudeCapability.passiveLevel");
        if (!hasFieldQuiet(capabilityNode, "toggleSkill")) failures.add("AptitudeCapability.toggleSkill");
        if (!hasMethodQuiet(capabilityNode, "setToggleSkill", "(Lcom/seniors/justlevelingfork/registry/skills/Skill;Z)V")) failures.add("AptitudeCapability.setToggleSkill(Skill,boolean)");
        if (!hasMethodQuiet(capabilityNode, "serializeNBT", "()Lnet/minecraft/nbt/CompoundTag;")) failures.add("AptitudeCapability.serializeNBT()");
        if (!hasMethodQuiet(capabilityNode, "deserializeNBT", "(Lnet/minecraft/nbt/CompoundTag;)V")) failures.add("AptitudeCapability.deserializeNBT(CompoundTag)");

        if (!hasFieldQuiet(levelUpNode, "aptitude")) failures.add("AptitudeLevelUpSP.aptitude");
        if (!hasMethodQuiet(levelUpNode, "requiredPoints", "(I)I")) failures.add("AptitudeLevelUpSP.requiredPoints(int)");
        if (!hasMethodQuiet(levelUpNode, "requiredExperienceLevels", "(I)I")) failures.add("AptitudeLevelUpSP.requiredExperienceLevels(int)");

        if (!hasMethodQuiet(handlerNode, "ForceRefresh", "()V")) failures.add("HandlerAptitude.ForceRefresh()");

        if (!failures.isEmpty()) {
            String joined = String.join(", ", failures);
            throw new IllegalStateException("[JustLevellingAddonJS] Incompatible justlevelingfork runtime fingerprint. Expected base "
                    + EXPECTED_BASE_VERSION + " ABI. Missing members: " + joined);
        }

        LOGGER.info("[compat] Verified justlevelingfork ABI fingerprint for expected base {}", EXPECTED_BASE_VERSION);
        fingerprintValidated = true;
    }

    private static boolean hasMethod(ClassNode node, String methodName) {
        boolean found = node.methods.stream().anyMatch(method -> method.name.equals(methodName));
        return found || warnMissing(node, "method", methodName);
    }

    private static boolean hasMethodQuiet(ClassNode node, String methodName) {
        return node.methods.stream().anyMatch(method -> method.name.equals(methodName));
    }

    private static boolean hasMethodQuiet(ClassNode node, String methodName, String descriptor) {
        return node.methods.stream().anyMatch(method -> method.name.equals(methodName) && descriptor.equals(method.desc));
    }

    private static boolean hasFieldQuiet(ClassNode node, String fieldName) {
        return node.fields.stream().anyMatch(field -> field.name.equals(fieldName));
    }

    private static boolean hasField(ClassNode node, String fieldName) {
        boolean found = node.fields.stream().anyMatch(field -> field.name.equals(fieldName));
        return found || warnMissing(node, "field", fieldName);
    }

    private static boolean warnMissing(ClassNode node, String kind, String member) {
        LOGGER.warn("[compat] Missing {} '{}' in target '{}'. Skipping related mixin.", kind, member, node.name.replace('/', '.'));
        return false;
    }

    private static void warnUnknown(String mixinClassName, String targetClassName, String reason) {
        LOGGER.debug("[compat] Could not pre-validate mixin '{}' for target '{}' ({}). Applying defensively.", mixinClassName, targetClassName, reason);
    }

    private static boolean isOptionalThirdPartyMixin(String mixinSimple) {
        return switch (mixinSimple) {
            case "MixLegendaryTabsAddTabMirror" -> true;
            default -> false;
        };
    }
}
