package net.turtleboi.ancientcurses.effect.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.turtleboi.ancientcurses.util.AttributeModifierUtil;

public class FrenziedBlowsEffect extends MobEffect {
    public FrenziedBlowsEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (!pLivingEntity.level().isClientSide && pLivingEntity instanceof Player player) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_SPEED,
                    "FrenziedBlowsAttackSpeed",
                    0.1 * (pAmplifier + 1),
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.MOVEMENT_SPEED,
                    "FrenziedBlowsMovementSpeed",
                    0.1 * (pAmplifier + 1),
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
        super.applyEffectTick(pLivingEntity, pAmplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        if (pLivingEntity instanceof Player player) {
            AttributeModifierUtil.removeModifier(player, Attributes.ATTACK_SPEED, "FrenziedBlowsAttackSpeed");
            AttributeModifierUtil.removeModifier(player, Attributes.MOVEMENT_SPEED, "FrenziedBlowsMovementSpeed");
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int pAmplifier) {
        return true;
    }
}