package com.joseetoon.justlevellingaddonjs.kubejs.compat;

import com.seniors.justlevelingfork.registry.passive.Passive;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public class PassiveDraft {
    private final ResourceLocation id;
    private String aptitudeName;
    private String texture;
    private String attributeId;
    private String attributeUUID;
    private Object attributeValue;
    private int[] levelsRequired = new int[0];
    private Integer pointCost = null;

    public PassiveDraft(ResourceLocation id) {
        if (id == null || id.getPath().isBlank()) {
            throw new IllegalArgumentException("Passive id cannot be empty");
        }
        this.id = id;
        this.texture = defaultTexture(id);
    }

    public PassiveDraft setAptitude(String aptitudeName) {
        this.aptitudeName = aptitudeName;
        return this;
    }

    public PassiveDraft setTexture(String texture) {
        this.texture = normalizeTexture(texture);
        return this;
    }

    public PassiveDraft setAttributeId(String attributeId) {
        this.attributeId = attributeId;
        return this;
    }

    public PassiveDraft setAttributeUUID(String uuid) {
        this.attributeUUID = uuid;
        return this;
    }

    public PassiveDraft setAttributeUuid(String uuid) {
        return setAttributeUUID(uuid);
    }

    public PassiveDraft setAttributeValue(Object value) {
        this.attributeValue = value;
        return this;
    }

    public PassiveDraft setLevelsRequired(int... levelsRequired) {
        this.levelsRequired = levelsRequired == null ? new int[0] : levelsRequired.clone();
        return this;
    }

    public PassiveDraft setPointCost(int spCost) {
        this.pointCost = Math.max(1, spCost);
        return this;
    }

    public PassiveDraft setSpCost(int spCost) {
        return setPointCost(spCost);
    }

    public Passive register() {
        String normalizedAptitude = normalizeAptitudeName(this.aptitudeName, this.id);
        if (AptitudeAPI.getByName(normalizedAptitude) == null) {
            throw new IllegalArgumentException("Aptitude name doesn't exist: " + this.aptitudeName);
        }

        if (this.attributeId == null || this.attributeId.isBlank()) {
            throw new IllegalArgumentException("Attribute id cannot be empty for passive: " + this.id);
        }
        if (this.attributeValue == null) {
            throw new IllegalArgumentException("Attribute value cannot be null for passive: " + this.id);
        }
        if (this.levelsRequired.length == 0) {
            throw new IllegalArgumentException("levelsRequired must contain at least one value for passive: " + this.id);
        }

        String idString = this.id.toString();
        Passive passive;
        if (this.attributeUUID == null || this.attributeUUID.isBlank()) {
            passive = PassiveCreator.createNewPassive(
                    idString,
                    normalizedAptitude,
                    this.texture,
                    this.attributeId,
                    this.attributeValue,
                    this.levelsRequired
            );
        } else {
            passive = PassiveCreator.createNewPassive(
                    idString,
                    normalizedAptitude,
                    this.texture,
                    this.attributeId,
                    this.attributeUUID,
                    this.attributeValue,
                    this.levelsRequired
            );
        }

        if (this.pointCost != null) {
            PassiveCompat.setPointCost(passive, this.pointCost);
        }
        return passive;
    }

    private static String normalizeAptitudeName(String aptitudeName, ResourceLocation forId) {
        if (aptitudeName == null || aptitudeName.isBlank()) {
            throw new IllegalArgumentException("Aptitude name cannot be empty for passive: " + forId);
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
