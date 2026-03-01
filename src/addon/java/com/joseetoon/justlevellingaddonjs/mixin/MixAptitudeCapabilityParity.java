package com.joseetoon.justlevellingaddonjs.mixin;

import com.joseetoon.justlevellingaddonjs.compat.AptitudeCompat;
import com.joseetoon.justlevellingaddonjs.compat.base121.BackportRegistryState;
import com.joseetoon.justlevellingaddonjs.kubejs.compat.SkillCompat;
import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.RegistryPassives;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.seniors.justlevelingfork.registry.passive.Passive;
import com.seniors.justlevelingfork.registry.skills.Skill;
import com.seniors.justlevelingfork.registry.title.Title;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = AptitudeCapability.class, remap = false)
public abstract class MixAptitudeCapabilityParity {
    @Unique
    private final Map<String, Integer> jlforkaddon$aptitudePointsSpent = new HashMap<>();
    @Unique
    private final Map<String, Boolean> jlforkaddon$unlockSkill = new HashMap<>();

    public int getAptitudeSkillPointsTotal(Aptitude aptitude) {
        if (aptitude == null || BackportRegistryState.isAptitudeDeleted(aptitude.getName())) {
            return 0;
        }
        AptitudeCapability self = (AptitudeCapability) (Object) this;
        int interval = Math.max(1, AptitudeCompat.getSkillPointInterval(aptitude));
        int level = Math.max(0, self.getAptitudeLevel(aptitude));
        return level / interval;
    }

    public int getAptitudeSkillPointsSpent(Aptitude aptitude) {
        if (aptitude == null || BackportRegistryState.isAptitudeDeleted(aptitude.getName())) {
            return 0;
        }
        String key = aptitude.getName();
        return Math.max(0, this.jlforkaddon$aptitudePointsSpent.getOrDefault(key, 0));
    }

    public int getAptitudeSkillPointsAvailable(Aptitude aptitude) {
        if (aptitude == null || BackportRegistryState.isAptitudeDeleted(aptitude.getName())) {
            return 0;
        }
        return Math.max(0, getAptitudeSkillPointsTotal(aptitude) - getAptitudeSkillPointsSpent(aptitude));
    }

    public boolean trySpendAptitudePoints(Aptitude aptitude, int amount) {
        if (aptitude == null || BackportRegistryState.isAptitudeDeleted(aptitude.getName())) {
            return false;
        }
        if (amount <= 0) {
            return true;
        }
        int available = getAptitudeSkillPointsAvailable(aptitude);
        if (available < amount) {
            return false;
        }
        this.jlforkaddon$aptitudePointsSpent.put(aptitude.getName(), getAptitudeSkillPointsSpent(aptitude) + amount);
        return true;
    }

    public void refundAptitudePoints(Aptitude aptitude, int amount) {
        if (aptitude == null || amount <= 0 || BackportRegistryState.isAptitudeDeleted(aptitude.getName())) {
            return;
        }
        String key = aptitude.getName();
        int current = getAptitudeSkillPointsSpent(aptitude);
        this.jlforkaddon$aptitudePointsSpent.put(key, Math.max(0, current - amount));
    }

    public boolean isSkillUnlocked(Skill skill) {
        if (skill == null || BackportRegistryState.isSkillBlockedByDeletedAptitude(skill)) {
            return false;
        }
        return this.jlforkaddon$unlockSkill.getOrDefault(skill.getName(), false);
    }

    public void setSkillUnlocked(Skill skill, boolean unlocked) {
        if (skill == null) {
            return;
        }
        if (BackportRegistryState.isSkillBlockedByDeletedAptitude(skill)) {
            unlocked = false;
        }

        AptitudeCapability self = (AptitudeCapability) (Object) this;
        this.jlforkaddon$unlockSkill.put(skill.getName(), unlocked);
        if (!unlocked) {
            self.setToggleSkill(skill, false);
        }
    }

    public boolean tryUnlockSkill(Skill skill) {
        if (skill == null || BackportRegistryState.isSkillBlockedByDeletedAptitude(skill)) {
            return false;
        }
        if (isSkillUnlocked(skill)) {
            return true;
        }

        AptitudeCapability self = (AptitudeCapability) (Object) this;
        if (!AptitudeCompat.isEnabled(skill.aptitude) || skill.requiredLevel <= 0) {
            return false;
        }
        if (self.getAptitudeLevel(skill.aptitude) < skill.requiredLevel) {
            return false;
        }
        if (!trySpendAptitudePoints(skill.aptitude, SkillCompat.getPointCost(skill))) {
            return false;
        }
        this.jlforkaddon$unlockSkill.put(skill.getName(), true);
        return true;
    }

