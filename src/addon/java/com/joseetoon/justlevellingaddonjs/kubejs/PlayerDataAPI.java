package com.joseetoon.justlevellingaddonjs.kubejs;

import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import com.seniors.justlevelingfork.network.packet.client.SyncAptitudeCapabilityCP;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.RegistryPassives;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import com.seniors.justlevelingfork.registry.RegistryTitles;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.seniors.justlevelingfork.registry.passive.Passive;
import com.seniors.justlevelingfork.registry.skills.Skill;
import com.seniors.justlevelingfork.registry.title.Title;
import com.joseetoon.justlevellingaddonjs.compat.AptitudeCompat;
import com.joseetoon.justlevellingaddonjs.compat.CapabilityCompat;
import com.joseetoon.justlevellingaddonjs.compat.base121.BackportRegistryState;
import com.joseetoon.justlevellingaddonjs.compat.base121.Base121Bridge;
import com.joseetoon.justlevellingaddonjs.compat.base121.TitleBlockState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PlayerDataAPI {
    private static void sync(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            SyncAptitudeCapabilityCP.send(serverPlayer);
        }
    }

    private static void updateDisplayedTitle(ServerPlayer player, Title title) {
        player.setCustomName(Base121Bridge.titleDisplayComponentOrFallback(title));
        player.refreshDisplayName();
        player.refreshTabListName();
    }

    private static int resolveReachableAptitudeTarget(Player player, AptitudeCapability cap, Aptitude aptitude, int requestedLevel) {
        int current = cap.getAptitudeLevel(aptitude);
        int clamped = Math.max(1, Math.min(requestedLevel, AptitudeCompat.getLevelCap(aptitude)));
        if (clamped <= current) {
            return clamped;
        }

        int reachable = current;
        for (int level = current + 1; level <= clamped; level++) {
            if (!LevelLockAPI.canReachLevel(player, AptitudeCompat.getName(aptitude), level)) {
                break;
            }
            reachable = level;
        }

        return reachable;
    }

    public static int getAptitudeLevel(Player player, String aptitudeName) {
        if (player == null) {
            return 0;
        }
        AptitudeCapability cap = AptitudeCapability.get(player);
        if (cap == null || aptitudeName == null || aptitudeName.isEmpty()) {
            return 0;
        }

        Aptitude aptitude = RegistryAptitudes.getAptitude(aptitudeName.toLowerCase(Locale.ROOT));
        if (aptitude == null || BackportRegistryState.isAptitudeDeleted(aptitude.getName())) {
            return 0;
        }
        return cap.getAptitudeLevel(aptitude);
    }

    public static void setAptitudeLevel(Player player, String aptitudeName, int level) {
        if (player == null) {
            return;
        }
        AptitudeCapability cap = AptitudeCapability.get(player);
        if (cap == null || aptitudeName == null || aptitudeName.isEmpty()) {
            return;
        }

        Aptitude aptitude = RegistryAptitudes.getAptitude(aptitudeName.toLowerCase(Locale.ROOT));
        if (aptitude == null || BackportRegistryState.isAptitudeDeleted(aptitude.getName())) {
            return;
        }

        int current = cap.getAptitudeLevel(aptitude);
        int target = resolveReachableAptitudeTarget(player, cap, aptitude, level);
        if (target == current) {
            return;
        }

        cap.setAptitudeLevel(aptitude, target);
        sync(player);
    }

    public static void addAptitudeLevel(Player player, String aptitudeName, int levels) {
        if (player == null) {
            return;
        }
        AptitudeCapability cap = AptitudeCapability.get(player);
        if (cap == null || aptitudeName == null || aptitudeName.isEmpty() || levels == 0) {
            return;
        }

        Aptitude aptitude = RegistryAptitudes.getAptitude(aptitudeName.toLowerCase(Locale.ROOT));
        if (aptitude == null || BackportRegistryState.isAptitudeDeleted(aptitude.getName())) {
            return;
        }

        int current = cap.getAptitudeLevel(aptitude);
        int target;
        if (levels > 0) {
            target = resolveReachableAptitudeTarget(player, cap, aptitude, current + levels);
        } else {
            target = Math.max(1, Math.min(current + levels, AptitudeCompat.getLevelCap(aptitude)));
        }

        if (target == current) {
            return;
        }

        cap.setAptitudeLevel(aptitude, target);
        sync(player);
    }

    public static int getGlobalLevel(Player player) {
        if (player == null) {
            return 0;
        }
        AptitudeCapability cap = AptitudeCapability.get(player);
        return cap == null ? 0 : cap.getGlobalLevel();
    }

    public static int getAptitudePoints(Player player, String aptitudeName) {
        if (player == null || aptitudeName == null || aptitudeName.isEmpty()) {
            return 0;
        }

        AptitudeCapability cap = AptitudeCapability.get(player);
        if (cap == null) {
            return 0;
        }

        Aptitude aptitude = RegistryAptitudes.getAptitude(aptitudeName.toLowerCase(Locale.ROOT));
        if (aptitude == null || BackportRegistryState.isAptitudeDeleted(aptitude.getName())) {
            return 0;
        }
        return CapabilityCompat.getAptitudeSkillPointsAvailable(cap, aptitude);
    }

    public static int getAptitudePointsSpent(Player player, String aptitudeName) {
        if (player == null || aptitudeName == null || aptitudeName.isEmpty()) {
            return 0;
        }

        AptitudeCapability cap = AptitudeCapability.get(player);
        if (cap == null) {
            return 0;
        }

        Aptitude aptitude = RegistryAptitudes.getAptitude(aptitudeName.toLowerCase(Locale.ROOT));
        if (aptitude == null || BackportRegistryState.isAptitudeDeleted(aptitude.getName())) {
            return 0;
        }
        return CapabilityCompat.getAptitudeSkillPointsSpent(cap, aptitude);
    }

    public static boolean respecAptitude(Player player, String aptitudeName) {
        if (player == null || aptitudeName == null || aptitudeName.isEmpty()) {
            return false;
        }

        AptitudeCapability cap = AptitudeCapability.get(player);
        if (cap == null) {
            return false;
        }

        Aptitude aptitude = RegistryAptitudes.getAptitude(aptitudeName.toLowerCase(Locale.ROOT));
        if (aptitude == null || BackportRegistryState.isAptitudeDeleted(aptitude.getName())) {
            return false;
        }

        List<Passive> passives = aptitude.getPassives(aptitude);
        if (passives == null) passives = java.util.Collections.emptyList();
        Map<String, Integer> passiveBefore = new HashMap<>();
        for (Passive passive : passives) {
            passiveBefore.put(passive.getName(), cap.getPassiveLevel(passive));
        }

        List<Skill> skills = aptitude.getSkills(aptitude);
        if (skills == null) skills = java.util.Collections.emptyList();
        Map<String, Boolean> skillBefore = new HashMap<>();
        for (Skill skill : skills) {
            skillBefore.put(skill.getName(), CapabilityCompat.isSkillUnlocked(cap, skill));
        }

        CapabilityCompat.respecAptitude(cap, aptitude);

        for (Passive passive : passives) {
            int before = passiveBefore.getOrDefault(passive.getName(), 0);
            int after = cap.getPassiveLevel(passive);
            SkillChangeAPI.handlePassiveLevelChanged(player, passive, before, after);
        }

        for (Skill skill : skills) {
            boolean before = skillBefore.getOrDefault(skill.getName(), false);
            boolean after = CapabilityCompat.isSkillUnlocked(cap, skill);
            SkillChangeAPI.handleSkillUnlockStateChange(player, skill, before, after);
        }

        sync(player);
        return true;
    }

    public static boolean getSkillToggle(Player player, String skillName) {
        if (player == null) {
            return false;
        }
        AptitudeCapability cap = AptitudeCapability.get(player);
        if (cap == null || skillName == null || skillName.isEmpty()) {
            return false;
        }

        Skill skill = RegistrySkills.getSkill(skillName.toLowerCase(Locale.ROOT));
        return skill != null
                && !BackportRegistryState.isSkillBlockedByDeletedAptitude(skill)
                && cap.getToggleSkill(skill);
    }

    public static boolean isSkillUnlocked(Player player, String skillName) {
        if (player == null) {
            return false;
        }
        AptitudeCapability cap = AptitudeCapability.get(player);
        if (cap == null || skillName == null || skillName.isEmpty()) {
            return false;
        }

        Skill skill = RegistrySkills.getSkill(skillName.toLowerCase(Locale.ROOT));
        return skill != null
                && !BackportRegistryState.isSkillBlockedByDeletedAptitude(skill)
                && CapabilityCompat.isSkillUnlocked(cap, skill);
    }

    public static boolean unlockSkill(Player player, String skillName) {
        if (player == null) {
            return false;
        }
        AptitudeCapability cap = AptitudeCapability.get(player);
        if (cap == null || skillName == null || skillName.isEmpty()) {
            return false;
        }

        Skill skill = RegistrySkills.getSkill(skillName.toLowerCase(Locale.ROOT));
        if (skill == null || BackportRegistryState.isSkillBlockedByDeletedAptitude(skill)) {
            return false;
        }

        boolean beforeUnlocked = CapabilityCompat.isSkillUnlocked(cap, skill);
        boolean changed = !beforeUnlocked && CapabilityCompat.tryUnlockSkill(cap, skill);
        boolean afterUnlocked = CapabilityCompat.isSkillUnlocked(cap, skill);
        SkillChangeAPI.handleSkillUnlockStateChange(player, skill, beforeUnlocked, afterUnlocked);
        if (changed) {
            sync(player);
        }
        return afterUnlocked;
    }

    public static void setSkillUnlocked(Player player, String skillName, boolean unlocked) {
        if (player == null) {
            return;
        }
        AptitudeCapability cap = AptitudeCapability.get(player);
        if (cap == null || skillName == null || skillName.isEmpty()) {
            return;
        }

        Skill skill = RegistrySkills.getSkill(skillName.toLowerCase(Locale.ROOT));
        if (skill == null || BackportRegistryState.isSkillBlockedByDeletedAptitude(skill)) {
            return;
        }

        boolean beforeUnlocked = CapabilityCompat.isSkillUnlocked(cap, skill);
        CapabilityCompat.setSkillUnlocked(cap, skill, unlocked);
        boolean afterUnlocked = CapabilityCompat.isSkillUnlocked(cap, skill);
        SkillChangeAPI.handleSkillUnlockStateChange(player, skill, beforeUnlocked, afterUnlocked);
        if (beforeUnlocked != afterUnlocked) {
            sync(player);
        }
    }

    public static void setSkillToggle(Player player, String skillName, boolean toggle) {
        if (player == null) {
            return;
        }
        AptitudeCapability cap = AptitudeCapability.get(player);
        if (cap == null || skillName == null || skillName.isEmpty()) {
            return;
        }

        Skill skill = RegistrySkills.getSkill(skillName.toLowerCase(Locale.ROOT));
        if (skill == null || BackportRegistryState.isSkillBlockedByDeletedAptitude(skill)) {
            return;
        }

        if (toggle) {
            int aptitudeLevel = cap.getAptitudeLevel(skill.aptitude);
            if (!CapabilityCompat.isSkillUnlocked(cap, skill)
                    || !AptitudeCompat.isEnabled(skill.aptitude)
                    || skill.getLvl() <= 0
                    || aptitudeLevel < skill.getLvl()) {
                toggle = false;
            }
        }

        cap.setToggleSkill(skill, toggle);
        sync(player);
    }

    public static int getPassiveLevel(Player player, String passiveName) {
        if (player == null) {
            return 0;
        }
        AptitudeCapability cap = AptitudeCapability.get(player);
        if (cap == null || passiveName == null || passiveName.isEmpty()) {
            return 0;
        }

        Passive passive = RegistryPassives.getPassive(passiveName.toLowerCase(Locale.ROOT));
        if (passive == null || BackportRegistryState.isPassiveBlockedByDeletedAptitude(passive)) {
            return 0;
        }
        return cap.getPassiveLevel(passive);
    }

    public static void setPassiveLevel(Player player, String passiveName, int level) {
        if (player == null) {
            return;
        }
        AptitudeCapability cap = AptitudeCapability.get(player);
        if (cap == null || passiveName == null || passiveName.isEmpty()) {
            return;
        }

        Passive passive = RegistryPassives.getPassive(passiveName.toLowerCase(Locale.ROOT));
        if (passive == null || BackportRegistryState.isPassiveBlockedByDeletedAptitude(passive)) {
            return;
        }

        int current = cap.getPassiveLevel(passive);
        int clamped = Math.max(0, Math.min(level, passive.getMaxLevel()));
        if (clamped > current) {
            cap.addPassiveLevel(passive, clamped - current);
        } else if (clamped < current) {
            cap.subPassiveLevel(passive, current - clamped);
        } else {
            return;
        }
        int after = cap.getPassiveLevel(passive);
        SkillChangeAPI.handlePassiveLevelChanged(player, passive, current, after);
        if (after != current) {
            sync(player);
        }
    }

    public static boolean hasTitleUnlocked(Player player, String titleName) {
        if (player == null || titleName == null || titleName.isEmpty()) {
            return false;
        }
        if (BackportRegistryState.isTitleDeleted(titleName)
                || BackportRegistryState.isTitleDisabledByAddonConfig(titleName)) {
            return false;
        }
        if (TitleBlockState.isBlocked(player, titleName)) {
            return false;
        }

        AptitudeCapability cap = AptitudeCapability.get(player);
        Title title = RegistryTitles.getTitle(titleName.toLowerCase(Locale.ROOT));
        return cap != null && title != null && cap.getLockTitle(title);
    }

    public static boolean setTitleUnlocked(Player player, String titleName, boolean unlocked) {
        if (player == null || titleName == null || titleName.isEmpty()) {
            return false;
        }
        if (BackportRegistryState.isTitleDeleted(titleName)
                || BackportRegistryState.isTitleDisabledByAddonConfig(titleName)) {
            return false;
        }
        if (unlocked && TitleBlockState.isBlocked(player, titleName)) {
            return false;
        }

        AptitudeCapability cap = AptitudeCapability.get(player);
        Title title = RegistryTitles.getTitle(titleName.toLowerCase(Locale.ROOT));
        if (cap == null || title == null) {
            return false;
        }

        boolean changed = cap.getLockTitle(title) != unlocked;
        cap.setUnlockTitle(title, unlocked);

        if (!unlocked) {
            Title currentTitle = RegistryTitles.getTitle(cap.getPlayerTitle());
            if (currentTitle != null && currentTitle.getName().equalsIgnoreCase(title.getName())) {
                clearPlayerTitle(player);
                return true;
            }
        }

        if (changed) {
            sync(player);
        }
        return true;
    }

    public static String getPlayerTitle(Player player) {
        if (player == null) {
            return "";
        }

        AptitudeCapability cap = AptitudeCapability.get(player);
        return cap == null ? "" : cap.getPlayerTitle();
    }

    public static boolean setPlayerTitle(Player player, String titleName) {
        if (player == null || titleName == null || titleName.isEmpty()) {
            return false;
        }
        if (BackportRegistryState.isTitleDeleted(titleName)
                || BackportRegistryState.isTitleDisabledByAddonConfig(titleName)) {
            return false;
        }
        if (TitleBlockState.isBlocked(player, titleName)) {
            return false;
        }

        AptitudeCapability cap = AptitudeCapability.get(player);
        Title title = RegistryTitles.getTitle(titleName.toLowerCase(Locale.ROOT));
        if (cap == null || title == null || !cap.getLockTitle(title)) {
            return false;
        }

        if (title.getName().equalsIgnoreCase(cap.getPlayerTitle())) {
            return true;
        }

        cap.setPlayerTitle(title);
        if (player instanceof ServerPlayer serverPlayer) {
            updateDisplayedTitle(serverPlayer, title);
        }
        sync(player);
        return true;
    }

    public static void clearPlayerTitle(Player player) {
        if (player == null) {
            return;
        }

        AptitudeCapability cap = AptitudeCapability.get(player);
        BackportRegistryState.ensureTitlelessPresent(RegistryTitles.TITLES_REGISTRY.get());
        Title titleless = RegistryTitles.getTitle("titleless");
        if (cap == null || titleless == null) {
            return;
        }

        if (!titleless.getName().equalsIgnoreCase(cap.getPlayerTitle())) {
            cap.setPlayerTitle(titleless);
            if (player instanceof ServerPlayer serverPlayer) {
                updateDisplayedTitle(serverPlayer, titleless);
            }
            sync(player);
        }
    }

    public static boolean blockTitle(Player player, String titleName) {
        if (player == null || titleName == null || titleName.isEmpty()) {
            return false;
        }

        AptitudeCapability cap = AptitudeCapability.get(player);
        if (cap == null) {
            return false;
        }

        String normalized = BackportRegistryState.normalizePath(titleName);
        if (normalized == null) {
            return false;
        }

        boolean changed = TitleBlockState.setBlocked(player, normalized, true);
        if (!changed) {
            return false;
        }

        boolean needsSync = false;
        Title title = RegistryTitles.getTitle(normalized);
        if (title != null && cap.getLockTitle(title)) {
            cap.setUnlockTitle(title, false);
            needsSync = true;
        }

        String currentTitle = BackportRegistryState.normalizePath(cap.getPlayerTitle());
        if (normalized.equals(currentTitle)) {
            clearPlayerTitle(player);
            return true;
        }

        if (needsSync) {
            sync(player);
        }
        return true;
    }

    public static boolean unblockTitle(Player player, String titleName) {
        if (player == null || titleName == null || titleName.isEmpty()) {
            return false;
        }

        String normalized = BackportRegistryState.normalizePath(titleName);
        if (normalized == null) {
            return false;
        }

        boolean changed = TitleBlockState.setBlocked(player, normalized, false);
        if (changed) {
            sync(player);
        }
        return changed;
    }

    public static boolean isTitleBlocked(Player player, String titleName) {
        return TitleBlockState.isBlocked(player, titleName);
    }

    public static List<String> getBlockedTitles(Player player) {
        return TitleBlockState.getBlockedTitles(player);
    }

    public static void clearBlockedTitles(Player player) {
        if (player == null) {
            return;
        }
        List<String> current = TitleBlockState.getBlockedTitles(player);
        if (current.isEmpty()) {
            return;
        }
        TitleBlockState.clearBlockedTitles(player);
        sync(player);
    }
}
