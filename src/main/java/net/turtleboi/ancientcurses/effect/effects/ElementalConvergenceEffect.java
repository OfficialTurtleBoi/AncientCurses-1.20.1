package net.turtleboi.ancientcurses.effect.effects;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ElementalConvergenceEffect extends MobEffect {
    public ElementalConvergenceEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }
    private static final Random random = new Random();

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
            List<Entity> nearbyEntities = player.level().getEntitiesOfClass(Entity.class, player.getBoundingBox().inflate(4));

            for (Entity entity : nearbyEntities) {
                if (entity instanceof LivingEntity livingEntity && entity != player) {
                    MobCategory category = livingEntity.getType().getCategory();
                    if (!isAllowedCategory(category)) {
                        triggerPulse(player, livingEntity);
                        livingEntity.hurt(player.damageSources().magic(), 5.0F);
                        Vec3 direction = livingEntity.position().subtract(player.position()).normalize().multiply(1.0, 0.0, 1.0);
                        livingEntity.setDeltaMovement(direction.x, 0.5, direction.z);
                        livingEntity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 0, false, false));
                    }
                }
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int pAmplifier) {
        return true;
    }

    private void triggerPulse(Player player, LivingEntity livingEntity) {
        MobEffect selectedBuff = null;
        int buffAmplifier = 1;
        int buffDuration = 100;

        int buffChoice = random.nextInt(3);
        switch (buffChoice) {
            case 0:
                selectedBuff = MobEffects.REGENERATION;
                break;
            case 1:
                selectedBuff = MobEffects.DAMAGE_BOOST;
                break;
            case 2:
                selectedBuff = MobEffects.MOVEMENT_SPEED;
                break;
        }

        if (selectedBuff != null) {
            player.addEffect(new MobEffectInstance(selectedBuff, buffDuration, buffAmplifier, false, false));
        }

        int auraChoice = random.nextInt(3);
        switch (auraChoice) {
            case 0:
                applyFireAura(player, livingEntity);
                break;
            case 1:
                applyLightningAura(player, livingEntity);
                break;
            case 2:
                applyIceAura(player, livingEntity);
                break;
        }
    }

    private void applyFireAura(Player player, LivingEntity livingEntity) {
        Level level = player.level();
        if (level instanceof ServerLevel serverLevel){
            spawnCircularParticles(player, 4, ParticleTypes.FLAME);
            serverLevel.playSound(
                    null,
                    player.getX(),
                    player.getY() + 1.0,
                    player.getZ(),
                    SoundEvents.GHAST_SHOOT,
                    SoundSource.HOSTILE,
                    1.0f,
                    0.5f
            );
        }
        livingEntity.setSecondsOnFire(3);
    }

    private void applyLightningAura(Player player, LivingEntity livingEntity) {
        spawnCircularParticles(player, 4, ParticleTypes.ELECTRIC_SPARK);
        Level level = livingEntity.level();
        LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
        lightningBolt.setPos(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());

        level.addFreshEntity(lightningBolt);
    }

    private void applyIceAura(Player player, LivingEntity livingEntity) {
        Level level = player.level();
        if (level instanceof ServerLevel serverLevel){
            spawnCircularParticles(player, 4, ParticleTypes.SNOWFLAKE);
            serverLevel.playSound(
                    null,
                    player.getX(),
                    player.getY() + 1.0,
                    player.getZ(),
                    SoundEvents.PLAYER_HURT_FREEZE,
                    SoundSource.HOSTILE,
                    1.0f,
                    0.5f
            );
        }
        livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1, false, false));
        livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1, false, false));
    }

    public static void spawnCircularParticles(Player player, double radius, ParticleOptions particleType) {
        ServerLevel serverLevel = (ServerLevel) player.level();
        double centerX = player.getX();
        double centerY = player.getY() + 1.0;
        double centerZ = player.getZ();

        int numParticles = 108;
        double angleStep = 360.0 / numParticles;

        for (int i = 0; i < numParticles; i++) {
            double angle = Math.toRadians(i * angleStep);
            double xOffset = radius * Math.cos(angle);
            double zOffset = radius * Math.sin(angle);
            double x = centerX + xOffset;
            double z = centerZ + zOffset;
            double y = centerY;

            serverLevel.sendParticles(
                    particleType,
                    x, y, z,
                    1,
                    0.0, 0.0, 0.0,
                    0.0
            );
        }
    }

    private boolean isAllowedCategory(MobCategory category) {
        return ALLOWED_CATEGORIES.contains(category);
    }

    private static final Set<MobCategory> ALLOWED_CATEGORIES = EnumSet.of(
            MobCategory.AMBIENT,
            MobCategory.CREATURE,
            MobCategory.AXOLOTLS,
            MobCategory.UNDERGROUND_WATER_CREATURE,
            MobCategory.WATER_CREATURE
    );
}