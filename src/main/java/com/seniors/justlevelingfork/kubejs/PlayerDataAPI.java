package com.seniors.justlevelingfork.kubejs;

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
        player.setCustomName(title.getDisplayNameComponentOrFallback());
        player.refreshDisplayName();
        player.refreshTabListName();
    }

    private static int resolveReachableAptitudeTarget(Player player, AptitudeCapability cap, Aptitude aptitude, int requestedLevel) {
        int current = cap.getAptitudeLevel(aptitude);
        int clamped = Math.max(1, Math.min(requestedLevel, aptitude.getLevelCap()));
        if (clamped <= current) {
            return clamped;
        }

        int reachable = current;
        for (int level = current + 1; level <= clamped; level++) {
            if (!LevelLockAPI.canReachLevel(player, aptitude.getName(), level)) {
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
        if (aptitude == null) {
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
        if (aptitude == null) {
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
        if (aptitude == null) {
            return;
        }

        int current = cap.getAptitudeLevel(aptitude);
        int target;
        if (levels > 0) {
            target = resolveReachableAptitudeTarget(player, cap, aptitude, current + levels);
        } else {
            target = Math.max(1, Math.min(current + levels, aptitude.getLevelCap()));
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
        return aptitude == null ? 0 : cap.getAptitudeSkillPointsAvailable(aptitude);
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
        return aptitude == null ? 0 : cap.getAptitudeSkillPointsSpent(aptitude);
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
        if (aptitude == null) {
            return false;
        }

        List<Passive> passives = aptitude.getPassives(aptitude);
        Map<String, Integer> passiveBefore = new HashMap<>();
        for (Passive passive : passives) {
            passiveBefore.put(passive.getName(), cap.getPassiveLevel(passive));
        }

        List<Skill> skills = aptitude.getSkills(aptitude);
        Map<String, Boolean> skillBefore = new HashMap<>();
        for (Skill skill : skills) {
            skillBefore.put(skill.getName(), cap.isSkillUnlocked(skill));
        }

        cap.respecAptitude(aptitude);

        for (Passive passive : passives) {
            int before = passiveBefore.getOrDefault(passive.getName(), 0);
            int after = cap.getPassiveLevel(passive);
            SkillChangeAPI.handlePassiveLevelChanged(player, passive, before, after);
        }

        for (Skill skill : skills) {
            boolean before = skillBefore.getOrDefault(skill.getName(), false);
            boolean after = cap.isSkillUnlocked(skill);
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
        return skill != null && cap.getToggleSkill(skill);
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
        return skill != null && cap.isSkillUnlocked(skill);
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
        if (skill == null) {
            return false;
        }

        boolean beforeUnlocked = cap.isSkillUnlocked(skill);
        boolean changed = !beforeUnlocked && cap.tryUnlockSkill(skill);
        boolean afterUnlocked = cap.isSkillUnlocked(skill);
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
        if (skill == null) {
            return;
        }

        boolean beforeUnlocked = cap.isSkillUnlocked(skill);
        cap.setSkillUnlocked(skill, unlocked);
        boolean afterUnlocked = cap.isSkillUnlocked(skill);
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
        if (skill == null) {
            return;
        }

        if (toggle) {
            int aptitudeLevel = cap.getAptitudeLevel(skill.aptitude);
            if (!cap.isSkillUnlocked(skill)
                    || !skill.aptitude.isEnabled()
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
        return passive == null ? 0 : cap.getPassiveLevel(passive);
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
        if (passive == null) {
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

        AptitudeCapability cap = AptitudeCapability.get(player);
        Title title = RegistryTitles.getTitle(titleName.toLowerCase(Locale.ROOT));
        return cap != null && title != null && cap.getLockTitle(title);
    }

    public static boolean setTitleUnlocked(Player player, String titleName, boolean unlocked) {
        if (player == null || titleName == null || titleName.isEmpty()) {
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
}
