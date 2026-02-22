package com.joseetoon.justlevellingaddonjs.kubejs.compat;

import com.seniors.justlevelingfork.client.core.Value;
import com.seniors.justlevelingfork.registry.RegistrySkills;
import com.seniors.justlevelingfork.registry.skills.Skill;
import net.minecraft.resources.ResourceLocation;

public class AbilityCreator {
    public static SkillDraft createAbility(String name) {
        ResourceLocation id = AddonResourceUtils.parseResourceLocation(name, true);
        if (id == null) {
            throw new IllegalArgumentException("Invalid ability name: " + name);
        }
        return new SkillDraft(id);
    }

    public static SkillDraft createNewAbility(String id) {
        ResourceLocation resourceLocation = AddonResourceUtils.parseResourceLocation(id, false);
        if (resourceLocation == null) {
            throw new IllegalArgumentException("Invalid ability id: " + id);
        }
        return new SkillDraft(resourceLocation);
    }

    public static Skill createAbility(String name, String aptitudeName, int requiredLevel, String texture, Value... values) {
        ResourceLocation id = AddonResourceUtils.parseResourceLocation(name, true);
        if (id == null) {
            throw new IllegalArgumentException("Invalid ability name: " + name);
        }
        return SkillCompat.addWithId(id, aptitudeName, requiredLevel, AddonResourceUtils.normalizeTexture(texture), values);
    }

    public static Skill createAbility(String name, String aptitudeName, int requiredLevel, String texture, int spCost, Value... values) {
        Skill skill = createAbility(name, aptitudeName, requiredLevel, texture, values);
        SkillCompat.setPointCost(skill, spCost);
        return skill;
    }

    public static Skill createNewAbility(String id, String aptitudeName, int requiredLevel, String texture, Value... values) {
        ResourceLocation resourceLocation = AddonResourceUtils.parseResourceLocation(id, false);
        if (resourceLocation == null) {
            throw new IllegalArgumentException("Invalid ability id: " + id);
        }
        return SkillCompat.addWithId(resourceLocation, aptitudeName, requiredLevel, AddonResourceUtils.normalizeTexture(texture), values);
    }

    public static Skill createNewAbility(String id, String aptitudeName, int requiredLevel, String texture, int spCost, Value... values) {
        Skill skill = createNewAbility(id, aptitudeName, requiredLevel, texture, values);
        SkillCompat.setPointCost(skill, spCost);
        return skill;
    }

    public static Skill get(String nameOrId) {
        return RegistrySkills.getSkill(nameOrId);
    }
}