    @Inject(method = "setToggleSkill", at = @At("HEAD"), cancellable = true, require = 0)
    private void jlforkaddon$setToggleSkillGuard(Skill skill, boolean toggle, CallbackInfo ci) {
        AptitudeCapability self = (AptitudeCapability) (Object) this;
        if (skill == null) {
            ci.cancel();
            return;
        }

        boolean canEnable = !BackportRegistryState.isSkillBlockedByDeletedAptitude(skill)
                && AptitudeCompat.isEnabled(skill.aptitude)
                && skill.requiredLevel > 0
                && self.getAptitudeLevel(skill.aptitude) >= skill.requiredLevel;
        self.toggleSkill.put(skill.getName(), toggle && canEnable);
        ci.cancel();
    }

    @Inject(method = "addPassiveLevel", at = @At("HEAD"), cancellable = true, require = 0)
    private void jlforkaddon$addPassiveLevelGuard(Passive passive, int addLvl, CallbackInfo ci) {
        AptitudeCapability self = (AptitudeCapability) (Object) this;
        if (passive == null || addLvl <= 0 || BackportRegistryState.isPassiveBlockedByDeletedAptitude(passive)) {
            ci.cancel();
            return;
        }

        int currentLevel = self.getPassiveLevel(passive);
        int targetLevel = Math.min(currentLevel + addLvl, passive.levelsRequired.length);
        int aptitudeLevel = self.getAptitudeLevel(passive.aptitude);

        while (currentLevel < targetLevel) {
            int requiredAptitudeLevel = passive.levelsRequired[currentLevel];
            if (aptitudeLevel < requiredAptitudeLevel) {
                break;
            }
            currentLevel++;
        }

        self.passiveLevel.put(passive.getName(), currentLevel);
        ci.cancel();
    }

    public void respecAptitude(Aptitude aptitude) {
        if (aptitude == null || BackportRegistryState.isAptitudeDeleted(aptitude.getName())) {
            return;
        }

        AptitudeCapability self = (AptitudeCapability) (Object) this;
        for (Passive passive : RegistryPassives.PASSIVES_REGISTRY.get().getValues()) {
            if (passive.aptitude == aptitude) {
                self.passiveLevel.put(passive.getName(), 0);
            }
        }

        for (Skill skill : RegistrySkills.SKILLS_REGISTRY.get().getValues()) {
            if (skill.aptitude == aptitude) {
                this.jlforkaddon$unlockSkill.put(skill.getName(), false);
                self.toggleSkill.put(skill.getName(), false);
            }
        }

        this.jlforkaddon$aptitudePointsSpent.put(aptitude.getName(), 0);
    }

    @Inject(method = "setAptitudeLevel", at = @At("TAIL"), require = 0)
    private void jlforkaddon$setAptitudeLevel(Aptitude aptitude, int lvl, CallbackInfo ci) {
        jlforkaddon$clampAptitudePointsSpent(aptitude);
    }

    @Inject(method = "addAptitudeLevel", at = @At("TAIL"), require = 0)
    private void jlforkaddon$addAptitudeLevel(Aptitude aptitude, int lvl, CallbackInfo ci) {
        jlforkaddon$clampAptitudePointsSpent(aptitude);
    }

