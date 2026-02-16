package com.seniors.justlevelingfork.kubejs;

import com.seniors.justlevelingfork.registry.RegistrySkills;
import com.seniors.justlevelingfork.registry.skills.Skill;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TransmutationAPI {
    private static final String AGNOSTIC_KEY = "__agnostic__";
    private static final Map<String, List<TransmutationRule>> REAGENT_RULES = new LinkedHashMap<>();
    private static final List<TransmutationRule> AGNOSTIC_RULES = new ArrayList<>();
    private static boolean consumeReagent = true;

    public static void setConsumeReagent(boolean consume) {
        consumeReagent = consume;
    }

    public static boolean getConsumeReagent() {
        return consumeReagent;
    }

    public static void addEntryToReagent(String reagentItemId, String startState, String endState) {
        addEntryToReagent(reagentItemId, startState, endState, null);
    }

    public static void addEntryToReagent(String reagentItemId, String startState, String endState, String requiredSkill) {
        String normalizedReagent = normalizeResourceId(reagentItemId);
        if (normalizedReagent.isEmpty()) {
            return;
        }

        StateSpec startSpec = parseStateSpec(startState);
        StateSpec endSpec = parseStateSpec(endState);
        String normalizedSkill = normalizeNullableSkill(requiredSkill);

        REAGENT_RULES.computeIfAbsent(normalizedReagent, ignored -> new ArrayList<>())
                .add(new TransmutationRule(startSpec, endSpec, normalizedSkill));
    }

    public static void addEntryToReagentAgnostic(String startState, String endState) {
        addEntryToReagentAgnostic(startState, endState, null);
    }

    public static void addEntryToReagentAgnostic(String startState, String endState, String requiredSkill) {
        StateSpec startSpec = parseStateSpec(startState);
        StateSpec endSpec = parseStateSpec(endState);
        String normalizedSkill = normalizeNullableSkill(requiredSkill);
        AGNOSTIC_RULES.add(new TransmutationRule(startSpec, endSpec, normalizedSkill));
    }

    public static void removeStartStateFromReagent(String reagentItemId, String startState) {
        String normalizedReagent = normalizeResourceId(reagentItemId);
        if (normalizedReagent.isEmpty()) {
            return;
        }

        StateSpec startSpec = parseStateSpec(startState);
        List<TransmutationRule> rules = REAGENT_RULES.get(normalizedReagent);
        if (rules == null) {
            return;
        }

        rules.removeIf(rule -> rule.start().canonical().equals(startSpec.canonical()));
        if (rules.isEmpty()) {
            REAGENT_RULES.remove(normalizedReagent);
        }
    }

    public static void removeStartStateReagentAgnostic(String startState) {
        StateSpec startSpec = parseStateSpec(startState);
        AGNOSTIC_RULES.removeIf(rule -> rule.start().canonical().equals(startSpec.canonical()));
    }

    public static void removeEndStateFromReagent(String reagentItemId, String endState) {
        String normalizedReagent = normalizeResourceId(reagentItemId);
        if (normalizedReagent.isEmpty()) {
            return;
        }

        StateSpec endSpec = parseStateSpec(endState);
        List<TransmutationRule> rules = REAGENT_RULES.get(normalizedReagent);
        if (rules == null) {
            return;
        }

        rules.removeIf(rule -> rule.end().canonical().equals(endSpec.canonical()));
        if (rules.isEmpty()) {
            REAGENT_RULES.remove(normalizedReagent);
        }
    }

    public static void removeEndStateReagentAgnostic(String endState) {
        StateSpec endSpec = parseStateSpec(endState);
        AGNOSTIC_RULES.removeIf(rule -> rule.end().canonical().equals(endSpec.canonical()));
    }

    public static void clearMapOfReagent(String reagentItemId) {
        String normalizedReagent = normalizeResourceId(reagentItemId);
        if (normalizedReagent.isEmpty()) {
            return;
        }
        REAGENT_RULES.remove(normalizedReagent);
    }

    public static void clearReagentOfEntries(String reagentItemId) {
        clearMapOfReagent(reagentItemId);
    }

    public static void clearReagentMap() {
        REAGENT_RULES.clear();
        AGNOSTIC_RULES.clear();
    }

    public static Map<String, List<Map<String, String>>> getRulesSnapshot() {
        Map<String, List<Map<String, String>>> snapshot = new LinkedHashMap<>();

        REAGENT_RULES.forEach((reagentId, rules) -> {
            List<Map<String, String>> serializedRules = new ArrayList<>();
            for (TransmutationRule rule : rules) {
                serializedRules.add(rule.toSnapshotMap());
            }
            snapshot.put(reagentId, Collections.unmodifiableList(serializedRules));
        });

        List<Map<String, String>> agnosticSerializedRules = new ArrayList<>();
        for (TransmutationRule rule : AGNOSTIC_RULES) {
            agnosticSerializedRules.add(rule.toSnapshotMap());
        }
        snapshot.put(AGNOSTIC_KEY, Collections.unmodifiableList(agnosticSerializedRules));

        return Collections.unmodifiableMap(snapshot);
    }

    public static boolean tryTransmute(Player player, Level level, BlockPos pos, ItemStack reagentStack) {
        if (level == null || level.isClientSide() || pos == null || reagentStack == null || reagentStack.isEmpty()) {
            return false;
        }

        ResourceLocation reagentKey = ForgeRegistries.ITEMS.getKey(reagentStack.getItem());
        if (reagentKey == null) {
            return false;
        }

        String reagentId = normalizeResourceId(reagentKey.toString());
        BlockState currentState = level.getBlockState(pos);
        List<TransmutationRule> candidates = new ArrayList<>();
        List<TransmutationRule> reagentRules = REAGENT_RULES.get(reagentId);
        if (reagentRules != null) {
            candidates.addAll(reagentRules);
        }
        candidates.addAll(AGNOSTIC_RULES);

        for (TransmutationRule rule : candidates) {
            if (!rule.matches(currentState, player)) {
                continue;
            }

            BlockState nextState = rule.end().toBlockState();
            if (nextState == null || nextState.equals(currentState)) {
                continue;
            }

            boolean changed = level.setBlock(pos, nextState, 3);
            if (!changed) {
                continue;
            }

            if (consumeReagent && player != null && !player.isCreative()) {
                reagentStack.shrink(1);
            }
            return true;
        }

        return false;
    }

    private static String normalizeNullableSkill(String requiredSkill) {
        String normalized = RequirementParser.normalizeId(requiredSkill);
        return normalized.isEmpty() ? null : normalized;
    }

    private static String normalizeResourceId(String value) {
        String normalized = RequirementParser.normalizeId(value);
        if (normalized.isEmpty()) {
            return "";
        }
        ResourceLocation parsed = ResourceLocation.tryParse(normalized);
        return parsed == null ? "" : parsed.toString();
    }

    private static StateSpec parseStateSpec(String value) {
        String normalized = RequirementParser.normalizeId(value);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("State cannot be empty");
        }

        String blockPart = normalized;
        String propertiesPart = null;
        int bracketStart = normalized.indexOf('[');
        if (bracketStart >= 0) {
            if (!normalized.endsWith("]")) {
                throw new IllegalArgumentException("Invalid state format: " + value);
            }
            blockPart = normalized.substring(0, bracketStart).trim();
            propertiesPart = normalized.substring(bracketStart + 1, normalized.length() - 1).trim();
        }

        ResourceLocation blockId = ResourceLocation.tryParse(blockPart);
        if (blockId == null) {
            throw new IllegalArgumentException("Invalid block id: " + value);
        }

        Block block = ForgeRegistries.BLOCKS.getValue(blockId);
        if (block == null) {
            throw new IllegalArgumentException("Unknown block id: " + value);
        }

        Map<String, String> properties = new LinkedHashMap<>();
        if (propertiesPart != null && !propertiesPart.isBlank()) {
            String[] pairs = propertiesPart.split(",");
            for (String pair : pairs) {
                if (pair == null || pair.isBlank()) {
                    continue;
                }

                String[] split = pair.split("=", 2);
                if (split.length != 2) {
                    throw new IllegalArgumentException("Invalid blockstate property: " + pair);
                }

                String propertyName = split[0].trim().toLowerCase(Locale.ROOT);
                String propertyValue = split[1].trim().toLowerCase(Locale.ROOT);
                if (propertyName.isEmpty() || propertyValue.isEmpty()) {
                    throw new IllegalArgumentException("Invalid blockstate property: " + pair);
                }
                properties.put(propertyName, propertyValue);
            }
        }

        validateProperties(block.defaultBlockState(), properties, value);
        String canonical = canonicalizeState(blockId, properties);
        return new StateSpec(blockId, properties, canonical);
    }

    private static void validateProperties(BlockState baseState, Map<String, String> properties, String rawInput) {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            Property<?> property = baseState.getBlock().getStateDefinition().getProperty(entry.getKey());
            if (property == null) {
                throw new IllegalArgumentException("Unknown property '" + entry.getKey() + "' for state: " + rawInput);
            }

            Comparable<?> parsedValue = parsePropertyValue(property, entry.getValue());
            if (parsedValue == null) {
                throw new IllegalArgumentException("Invalid value '" + entry.getValue() + "' for property '" + entry.getKey() + "'");
            }
        }
    }

    private static String canonicalizeState(ResourceLocation blockId, Map<String, String> properties) {
        if (properties.isEmpty()) {
            return blockId.toString();
        }

        List<String> keys = new ArrayList<>(properties.keySet());
        Collections.sort(keys);
        StringBuilder builder = new StringBuilder(blockId.toString()).append('[');
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            if (i > 0) {
                builder.append(',');
            }
            builder.append(key).append('=').append(properties.get(key));
        }
        builder.append(']');
        return builder.toString();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Comparable<?> parsePropertyValue(Property property, String rawValue) {
        return (Comparable<?>) property.getValue(rawValue).orElse(null);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static String propertyValueName(Property property, Comparable<?> value) {
        return property.getName(value).toLowerCase(Locale.ROOT);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static BlockState setPropertyValue(BlockState state, Property<?> property, Comparable<?> value) {
        return setPropertyValueUnchecked(state, (Property) property, (Comparable) value);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static BlockState setPropertyValueUnchecked(BlockState state, Property property, Comparable value) {
        return state.setValue(property, value);
    }

    private record StateSpec(
            ResourceLocation blockId,
            Map<String, String> properties,
            String canonical
    ) {
        boolean matches(BlockState state) {
            ResourceLocation stateBlockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
            if (stateBlockId == null || !stateBlockId.equals(blockId)) {
                return false;
            }

            for (Map.Entry<String, String> entry : properties.entrySet()) {
                Property<?> property = state.getBlock().getStateDefinition().getProperty(entry.getKey());
                if (property == null) {
                    return false;
                }

                Comparable<?> currentValue = state.getValue(property);
                if (currentValue == null) {
                    return false;
                }

                String currentName = propertyValueName(property, currentValue);
                if (!entry.getValue().equalsIgnoreCase(currentName)) {
                    return false;
                }
            }

            return true;
        }

        BlockState toBlockState() {
            Block block = ForgeRegistries.BLOCKS.getValue(blockId);
            if (block == null) {
                return null;
            }

            BlockState state = block.defaultBlockState();
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                Property<?> property = state.getBlock().getStateDefinition().getProperty(entry.getKey());
                if (property == null) {
                    return null;
                }

                Comparable<?> parsedValue = parsePropertyValue(property, entry.getValue());
                if (parsedValue == null) {
                    return null;
                }
                state = setPropertyValue(state, property, parsedValue);
            }
            return state;
        }
    }

    private record TransmutationRule(
            StateSpec start,
            StateSpec end,
            String requiredSkill
    ) {
        boolean matches(BlockState state, Player player) {
            if (!start.matches(state)) {
                return false;
            }

            if (requiredSkill == null || requiredSkill.isBlank()) {
                return true;
            }

            if (player == null) {
                return false;
            }

            Skill skill = RegistrySkills.getSkill(requiredSkill);
            return skill != null && skill.isEnabled(player);
        }

        Map<String, String> toSnapshotMap() {
            Map<String, String> data = new LinkedHashMap<>();
            data.put("startState", start.canonical());
            data.put("endState", end.canonical());
            if (requiredSkill != null && !requiredSkill.isBlank()) {
                data.put("requiredSkill", requiredSkill);
            }
            return Collections.unmodifiableMap(data);
        }
    }
}
