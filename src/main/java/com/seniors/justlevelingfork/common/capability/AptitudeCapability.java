package com.seniors.justlevelingfork.common.capability;

import com.seniors.justlevelingfork.client.core.Aptitudes;
import com.seniors.justlevelingfork.client.gui.OverlayAptitudeGui;
import com.seniors.justlevelingfork.handler.HandlerAptitude;
import com.seniors.justlevelingfork.network.packet.client.AptitudeOverlayCP;
import com.seniors.justlevelingfork.registry.*;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.seniors.justlevelingfork.registry.passive.Passive;
import com.seniors.justlevelingfork.registry.skills.Skill;
import com.seniors.justlevelingfork.registry.title.Title;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AptitudeCapability implements INBTSerializable<CompoundTag> {
    public Map<String, Integer> aptitudeLevel = mapAptitudes();
    public Map<String, Integer> aptitudePointsSpent = mapAptitudePointsSpent();
    public Map<String, Integer> passiveLevel = mapPassive();
    public Map<String, Boolean> unlockSkill = mapUnlockedSkills();
    public Map<String, Boolean> toggleSkill = mapSkills();
    public Map<String, Boolean> unlockTitle = mapTitles();
    public String playerTitle = "titleless";
    public double betterCombatEntityRange = 0.0D;

    public int counterAttackTimer = 0;
    public boolean counterAttack = false;

    private Map<String, Integer> mapAptitudes() {
        Map<String, Integer> map = new HashMap<>();
        List<Aptitude> aptitudeList = RegistryAptitudes.APTITUDES_REGISTRY.get().getValues().stream().toList();
        for (Aptitude aptitude : aptitudeList) {
            map.put(aptitude.getName(), 1);
        }
        return map;
    }

    private Map<String, Integer> mapPassive() {
        Map<String, Integer> map = new HashMap<>();
        List<Passive> passiveList = RegistryPassives.PASSIVES_REGISTRY.get().getValues().stream().toList();
        for (Passive passive : passiveList) {
            map.put(passive.getName(), 0);
        }
        return map;
    }

    private Map<String, Integer> mapAptitudePointsSpent() {
        Map<String, Integer> map = new HashMap<>();
        List<Aptitude> aptitudeList = RegistryAptitudes.APTITUDES_REGISTRY.get().getValues().stream().toList();
        for (Aptitude aptitude : aptitudeList) {
            map.put(aptitude.getName(), 0);
        }
        return map;
    }

    private Map<String, Boolean> mapSkills() {
        Map<String, Boolean> map = new HashMap<>();
        List<Skill> skillList = RegistrySkills.SKILLS_REGISTRY.get().getValues().stream().toList();
        for (Skill skill : skillList) {
            map.put(skill.getName(), false);
        }
        return map;
    }

    private Map<String, Boolean> mapUnlockedSkills() {
        Map<String, Boolean> map = new HashMap<>();
        List<Skill> skillList = RegistrySkills.SKILLS_REGISTRY.get().getValues().stream().toList();
        for (Skill skill : skillList) {
            map.put(skill.getName(), false);
        }
        return map;
    }

    private Map<String, Boolean> mapTitles() {
        Map<String, Boolean> map = new HashMap<>();
        List<Title> titleList = RegistryTitles.TITLES_REGISTRY.get().getValues().stream().toList();
        for (Title title : titleList) {
            map.put(title.getName(), title.Requirement);
        }
        return map;
    }

    public boolean getCounterAttack() {
        return this.counterAttack;
    }

    public void setCounterAttack(boolean set) {
        this.counterAttack = set;
    }

    public int getCounterAttackTimer() {
        return this.counterAttackTimer;
    }

    public void setCounterAttackTimer(int timer) {
        this.counterAttackTimer = timer;
    }

    @Nullable
    public static AptitudeCapability get(Player player) {
        LazyOptional<AptitudeCapability> capability = player.getCapability(RegistryCapabilities.APTITUDE);
        if(capability.isPresent() && capability.resolve().isPresent()){
            return capability.resolve().get();
        }

        return null;
    }

    @Nullable
    public static AptitudeCapability get() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null ) {
            return null;
        }
        return player.getCapability(RegistryCapabilities.APTITUDE).orElseThrow(() -> new IllegalArgumentException("Player does not have Capabilities!"));
    }

    public int getAptitudeLevel(Aptitude aptitude) {
        if (aptitude == null) {
            return 0;
        }
        return this.aptitudeLevel.getOrDefault(aptitude.getName(), 1);
    }

    public int getAptitudeLevel(String aptitudeName) {
        if (aptitudeName == null || aptitudeName.isEmpty()) {
            return 0;
        }
        String normalized = aptitudeName.toLowerCase(Locale.ROOT);
        return this.aptitudeLevel.containsKey(normalized) ? this.aptitudeLevel.get(normalized) : 0;
    }

    public void setAptitudeLevel(Aptitude aptitude, int lvl) {
        if (aptitude == null) {
            return;
        }
        this.aptitudeLevel.put(aptitude.getName(), Math.max(1, Math.min(lvl, aptitude.getLevelCap())));
        clampAptitudePointsSpent(aptitude);
    }

    public int getGlobalLevel(){
        return this.aptitudeLevel.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void addAptitudeLevel(Aptitude aptitude, int addLvl) {
        if (aptitude == null) {
            return;
        }
        int currentLevel = this.aptitudeLevel.getOrDefault(aptitude.getName(), 1);
        int maxLevel = aptitude.getLevelCap();
        this.aptitudeLevel.put(aptitude.getName(), Math.max(1, Math.min(currentLevel + addLvl, maxLevel)));
        clampAptitudePointsSpent(aptitude);
    }

    public int getAptitudeSkillPointsTotal(Aptitude aptitude) {
        if (aptitude == null) {
            return 0;
        }
        int interval = Math.max(1, aptitude.getSkillPointInterval());
        int level = Math.max(0, getAptitudeLevel(aptitude));
        return level / interval;
    }

    public int getAptitudeSkillPointsSpent(Aptitude aptitude) {
        if (aptitude == null) {
            return 0;
        }
        return Math.max(0, this.aptitudePointsSpent.getOrDefault(aptitude.getName(), 0));
    }

    public int getAptitudeSkillPointsAvailable(Aptitude aptitude) {
        if (aptitude == null) {
            return 0;
        }
        return Math.max(0, getAptitudeSkillPointsTotal(aptitude) - getAptitudeSkillPointsSpent(aptitude));
    }

    public boolean trySpendAptitudePoints(Aptitude aptitude, int amount) {
        if (aptitude == null) {
            return false;
        }
        if (amount <= 0) {
            return true;
        }
        int available = getAptitudeSkillPointsAvailable(aptitude);
        if (available < amount) {
            return false;
        }
        this.aptitudePointsSpent.put(aptitude.getName(), getAptitudeSkillPointsSpent(aptitude) + amount);
        return true;
    }

    public void refundAptitudePoints(Aptitude aptitude, int amount) {
        if (aptitude == null || amount <= 0) {
            return;
        }
        int current = getAptitudeSkillPointsSpent(aptitude);
        this.aptitudePointsSpent.put(aptitude.getName(), Math.max(0, current - amount));
    }

    public void respecAptitude(Aptitude aptitude) {
        if (aptitude == null) {
            return;
        }

        for (Passive passive : RegistryPassives.PASSIVES_REGISTRY.get().getValues().stream().toList()) {
            if (passive.aptitude == aptitude) {
                this.passiveLevel.put(passive.getName(), 0);
            }
        }
        for (Skill skill : RegistrySkills.SKILLS_REGISTRY.get().getValues().stream().toList()) {
            if (skill.aptitude == aptitude) {
                this.unlockSkill.put(skill.getName(), false);
                this.toggleSkill.put(skill.getName(), false);
            }
        }
        this.aptitudePointsSpent.put(aptitude.getName(), 0);
    }

    public int getPassiveLevel(Passive passive) {
        if (passive == null) {
            return 0;
        }
        return this.passiveLevel.getOrDefault(passive.getName(), 0);
    }

    public void addPassiveLevel(Passive passive, int addLvl) {
        if (passive == null || addLvl <= 0) {
            return;
        }

        int currentLevel = getPassiveLevel(passive);
        int targetLevel = Math.min(currentLevel + addLvl, passive.levelsRequired.length);
        int aptitudeLevel = getAptitudeLevel(passive.aptitude);

        // Validate each passive level step against aptitude requirements.
        while (currentLevel < targetLevel) {
            int requiredAptitudeLevel = passive.levelsRequired[currentLevel];
            if (aptitudeLevel < requiredAptitudeLevel) {
                break;
            }
            currentLevel++;
        }

        this.passiveLevel.put(passive.getName(), currentLevel);
    }

    public void subPassiveLevel(Passive passive, int subLvl) {
        if (passive == null || subLvl <= 0) {
            return;
        }
        this.passiveLevel.put(passive.getName(), Math.max(getPassiveLevel(passive) - subLvl, 0));
    }

    public boolean getToggleSkill(Skill skill) {
        if (skill == null) {
            return false;
        }
        return this.toggleSkill.getOrDefault(skill.getName(), false);
    }

    public boolean isSkillUnlocked(Skill skill) {
        if (skill == null) {
            return false;
        }
        return this.unlockSkill.getOrDefault(skill.getName(), false);
    }

    public void setSkillUnlocked(Skill skill, boolean unlocked) {
        if (skill == null) {
            return;
        }
        this.unlockSkill.put(skill.getName(), unlocked);
        if (!unlocked) {
            this.toggleSkill.put(skill.getName(), false);
        }
    }

    public boolean tryUnlockSkill(Skill skill) {
        if (skill == null) {
            return false;
        }
        if (isSkillUnlocked(skill)) {
            return true;
        }
        if (!skill.aptitude.isEnabled() || skill.requiredLevel <= 0) {
            return false;
        }
        if (getAptitudeLevel(skill.aptitude) < skill.requiredLevel) {
            return false;
        }
        if (!trySpendAptitudePoints(skill.aptitude, skill.getPointCost())) {
            return false;
        }
        this.unlockSkill.put(skill.getName(), true);
        return true;
    }

    public void setToggleSkill(Skill skill, boolean toggle) {
        if (skill == null) {
            return;
        }

        boolean canEnable = skill.aptitude.isEnabled()
                && skill.requiredLevel > 0
                && getAptitudeLevel(skill.aptitude) >= skill.requiredLevel;

        this.toggleSkill.put(skill.getName(), toggle && canEnable);
    }

    public boolean getLockTitle(Title title) {
        if (title == null) {
            return false;
        }
        return this.unlockTitle.getOrDefault(title.getName(), title.Requirement);
    }

    public void setUnlockTitle(Title title, boolean requirement) {
        this.unlockTitle.put(title.getName(), requirement);
    }

    public String getPlayerTitle() {
        return this.playerTitle;
    }

    public void setPlayerTitle(Title title) {
        this.playerTitle = title.getName();
    }

    public boolean canUseItem(Player player, ItemStack item) {
        if (item == null || item.isEmpty()) {
            return true;
        }

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item.getItem());
        if (itemId == null) {
            return true;
        }

        List<Aptitudes> aptitude = HandlerAptitude.getValue(item);
        return canUse(player, itemId.toString(), aptitude);
    }

    public boolean canUseItem(Player player, ResourceLocation resourceLocation) {
        return canUse(player, resourceLocation);
    }

    // Required for locking PointBlank
    public boolean canUseItemClient(ItemStack item) {
        return canUseClient(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item.getItem())));
    }

    public boolean canUseSpecificID(Player player, String specificID){
        return canUse(player, specificID);
    }

    public boolean canUseBlock(Player player, Block block) {
        return canUse(player, Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)));
    }

    public boolean canUseEntity(Player player, Entity entity) {
        return canUse(player, Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType())));
    }

    private boolean canUse(Player player, ResourceLocation resource) {
        return canUse(player, resource.toString(), HandlerAptitude.getValue(resource.toString()));
    }

    // Required for locking PointBlank
    private boolean canUseClient(ResourceLocation resource) {
        List<Aptitudes> aptitude = HandlerAptitude.getValue(resource.toString());
        if (aptitude != null) {
            for (Aptitudes aptitudes : aptitude) {
                if (getAptitudeLevel(aptitudes.getAptitude()) < aptitudes.getAptitudeLvl()) {
                    OverlayAptitudeGui.showWarning(resource.toString());
                    return false;
                }
            }
        }
        return true;
    }

    private boolean canUse(Player player, String restrictionID) {
        return canUse(player, restrictionID, HandlerAptitude.getValue(restrictionID));
    }

    private boolean canUse(Player player, String restrictionID, List<Aptitudes> aptitude) {
        if (aptitude != null) {
            for (Aptitudes aptitudes : aptitude) {
                if (getAptitudeLevel(aptitudes.getAptitude()) < aptitudes.getAptitudeLvl()) {
                    if (player instanceof net.minecraft.server.level.ServerPlayer)
                        AptitudeOverlayCP.send(player, restrictionID);
                    return false;
                }
            }
        }
        return true;
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        for (Aptitude aptitude : RegistryAptitudes.APTITUDES_REGISTRY.get().getValues().stream().toList()){
            nbt.putInt("aptitude." + aptitude.getName(), this.aptitudeLevel.getOrDefault(aptitude.getName(), 1));
            nbt.putInt("aptitude_spent." + aptitude.getName(), this.aptitudePointsSpent.getOrDefault(aptitude.getName(), 0));
        }
        for (Passive passive : RegistryPassives.PASSIVES_REGISTRY.get().getValues().stream().toList()){
            nbt.putInt("passive." + passive.getName(), this.passiveLevel.getOrDefault(passive.getName(), 0));
        }
        for (Skill skill : RegistrySkills.SKILLS_REGISTRY.get().getValues().stream().toList()){
            nbt.putBoolean("skill_unlocked." + skill.getName(), this.unlockSkill.getOrDefault(skill.getName(), false));
            nbt.putBoolean("skill." + skill.getName(), this.toggleSkill.getOrDefault(skill.getName(), false));
        }
        for (Title title : RegistryTitles.TITLES_REGISTRY.get().getValues().stream().toList()){
            nbt.putBoolean("title." + title.getName(), this.unlockTitle.getOrDefault(title.getName(), title.Requirement));
        }
        nbt.putInt("counterAttackTimer", this.counterAttackTimer);
        nbt.putBoolean("counterAttack", this.counterAttack);
        nbt.putString("playerTitle", this.playerTitle);
        nbt.putDouble("betterCombatEntityRange", this.betterCombatEntityRange);
        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) {
        for (Aptitude aptitude : RegistryAptitudes.APTITUDES_REGISTRY.get().getValues().stream().toList()){
            String key = "aptitude." + aptitude.getName();
            if (nbt.contains(key, Tag.TAG_INT)) {
                this.aptitudeLevel.put(aptitude.getName(), Math.max(1, nbt.getInt(key)));
            }
            String spentKey = "aptitude_spent." + aptitude.getName();
            if (nbt.contains(spentKey, Tag.TAG_INT)) {
                this.aptitudePointsSpent.put(aptitude.getName(), Math.max(0, nbt.getInt(spentKey)));
            } else {
                this.aptitudePointsSpent.put(aptitude.getName(), 0);
            }
            clampAptitudePointsSpent(aptitude);
        }
        for (Passive passive : RegistryPassives.PASSIVES_REGISTRY.get().getValues().stream().toList()){
            String key = "passive." + passive.getName();
            if (nbt.contains(key, Tag.TAG_INT)) {
                this.passiveLevel.put(passive.getName(), Math.max(0, nbt.getInt(key)));
            }
        }
        for (Skill skill : RegistrySkills.SKILLS_REGISTRY.get().getValues().stream().toList()){
            String unlockedKey = "skill_unlocked." + skill.getName();
            if (nbt.contains(unlockedKey, Tag.TAG_BYTE)) {
                this.unlockSkill.put(skill.getName(), nbt.getBoolean(unlockedKey));
            } else {
                this.unlockSkill.put(skill.getName(), false);
            }
            String key = "skill." + skill.getName();
            if (nbt.contains(key, Tag.TAG_BYTE)) {
                this.toggleSkill.put(skill.getName(), nbt.getBoolean(key));
            }
            if (!this.unlockSkill.getOrDefault(skill.getName(), false)) {
                this.toggleSkill.put(skill.getName(), false);
            }
        }
        for (Title title : RegistryTitles.TITLES_REGISTRY.get().getValues().stream().toList()){
            String key = "title." + title.getName();
            if (nbt.contains(key, Tag.TAG_BYTE)) {
                this.unlockTitle.put(title.getName(), nbt.getBoolean(key));
            }
        }

        this.counterAttackTimer = nbt.getInt("counterAttackTimer");
        this.counterAttack = nbt.getBoolean("counterAttack");
        this.playerTitle = nbt.getString("playerTitle");
        this.betterCombatEntityRange = nbt.getDouble("betterCombatEntityRange");
    }

    public void copyFrom(AptitudeCapability source) {
        for (Aptitude aptitude : RegistryAptitudes.APTITUDES_REGISTRY.get().getValues().stream().toList()){
            this.aptitudeLevel.put(aptitude.getName(), source.aptitudeLevel.getOrDefault(aptitude.getName(), 1));
            this.aptitudePointsSpent.put(aptitude.getName(), source.aptitudePointsSpent.getOrDefault(aptitude.getName(), 0));
            clampAptitudePointsSpent(aptitude);
        }
        for (Passive passive : RegistryPassives.PASSIVES_REGISTRY.get().getValues().stream().toList()){
            this.passiveLevel.put(passive.getName(), source.passiveLevel.getOrDefault(passive.getName(), 0));
        }
        for (Skill skill : RegistrySkills.SKILLS_REGISTRY.get().getValues().stream().toList()){
            this.unlockSkill.put(skill.getName(), source.unlockSkill.getOrDefault(skill.getName(), false));
            this.toggleSkill.put(skill.getName(), source.toggleSkill.getOrDefault(skill.getName(), false));
            if (!this.unlockSkill.getOrDefault(skill.getName(), false)) {
                this.toggleSkill.put(skill.getName(), false);
            }
        }
        for (Title title : RegistryTitles.TITLES_REGISTRY.get().getValues().stream().toList()){
            this.unlockTitle.put(title.getName(), source.unlockTitle.getOrDefault(title.getName(), title.Requirement));
        }

        this.counterAttackTimer = source.counterAttackTimer;
        this.counterAttack = source.counterAttack;
        this.playerTitle = source.playerTitle;
        this.betterCombatEntityRange = source.betterCombatEntityRange;
    }

    private void clampAptitudePointsSpent(Aptitude aptitude) {
        if (aptitude == null) {
            return;
        }
        int maxSpend = getAptitudeSkillPointsTotal(aptitude);
        int current = getAptitudeSkillPointsSpent(aptitude);
        if (current > maxSpend) {
            this.aptitudePointsSpent.put(aptitude.getName(), maxSpend);
        }
    }
}


