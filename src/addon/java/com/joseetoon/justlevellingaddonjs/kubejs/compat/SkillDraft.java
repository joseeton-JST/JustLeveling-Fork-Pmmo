package com.joseetoon.justlevellingaddonjs.kubejs.compat;

import com.seniors.justlevelingfork.client.core.Value;
import com.seniors.justlevelingfork.registry.skills.Skill;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SkillDraft {
    private final ResourceLocation id;
    private String aptitudeName;
    private int requiredLevel = 1;
    private String texture;
    private final List<Value> values = new ArrayList<>();
    private Integer pointCost = null;

    public SkillDraft(ResourceLocation id) {
        if (id == null || id.getPath().isBlank()) {
            throw new IllegalArgumentException("Skill id cannot be empty");
        }
        this.id = id;
        this.texture = defaultTexture(id);
    }

    public SkillDraft setAptitude(String aptitudeName) {
        this.aptitudeName = aptitudeName;
        return this;
    }

    public SkillDraft setRequiredLevel(int level) {
        this.requiredLevel = level;
        return this;
    }

    public SkillDraft setTexture(String texture) {
        this.texture = normalizeTexture(texture);
        return this;
    }

    public SkillDraft setValues(Value... values) {
        this.values.clear();
        if (values == null) {
            return this;
        }
        for (Value value : values) {
            if (value != null) {
                this.values.add(value);
            }
        }
        return this;
    }

    public SkillDraft addValue(Value value) {
        if (value != null) {
            this.values.add(value);
        }
        return this;
    }

    public SkillDraft setPointCost(int spCost) {
        this.pointCost = Math.max(1, spCost);
        return this;
    }

    public SkillDraft setSpCost(int spCost) {
        return setPointCost(spCost);
    }

    public Skill register() {
        String normalizedAptitude = normalizeAptitudeName(this.aptitudeName, this.id);
        if (AptitudeAPI.getByName(normalizedAptitude) == null) {
            throw new IllegalArgumentException("Aptitude name doesn't exist: " + this.aptitudeName);
        }

        Value[] configuredValues = this.values.toArray(new Value[0]);
        Skill skill = SkillCompat.addWithId(this.id, normalizedAptitude, this.requiredLevel, this.texture, configuredValues);

        if (this.pointCost != null) {
            SkillCompat.setPointCost(skill, this.pointCost);
        }
        return skill;
    }

    private static String normalizeAptitudeName(String aptitudeName, ResourceLocation forId) {
        if (aptitudeName == null || aptitudeName.isBlank()) {
            throw new IllegalArgumentException("Aptitude name cannot be empty for skill: " + forId);
        }
        return aptitudeName.toLowerCase(Locale.ROOT);
    }

    private static String normalizeTexture(String texture) {
        if (texture == null || texture.isBlank()) {
            throw new IllegalArgumentException("Texture location cannot be empty");
        }
        return texture.toLowerCase(Locale.ROOT);
    }

    private static String defaultTexture(ResourceLocation id) {
        return id.getNamespace() + ":textures/skills/" + id.getPath() + ".png";
    }
}
