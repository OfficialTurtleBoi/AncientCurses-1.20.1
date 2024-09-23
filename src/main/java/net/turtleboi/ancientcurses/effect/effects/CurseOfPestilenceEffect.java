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
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;

import java.util.ArrayList;
import java.util.List;

public class CurseOfPestilenceEffect extends MobEffect {
    public CurseOfPestilenceEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity.level().isClientSide) {
            if (pLivingEntity.tickCount % 20 == 0) {
                int effectColor = this.getColor();
                float red = ((effectColor >> 16) & 0xFF) / 255.0F;
                float green = ((effectColor >> 8) & 0xFF) / 255.0F;
                float blue = (effectColor & 0xFF) / 255.0F;
                for (int i = 0; i < 5; i++) {
                    pLivingEntity.level().addParticle(
                            ModParticleTypes.CURSED_PARTICLE.get(),
                            pLivingEntity.getX() + (pLivingEntity.getRandom().nextDouble() - 0.5) * pLivingEntity.getBbWidth(),
                            pLivingEntity.getY() + pLivingEntity.getRandom().nextDouble() * pLivingEntity.getBbHeight(),
                            pLivingEntity.getZ() + (pLivingEntity.getRandom().nextDouble() - 0.5) * pLivingEntity.getBbWidth(),
                            red, green, blue);
                }
            }
        }

        if (!pLivingEntity.level().isClientSide && pLivingEntity instanceof Player player) {
            int pestilenceCooldown = getPestilenceCooldown(player);
            int minTimeInterval = getMinTimeInterval(pAmplifier);
            int maxTimeInterval = getMaxTimeInterval(pAmplifier);
            if (pestilenceCooldown <= 0) {
                applyNewDebuff(player, pAmplifier);
                if (pAmplifier >= 1) {
                    upgradeExistingDebuff(player, pAmplifier);
                }
                setPestilenceCooldown(player, minTimeInterval + player.getRandom().nextInt(maxTimeInterval - minTimeInterval + 1));
            } else {
                setPestilenceCooldown(player,pestilenceCooldown - 1);
                //player.displayClientMessage(Component.literal("Pestilence cooldown: " + pestilenceCooldown), true); //debug code
            }

            if (pAmplifier >= 2) {
                applyDebuffsToNearbyMobs(player);
            }
        }
        super.applyEffectTick(pLivingEntity, pAmplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        if (pLivingEntity instanceof Player player) {
            removeCurseDebuffs(player);
            resetPestilenceCooldown(player);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int pAmplifier) {
        return true;
    }

    private static int getMinTimeInterval(int pAmplifier){
        int[] timeIntervalValues = {800, 400, 200};
        int index = Math.min(pAmplifier, timeIntervalValues.length - 1);
        return timeIntervalValues[index];
    }

    private static int getMaxTimeInterval(int pAmplifier){
        int[] timeIntervalValues = {1200, 600, 300};
        int index = Math.min(pAmplifier, timeIntervalValues.length - 1);
        return timeIntervalValues[index];
    }

    private void setPestilenceCooldown(Player player, int cooldown) {
        player.getPersistentData().putInt("pestilenceCooldown", cooldown);
    }

    private int getPestilenceCooldown(Player player) {
        return player.getPersistentData().getInt("pestilenceCooldown");
    }

    private void resetPestilenceCooldown(Player player){
        player.getPersistentData().remove("pestilenceCooldown");
    }

    private void applyNewDebuff(Player player, int amplifier) {
        MobEffectInstance[] possibleDebuffs = {
                new MobEffectInstance(MobEffects.WEAKNESS, Integer.MAX_VALUE, 0),
                new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, Integer.MAX_VALUE, 0),
                new MobEffectInstance(MobEffects.BLINDNESS, Integer.MAX_VALUE, 0),
                new MobEffectInstance(MobEffects.POISON, Integer.MAX_VALUE, 0),
                new MobEffectInstance(MobEffects.WITHER, Integer.MAX_VALUE, 0),
                new MobEffectInstance(MobEffects.HUNGER, Integer.MAX_VALUE, 0),
                new MobEffectInstance(MobEffects.UNLUCK, Integer.MAX_VALUE, 0)
        };

        MobEffectInstance curseEffect = player.getEffect(ModEffects.CURSE_OF_PESTILENCE.get());
        if (curseEffect == null) return;

        int curseDuration = curseEffect.getDuration();
        for (int i = 0; i < amplifier + 1 && i < possibleDebuffs.length; i++) {
            MobEffectInstance debuff = possibleDebuffs[i];
            if (player.getEffect(debuff.getEffect()) == null) {
                player.addEffect(new MobEffectInstance(
                        debuff.getEffect(),
                        curseDuration,
                        0
                ));
            }
        }
    }

    private void upgradeExistingDebuff(Player player, int amplifier) {
        List<MobEffectInstance> playerDebuffs = new ArrayList<>();
        MobEffectInstance[] possibleDebuffs = {
                new MobEffectInstance(MobEffects.WEAKNESS, Integer.MAX_VALUE, 0),
                new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, Integer.MAX_VALUE, 0),
                new MobEffectInstance(MobEffects.BLINDNESS, Integer.MAX_VALUE, 0),
                new MobEffectInstance(MobEffects.POISON, Integer.MAX_VALUE, 0),
                new MobEffectInstance(MobEffects.WITHER, Integer.MAX_VALUE, 0),
                new MobEffectInstance(MobEffects.HUNGER, Integer.MAX_VALUE, 0),
                new MobEffectInstance(MobEffects.UNLUCK, Integer.MAX_VALUE, 0)
        };

        for (MobEffectInstance debuff : possibleDebuffs) {
            MobEffectInstance currentEffect = player.getEffect(debuff.getEffect());
            if (currentEffect != null) {
                playerDebuffs.add(currentEffect);
            }
        }

        MobEffectInstance curseEffect = player.getEffect(ModEffects.CURSE_OF_PESTILENCE.get());
        if (curseEffect == null) return;

        int curseDuration = curseEffect.getDuration();
        if (!playerDebuffs.isEmpty()) {
            for (int i = 0; i < amplifier; i++) {
                int debuffIndex = player.getRandom().nextInt(playerDebuffs.size());
                MobEffectInstance selectedDebuff = playerDebuffs.get(debuffIndex);

                player.addEffect(new MobEffectInstance(
                        selectedDebuff.getEffect(),
                        curseDuration,
                        selectedDebuff.getAmplifier() + 1
                ));
            }
        }
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

    private void applyDebuffsToNearbyMobs(Player player) {
        Level level = player.level();
        List<LivingEntity> nearbyMobs = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(25.0));

        for (LivingEntity mob : nearbyMobs) {
            if (!(mob instanceof Monster) && mob != player) {
                applyMobDebuffs(mob, 200);
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
