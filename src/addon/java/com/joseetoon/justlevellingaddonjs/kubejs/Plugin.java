package com.joseetoon.justlevellingaddonjs.kubejs;

import com.seniors.justlevelingfork.client.core.Value;
import com.seniors.justlevelingfork.client.core.ValueType;
import com.joseetoon.justlevellingaddonjs.kubejs.compat.AptitudeAPI;
import com.joseetoon.justlevellingaddonjs.kubejs.compat.AbilityCreator;
import com.joseetoon.justlevellingaddonjs.kubejs.compat.LegacySkill;
import com.joseetoon.justlevellingaddonjs.kubejs.compat.PassiveCompat;
import com.joseetoon.justlevellingaddonjs.kubejs.compat.PassiveCreator;
import com.joseetoon.justlevellingaddonjs.kubejs.compat.SkillCompat;
import com.joseetoon.justlevellingaddonjs.kubejs.compat.SkillCreator;
import com.joseetoon.justlevellingaddonjs.kubejs.compat.TitleCompat;
import com.joseetoon.justlevellingaddonjs.kubejs.events.CustomEvents;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;

public class Plugin extends KubeJSPlugin {

    @Override
    public void registerBindings(BindingsEvent event) {
        super.registerBindings(event);
        event.add("ValueType", ValueType.class);
        event.add("Value", Value.class);
        event.add("Aptitude", AptitudeAPI.class);
        event.add("SkillCreator", SkillCreator.class);
        event.add("AbilityCreator", AbilityCreator.class);
        event.add("PassiveCreator", PassiveCreator.class);
        event.add("LegacySkill", LegacySkill.class);
        event.add("Skill", SkillCompat.class);
        event.add("Passive", PassiveCompat.class);
        event.add("Title", TitleCompat.class);
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

