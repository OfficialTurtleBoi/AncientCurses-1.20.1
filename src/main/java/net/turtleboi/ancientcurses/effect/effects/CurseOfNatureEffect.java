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
            double hitChanceReduction = getSilverFishSpawnChance(pAmplifier);
            AttributeModifierUtil.applyTransientModifier(
                    player,
                    ModAttributes.SILVERFISH_SPAWN_CHANCE.get(),
                    "COGSilverFishSpawnChance",
                    hitChanceReduction,
                    AttributeModifier.Operation.ADDITION);
            if (player.level().isDay() && !player.level().isClientSide&&pAmplifier>=2) {
                float f = player.getLightLevelDependentMagicValue();
                BlockPos blockpos = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
                boolean flag = player.isInWaterRainOrBubble() || player.isInPowderSnow || player.wasInPowderSnow;
                if (f > 0.5F && player.getRandom().nextFloat() * 30.0F < (f - 0.4F) * 2.0F && !flag && player.level().canSeeSky(blockpos)) {
                    player.setSecondsOnFire(6);
                }
            }
        }
        super.applyEffectTick(pLivingEntity, pAmplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        if (pLivingEntity instanceof Player player) {
            AttributeModifierUtil.removeModifier(player, ModAttributes.SILVERFISH_SPAWN_CHANCE.get(), "COGSilverFishSpawnChance");
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int pAmplifier) {
        return true;
    }

    public double getSilverFishSpawnChance(int pAmplifier) {
        double[] silverfishspawnchancevalues = {0, 0.3, 0.6};
        int index = Math.min(pAmplifier, silverfishspawnchancevalues.length - 1);
        return silverfishspawnchancevalues[index];
    }
}