package net.turtleboi.ancientcurses.effect.effects;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;

import java.util.List;

public class CurseOfShadowsEffect extends MobEffect {
    public CurseOfShadowsEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int amplifier) {
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

        if (pLivingEntity instanceof Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, amplifier, false, false, false));
            Level level = player.level();

            if (player.level().random.nextFloat() < 0.02) {
                playAmbientCaveSound(level, player);
            }

            if (amplifier >= 1) {
                if (!player.level().isClientSide && player.level() instanceof ServerLevel serverLevel) {
                    if (serverLevel.getDayTime() > 18000 || serverLevel.getDayTime() < 13000) {
                        serverLevel.setDayTime(16500);
                    }
                }
            }

            if (amplifier >= 2) {
                applyInvisibilityToMobs(player, 25.0D);
            }
        }
        super.applyEffectTick(pLivingEntity, amplifier);
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

    private void playAmbientCaveSound(Level level, Player player) {
        BlockPos playerPos = player.blockPosition();
        level.playLocalSound(playerPos, SoundEvents.AMBIENT_CAVE.get(), SoundSource.AMBIENT, 1.0F, 1.0F, false);
    }

    private void applyInvisibilityToMobs(Player player, double radius) {
        Level level = player.level();
        List<Mob> nearbyMobs = level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(radius));

        for (Mob mob : nearbyMobs) {
            if (!(mob instanceof Creeper) && mob.getTarget() != player) {
                mob.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200));
            }
        }
    }
}