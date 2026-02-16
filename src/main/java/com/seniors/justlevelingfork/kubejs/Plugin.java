package com.seniors.justlevelingfork.kubejs;

import com.seniors.justlevelingfork.client.core.Value;
import com.seniors.justlevelingfork.client.core.ValueType;
import com.seniors.justlevelingfork.kubejs.compat.AbilityCreator;
import com.seniors.justlevelingfork.kubejs.compat.LegacySkill;
import com.seniors.justlevelingfork.kubejs.compat.PassiveCreator;
import com.seniors.justlevelingfork.kubejs.compat.SkillCreator;
import com.seniors.justlevelingfork.kubejs.events.CustomEvents;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.seniors.justlevelingfork.registry.passive.Passive;
import com.seniors.justlevelingfork.registry.skills.Skill;
import com.seniors.justlevelingfork.registry.title.Title;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;

public class Plugin extends KubeJSPlugin {

    @Override
    public void registerBindings(BindingsEvent event) {
        super.registerBindings(event);
        event.add("ValueType", ValueType.class);
        event.add("Value", Value.class);
        event.add("Aptitude", Aptitude.class);
        event.add("SkillCreator", SkillCreator.class);
        event.add("AbilityCreator", AbilityCreator.class);
        event.add("PassiveCreator", PassiveCreator.class);
        event.add("LegacySkill", LegacySkill.class);
        event.add("Skill", Skill.class);
        event.add("Passive", Passive.class);
        event.add("Title", Title.class);
        event.add("TitleAPI", TitleAPI.class);
        event.add("TitleComparator", TitleAPI.Comparator.class);
        event.add("LockItemAPI", LockItemAPI.class);
        event.add("NBTLockAPI", NBTLockAPI.class);
        event.add("LevelLockAPI", LevelLockAPI.class);
        event.add("VisibilityLockAPI", VisibilityLockAPI.class);
        event.add("TransmutationAPI", TransmutationAPI.class);
        event.add("SkillChangeAPI", SkillChangeAPI.class);
        event.add("PlayerDataAPI", PlayerDataAPI.class);
    }

    @Override
    public void registerEvents() {
        CustomEvents.GROUP.register();
    }
}
