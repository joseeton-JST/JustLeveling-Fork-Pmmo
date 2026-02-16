package com.seniors.justlevelingfork.kubejs;

import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.seniors.justlevelingfork.registry.passive.Passive;
import com.seniors.justlevelingfork.registry.skills.Skill;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SkillChangeAPI {
    private static final Map<String, List<String>> LEVEL_UP_COMMANDS = new HashMap<>();
    private static final Map<String, List<String>> SKILL_UNLOCK_COMMANDS = new HashMap<>();
    private static final Map<String, List<String>> SKILL_LOCK_COMMANDS = new HashMap<>();
    private static final Map<String, List<String>> PASSIVE_LEVEL_UP_COMMANDS = new HashMap<>();
    private static final Map<String, List<String>> PASSIVE_LEVEL_DOWN_COMMANDS = new HashMap<>();

    public static void addLevelUpCommands(String aptitude, int level, String... commands) {
        String normalizedAptitude = normalizePath(aptitude);
        if (normalizedAptitude.isEmpty() || level < 1 || commands == null || commands.length == 0) {
            return;
        }

        String key = normalizedAptitude + ":" + level;
        LEVEL_UP_COMMANDS.computeIfAbsent(key, k -> new ArrayList<>()).addAll(Arrays.asList(commands));
    }

    public static void addSkillUnlockCommands(String skill, String... commands) {
        addStateCommands(SKILL_UNLOCK_COMMANDS, normalizePath(skill), commands);
    }

    public static void addSkillLockCommands(String skill, String... commands) {
        addStateCommands(SKILL_LOCK_COMMANDS, normalizePath(skill), commands);
    }

    public static void addPassiveLevelUpCommands(String passive, int level, String... commands) {
        addLevelStateCommands(PASSIVE_LEVEL_UP_COMMANDS, normalizePath(passive), level, commands);
    }

    public static void addPassiveLevelDownCommands(String passive, int level, String... commands) {
        addLevelStateCommands(PASSIVE_LEVEL_DOWN_COMMANDS, normalizePath(passive), level, commands);
    }

    public static void clearLevelUpCommands() {
        LEVEL_UP_COMMANDS.clear();
    }

    public static void clearSkillUnlockCommands(String skill) {
        SKILL_UNLOCK_COMMANDS.remove(normalizePath(skill));
    }

    public static void clearSkillLockCommands(String skill) {
        SKILL_LOCK_COMMANDS.remove(normalizePath(skill));
    }

    public static void clearPassiveLevelUpCommands(String passive, int level) {
        PASSIVE_LEVEL_UP_COMMANDS.remove(levelKey(normalizePath(passive), level));
    }

    public static void clearPassiveLevelDownCommands(String passive, int level) {
        PASSIVE_LEVEL_DOWN_COMMANDS.remove(levelKey(normalizePath(passive), level));
    }

    public static void clearAllStateCommands() {
        SKILL_UNLOCK_COMMANDS.clear();
        SKILL_LOCK_COMMANDS.clear();
        PASSIVE_LEVEL_UP_COMMANDS.clear();
        PASSIVE_LEVEL_DOWN_COMMANDS.clear();
    }

    public static void handleLevelUp(Player player, Aptitude aptitude, int newLevel) {
        handleLevelUp(player, aptitude, Math.max(0, newLevel - 1), newLevel);
    }

    public static void handleLevelUp(Player player, Aptitude aptitude, int previousLevel, int newLevel) {
        if (!(player instanceof ServerPlayer serverPlayer) || aptitude == null || newLevel < 1) {
            return;
        }

        String key = aptitude.getName().toLowerCase(Locale.ROOT) + ":" + newLevel;
        List<String> commands = LEVEL_UP_COMMANDS.get(key);
        if (commands == null || commands.isEmpty()) {
            return;
        }

        Map<String, String> placeholders = basePlaceholders(player);
        placeholders.put("{aptitude}", aptitude.getName().toLowerCase(Locale.ROOT));
        placeholders.put("{previous_level}", Integer.toString(previousLevel));
        placeholders.put("{new_level}", Integer.toString(newLevel));
        executeCommands(serverPlayer, commands, placeholders);
    }

    public static void handleSkillUnlockStateChange(Player player, Skill skill, boolean previousUnlocked, boolean newUnlocked) {
        if (!(player instanceof ServerPlayer serverPlayer) || skill == null || previousUnlocked == newUnlocked) {
            return;
        }

        Map<String, String> placeholders = basePlaceholders(player);
        placeholders.put("{skill}", skill.getName().toLowerCase(Locale.ROOT));
        placeholders.put("{aptitude}", skill.aptitude.getName().toLowerCase(Locale.ROOT));
        placeholders.put("{previous_level}", previousUnlocked ? "1" : "0");
        placeholders.put("{new_level}", newUnlocked ? "1" : "0");

        String skillKey = skill.getName().toLowerCase(Locale.ROOT);
        List<String> commands = newUnlocked
                ? SKILL_UNLOCK_COMMANDS.getOrDefault(skillKey, Collections.emptyList())
                : SKILL_LOCK_COMMANDS.getOrDefault(skillKey, Collections.emptyList());
        executeCommands(serverPlayer, commands, placeholders);
    }

    public static void handlePassiveLevelChanged(Player player, Passive passive, int previousLevel, int newLevel) {
        if (!(player instanceof ServerPlayer serverPlayer) || passive == null || previousLevel == newLevel) {
            return;
        }

        String passiveKey = passive.getName().toLowerCase(Locale.ROOT);
        Map<String, String> placeholders = basePlaceholders(player);
        placeholders.put("{passive}", passiveKey);
        placeholders.put("{aptitude}", passive.aptitude.getName().toLowerCase(Locale.ROOT));

        if (newLevel > previousLevel) {
            int from = previousLevel;
            for (int level = previousLevel + 1; level <= newLevel; level++) {
                placeholders.put("{previous_level}", Integer.toString(from));
                placeholders.put("{new_level}", Integer.toString(level));
                executeCommands(serverPlayer, PASSIVE_LEVEL_UP_COMMANDS.getOrDefault(levelKey(passiveKey, level), Collections.emptyList()), placeholders);
                from = level;
            }
            return;
        }

        int from = previousLevel;
        for (int level = previousLevel - 1; level >= newLevel; level--) {
            placeholders.put("{previous_level}", Integer.toString(from));
            placeholders.put("{new_level}", Integer.toString(level));
            executeCommands(serverPlayer, PASSIVE_LEVEL_DOWN_COMMANDS.getOrDefault(levelKey(passiveKey, level), Collections.emptyList()), placeholders);
            from = level;
        }
    }

    private static void addStateCommands(Map<String, List<String>> stateMap, String normalizedPath, String... commands) {
        if (normalizedPath.isEmpty() || commands == null || commands.length == 0) {
            return;
        }
        stateMap.computeIfAbsent(normalizedPath, ignored -> new ArrayList<>()).addAll(Arrays.asList(commands));
    }

    private static void addLevelStateCommands(Map<String, List<String>> stateMap, String normalizedPath, int level, String... commands) {
        if (normalizedPath.isEmpty() || level < 1 || commands == null || commands.length == 0) {
            return;
        }
        stateMap.computeIfAbsent(levelKey(normalizedPath, level), ignored -> new ArrayList<>()).addAll(Arrays.asList(commands));
    }

    private static Map<String, String> basePlaceholders(Player player) {
        Map<String, String> placeholders = new LinkedHashMap<>();
        placeholders.put("{player}", player.getName().getString());
        return placeholders;
    }

    private static String normalizePath(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return "";
        }
        String normalized = rawValue.toLowerCase(Locale.ROOT).trim();
        int separator = normalized.indexOf(':');
        return separator >= 0 && separator + 1 < normalized.length()
                ? normalized.substring(separator + 1)
                : normalized;
    }

    private static String levelKey(String id, int level) {
        return id + ":" + Math.max(1, level);
    }

    private static void executeCommands(ServerPlayer serverPlayer, List<String> commands, Map<String, String> placeholders) {
        if (commands == null || commands.isEmpty()) {
            return;
        }

        MinecraftServer server = serverPlayer.getServer();
        if (server == null) {
            return;
        }

        for (String cmd : commands) {
            if (cmd == null || cmd.isBlank()) {
                continue;
            }

            String parsed = cmd;
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                parsed = parsed.replace(entry.getKey(), entry.getValue());
            }
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), parsed);
        }
    }
}
