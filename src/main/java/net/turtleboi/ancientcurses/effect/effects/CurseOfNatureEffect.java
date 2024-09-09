package net.turtleboi.ancientcurses.effect.effects;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.turtleboi.ancientcurses.init.ModAttributes;
import net.turtleboi.ancientcurses.util.AttributeModifierUtil;

public class CurseOfNatureEffect extends MobEffect {
    public CurseOfNatureEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (!pLivingEntity.level().isClientSide && pLivingEntity instanceof Player player) {
            if (pAmplifier >= 2) {
                if (player.level().isDay() && !player.level().isClientSide) {
                    float lightLevel = player.level().getMaxLocalRawBrightness(player.blockPosition());
                    BlockPos blockpos = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
                    boolean isWet = player.isInWaterRainOrBubble() || player.isInPowderSnow || player.wasInPowderSnow;
                    if (lightLevel > 0.5F && player.getRandom().nextFloat() * 30.0F < (lightLevel - 0.4F) * 2.0F && !isWet && player.level().canSeeSky(blockpos)) {
                        player.setSecondsOnFire(6);
                    }
                }
            }
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

    public static double getSilverFishSpawnChance(int pAmplifier) {
        double[] silverfishSpawnChanceValues = {0.25, 0.33, 0.5};
        int index = Math.min(pAmplifier, silverfishSpawnChanceValues.length - 1);
        return silverfishSpawnChanceValues[index];
    }
}