    @Inject(method = "getAptitudeLevel(Lcom/seniors/justlevelingfork/registry/aptitude/Aptitude;)I", at = @At("HEAD"), cancellable = true, require = 0)
    private void jlforkaddon$getAptitudeLevelDeleted(Aptitude aptitude, CallbackInfoReturnable<Integer> cir) {
        if (aptitude == null) {
            cir.setReturnValue(0);
            return;
        }
        if (BackportRegistryState.isAptitudeDeleted(aptitude.getName())) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "getAptitudeLevel(Ljava/lang/String;)I", at = @At("HEAD"), cancellable = true, require = 0)
    private void jlforkaddon$getAptitudeLevelByNameDeleted(String aptitudeName, CallbackInfoReturnable<Integer> cir) {
        if (aptitudeName == null || aptitudeName.isEmpty()) {
            cir.setReturnValue(0);
            return;
        }
        if (BackportRegistryState.isAptitudeDeleted(aptitudeName)) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "getGlobalLevel", at = @At("HEAD"), cancellable = true, require = 0)
    private void jlforkaddon$getGlobalLevelDeletedAware(CallbackInfoReturnable<Integer> cir) {
        AptitudeCapability self = (AptitudeCapability) (Object) this;

        int totalWeight = 0;
        long weightedSum = 0;

        for (Aptitude aptitude : RegistryAptitudes.APTITUDES_REGISTRY.get().getValues()) {
            if (BackportRegistryState.isAptitudeDeleted(aptitude.getName())) {
                continue;
            }
            int weight = BackportRegistryState.getAptitudeGlobalLevelWeight(aptitude.getName());
            int level = self.aptitudeLevel.getOrDefault(aptitude.getName(), 0);
            totalWeight += weight;
            weightedSum += (long) level * weight;
        }

        int result = totalWeight > 0
                ? (int) Math.floor((double) weightedSum / totalWeight)
                : 0;
        cir.setReturnValue(result);
    }

