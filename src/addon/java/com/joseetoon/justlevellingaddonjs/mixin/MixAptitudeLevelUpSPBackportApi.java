package com.joseetoon.justlevellingaddonjs.mixin;

import com.joseetoon.justlevellingaddonjs.compat.AptitudeCompat;
import com.seniors.justlevelingfork.network.packet.common.AptitudeLevelUpSP;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = AptitudeLevelUpSP.class, remap = false)
public abstract class MixAptitudeLevelUpSPBackportApi {
    public static int requiredPoints(int aptitudeLevel, Aptitude aptitude) {
        if (aptitude == null) {
            return AptitudeLevelUpSP.requiredPoints(aptitudeLevel);
        }
        return AptitudeCompat.getLevelUpPointCost(aptitude, aptitudeLevel);
    }

    public static int requiredExperienceLevels(int aptitudeLevel, Aptitude aptitude) {
        if (aptitude == null) {
            return AptitudeLevelUpSP.requiredExperienceLevels(aptitudeLevel);
        }
        return AptitudeCompat.getLevelUpExperienceLevels(aptitude, aptitudeLevel);
    }
}
