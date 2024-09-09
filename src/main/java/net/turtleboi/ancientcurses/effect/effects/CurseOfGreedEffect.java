package net.turtleboi.ancientcurses.effect.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.turtleboi.ancientcurses.init.ModAttributes;
import net.turtleboi.ancientcurses.util.AttributeModifierUtil;

public class CurseOfGreedEffect extends MobEffect {
    public CurseOfGreedEffect(MobEffectCategory pCategory, int pColor) {
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

    public static double getItemDestroyChance(int pAmplifier) {
        double[] itemDestroyChanceValues = {0.0, 0.10, 0.25};
        int index = Math.min(pAmplifier, itemDestroyChanceValues.length - 1);
        return itemDestroyChanceValues[index];
    }
}