    @Inject(method = "getToggleSkill", at = @At("HEAD"), cancellable = true, require = 0)
    private void jlforkaddon$getToggleSkillDeleted(Skill skill, CallbackInfoReturnable<Boolean> cir) {
        if (BackportRegistryState.isSkillBlockedByDeletedAptitude(skill)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isSkillUnlocked", at = @At("HEAD"), cancellable = true, require = 0)
    private void jlforkaddon$isSkillUnlockedDeleted(Skill skill, CallbackInfoReturnable<Boolean> cir) {
        if (BackportRegistryState.isSkillBlockedByDeletedAptitude(skill)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getPassiveLevel", at = @At("HEAD"), cancellable = true, require = 0)
    private void jlforkaddon$getPassiveLevelDeleted(Passive passive, CallbackInfoReturnable<Integer> cir) {
        if (BackportRegistryState.isPassiveBlockedByDeletedAptitude(passive)) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "serializeNBT()Lnet/minecraft/nbt/CompoundTag;", at = @At("RETURN"), cancellable = true, require = 0)
    private void jlforkaddon$serializeNBT(CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag nbt = cir.getReturnValue();
        if (nbt == null) {
            nbt = new CompoundTag();
        }

        nbt.putInt("justlevellingaddonjs:data_version", 1);
        for (Aptitude aptitude : RegistryAptitudes.APTITUDES_REGISTRY.get().getValues()) {
            int spent = BackportRegistryState.isAptitudeDeleted(aptitude.getName()) ? 0 : getAptitudeSkillPointsSpent(aptitude);
            nbt.putInt("justlevellingaddonjs:aptitude_spent." + aptitude.getName(), spent);
        }
        for (Skill skill : RegistrySkills.SKILLS_REGISTRY.get().getValues()) {
            boolean unlocked = !BackportRegistryState.isSkillBlockedByDeletedAptitude(skill) && isSkillUnlocked(skill);
            nbt.putBoolean("justlevellingaddonjs:skill_unlocked." + skill.getName(), unlocked);
        }
        cir.setReturnValue(nbt);
    }

    @Inject(method = "deserializeNBT(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"), require = 0)
    private void jlforkaddon$deserializeNBT(CompoundTag nbt, CallbackInfo ci) {
        if (nbt == null) {
            return;
        }

        for (Aptitude aptitude : RegistryAptitudes.APTITUDES_REGISTRY.get().getValues()) {
            if (BackportRegistryState.isAptitudeDeleted(aptitude.getName())) {
                this.jlforkaddon$aptitudePointsSpent.put(aptitude.getName(), 0);
                continue;
            }
            String spentKey = "justlevellingaddonjs:aptitude_spent." + aptitude.getName();
            if (nbt.contains(spentKey, Tag.TAG_INT)) {
                this.jlforkaddon$aptitudePointsSpent.put(aptitude.getName(), Math.max(0, nbt.getInt(spentKey)));
            } else {
                this.jlforkaddon$aptitudePointsSpent.putIfAbsent(aptitude.getName(), 0);
            }
            jlforkaddon$clampAptitudePointsSpent(aptitude);
        }

        for (Skill skill : RegistrySkills.SKILLS_REGISTRY.get().getValues()) {
            if (BackportRegistryState.isSkillBlockedByDeletedAptitude(skill)) {
                this.jlforkaddon$unlockSkill.put(skill.getName(), false);
                ((AptitudeCapability) (Object) this).toggleSkill.put(skill.getName(), false);
                continue;
            }
            String unlockedKey = "justlevellingaddonjs:skill_unlocked." + skill.getName();
            if (nbt.contains(unlockedKey, Tag.TAG_BYTE)) {
                this.jlforkaddon$unlockSkill.put(skill.getName(), nbt.getBoolean(unlockedKey));
            } else {
                this.jlforkaddon$unlockSkill.putIfAbsent(skill.getName(), false);
            }
            if (!this.jlforkaddon$unlockSkill.getOrDefault(skill.getName(), false)) {
                ((AptitudeCapability) (Object) this).toggleSkill.put(skill.getName(), false);
            }
        }

        AptitudeCapability self = (AptitudeCapability) (Object) this;
        for (Title title : RegistryTitles.TITLES_REGISTRY.get().getValues()) {
            String titleKey = "title." + title.getName();
            if (!nbt.contains(titleKey, Tag.TAG_BYTE)) {
                self.unlockTitle.put(title.getName(), title.Requirement);
            }
        }
        jlforkaddon$sanitizeDeletedState();
    }

    @Inject(method = "copyFrom", at = @At("TAIL"), require = 0)
    private void jlforkaddon$copyFrom(AptitudeCapability source, CallbackInfo ci) {
        if (source == null) {
            return;
        }

        try {
            var field = source.getClass().getDeclaredField("jlforkaddon$unlockSkill");
            field.setAccessible(true);
            Object unlockSkillField = field.get(source);
            if (unlockSkillField instanceof Map<?, ?> unlockMap) {
                this.jlforkaddon$unlockSkill.clear();
                for (Map.Entry<?, ?> entry : unlockMap.entrySet()) {
                    if (entry.getKey() instanceof String key && entry.getValue() instanceof Boolean value) {
                        this.jlforkaddon$unlockSkill.put(key, value);
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        try {
            var field = source.getClass().getDeclaredField("jlforkaddon$aptitudePointsSpent");
            field.setAccessible(true);
            Object spentField = field.get(source);
            if (spentField instanceof Map<?, ?> spentMap) {
                this.jlforkaddon$aptitudePointsSpent.clear();
                for (Map.Entry<?, ?> entry : spentMap.entrySet()) {
                    if (entry.getKey() instanceof String key && entry.getValue() instanceof Number value) {
                        this.jlforkaddon$aptitudePointsSpent.put(key, Math.max(0, value.intValue()));
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        jlforkaddon$sanitizeDeletedState();
    }

    @Unique
    private void jlforkaddon$clampAptitudePointsSpent(Aptitude aptitude) {
        if (aptitude == null) {
            return;
        }
        if (BackportRegistryState.isAptitudeDeleted(aptitude.getName())) {
            this.jlforkaddon$aptitudePointsSpent.put(aptitude.getName(), 0);
            return;
        }
        int maxSpend = getAptitudeSkillPointsTotal(aptitude);
        int current = getAptitudeSkillPointsSpent(aptitude);
        if (current > maxSpend) {
            this.jlforkaddon$aptitudePointsSpent.put(aptitude.getName(), maxSpend);
        }
    }

    @Unique
    private void jlforkaddon$sanitizeDeletedState() {
        AptitudeCapability self = (AptitudeCapability) (Object) this;
        for (Aptitude aptitude : RegistryAptitudes.APTITUDES_REGISTRY.get().getValues()) {
            if (!BackportRegistryState.isAptitudeDeleted(aptitude.getName())) {
                continue;
            }
            self.aptitudeLevel.put(aptitude.getName(), 0);
            this.jlforkaddon$aptitudePointsSpent.put(aptitude.getName(), 0);
        }

        for (Passive passive : RegistryPassives.PASSIVES_REGISTRY.get().getValues()) {
            if (BackportRegistryState.isPassiveBlockedByDeletedAptitude(passive)) {
                self.passiveLevel.put(passive.getName(), 0);
            }
        }

        for (Skill skill : RegistrySkills.SKILLS_REGISTRY.get().getValues()) {
            if (!BackportRegistryState.isSkillBlockedByDeletedAptitude(skill)) {
                continue;
            }
            this.jlforkaddon$unlockSkill.put(skill.getName(), false);
            self.toggleSkill.put(skill.getName(), false);
        }
    }
}
