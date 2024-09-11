package net.turtleboi.ancientcurses.effect.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Random;

public class CurseOfPestilenceEffect extends MobEffect {
    private static final Random random = new Random();
    public CurseOfPestilenceEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (!pLivingEntity.level().isClientSide && pLivingEntity instanceof Player player) {
            if (player.tickCount % getTimeInterval(pAmplifier) == 0) {
                applyRandomDebuff(player, pAmplifier);
                if (pAmplifier >= 2) {
                    applyDebuffsToNearbyMobs(player, 25.0D, 10 * 20);
                }
            }
        }
        super.applyEffectTick(pLivingEntity, pAmplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        if (pLivingEntity instanceof Player player) {
            removeCurseDebuffs(player);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int pAmplifier) {
        return true;
    }

    private static int getTimeInterval(int pAmplifier){
        int[] timeIntervalValues = {1200, 600, 300};
        int index = Math.min(pAmplifier, timeIntervalValues.length - 1);
        return timeIntervalValues[index];
    }

    private void applyRandomDebuff(Player player, int amplifier) {
        MobEffectInstance[] possibleDebuffs = {
                new MobEffectInstance(MobEffects.WEAKNESS, Integer.MAX_VALUE, 0),
                new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, Integer.MAX_VALUE, 0),
                new MobEffectInstance(MobEffects.BLINDNESS, Integer.MAX_VALUE, 0),
                new MobEffectInstance(MobEffects.POISON, Integer.MAX_VALUE, 0),
                new MobEffectInstance(MobEffects.WITHER, Integer.MAX_VALUE, 0),
                new MobEffectInstance(MobEffects.HUNGER, Integer.MAX_VALUE, 0),
                new MobEffectInstance(MobEffects.UNLUCK, Integer.MAX_VALUE, 0)
        };
        int debuffIndex = random.nextInt(possibleDebuffs.length);
        MobEffectInstance selectedDebuff = possibleDebuffs[debuffIndex];
        if (amplifier >= 1) {
            selectedDebuff = new MobEffectInstance(
                    selectedDebuff.getEffect(),
                    selectedDebuff.getDuration(),
                    selectedDebuff.getAmplifier() + 1
            );
        }
        player.addEffect(selectedDebuff);
    }

    private void removeCurseDebuffs(Player player) {
        MobEffect[] curseDebuffs = {
                MobEffects.WEAKNESS,
                MobEffects.MOVEMENT_SLOWDOWN,
                MobEffects.BLINDNESS,
                MobEffects.POISON,
                MobEffects.WITHER,
                MobEffects.HUNGER,
                MobEffects.UNLUCK
        };

        for (MobEffect debuff : curseDebuffs) {
            if (player.hasEffect(debuff)) {
                player.removeEffect(debuff);
            }
        }
    }

    private void applyDebuffsToNearbyMobs(Player player, double radius, int duration) {
        Level level = player.level();
        List<LivingEntity> nearbyMobs = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius));

        for (LivingEntity mob : nearbyMobs) {
            if (!(mob instanceof Monster) && mob != player) {
                applyMobDebuffs(mob, duration);
            }
        }
    }

    private void applyMobDebuffs(LivingEntity mob, int duration) {
        MobEffectInstance[] possibleDebuffs = {
                new MobEffectInstance(MobEffects.WEAKNESS, duration, 0),
                new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 0),
                new MobEffectInstance(MobEffects.BLINDNESS, duration, 0),
                new MobEffectInstance(MobEffects.POISON, duration, 0),
                new MobEffectInstance(MobEffects.WITHER, duration, 0),
                new MobEffectInstance(MobEffects.HUNGER, duration, 0),
                new MobEffectInstance(MobEffects.UNLUCK, duration, 0)
        };
        for (MobEffectInstance debuff : possibleDebuffs) {
            mob.addEffect(debuff);
        }
    }
}
