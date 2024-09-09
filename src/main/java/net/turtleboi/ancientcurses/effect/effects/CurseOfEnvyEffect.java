package net.turtleboi.ancientcurses.effect.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;

public class CurseOfEnvyEffect extends MobEffect {
    public CurseOfEnvyEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (!pLivingEntity.level().isClientSide && pLivingEntity instanceof Player player) {

        }
        super.applyEffectTick(pLivingEntity, pAmplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        if (pLivingEntity instanceof Player player) {

        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int pAmplifier) {
        return true;
    }

    public static double getItemDropOnUseChance(int pAmplifier) {
        double[] silverfishSpawnChanceValues = {0.2, 0.3, 0.4};
        int index = Math.min(pAmplifier, silverfishSpawnChanceValues.length - 1);
        return silverfishSpawnChanceValues[index];
    }
    public static double getattackhealingpercentage(int pAmplifier) {
        double[] silverfishSpawnChanceValues = {0, 1, 1.5};
        int index = Math.min(pAmplifier, silverfishSpawnChanceValues.length - 1);
        return silverfishSpawnChanceValues[index];
    }
}