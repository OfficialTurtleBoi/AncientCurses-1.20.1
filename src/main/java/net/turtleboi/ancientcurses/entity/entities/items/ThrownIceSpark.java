package net.turtleboi.ancientcurses.entity.entities.items;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.turtleboi.ancientcurses.entity.ModEntities;
import net.turtleboi.ancientcurses.item.ModItems;
import net.turtleboi.turtlecore.effect.CoreEffects;
import net.turtleboi.turtlecore.util.EffectApplicationUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ThrownIceSpark extends ThrowableItemProjectile {
    private static final String DEPLOYED_TAG = "Deployed";
    private static final String DEPLOYED_TICKS_TAG = "DeployedTicks";
    private static final String BASE_Y_TAG = "BaseY";
    private static final String HOVER_Y_TAG = "HoverY";
    private static final int AURA_DURATION_TICKS = 200;
    private static final int AURA_INTERVAL_TICKS = 20;
    private static final int CHILLED_TICKS = 60;
    private static final int FROZEN_CHILL_STACK = 4;
    private static final int FROZEN_RENEW_THRESHOLD_TICKS = 20;
    private static final int FROZEN_RENEW_TICKS = 40;
    private static final double AURA_RADIUS = 5.0D;
    private static final double HOVER_HEIGHT = 3.0D;
    private static final double RISE_DECAY = 0.18D;
    private static final double BOB_AMPLITUDE = 0.12D;
    private static final double BOB_SPEED = 0.12D;
    private boolean deployed;
    private int deployedTicks;
    private double baseY;
    private double hoverY;

    public ThrownIceSpark(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownIceSpark(Level level, LivingEntity shooter) {
        super(ModEntities.ICE_SPARK.get(), shooter, level);
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return ModItems.ICE_SPARK.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        deployAt(result.getEntity().getX(), result.getEntity().getY(), result.getEntity().getZ());
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        deployAt(result.getLocation().x, result.getLocation().y, result.getLocation().z);
    }

    @Override
    public void tick() {
        if (!deployed) {
            super.tick();
            return;
        }

        setDeltaMovement(0.0D, 0.0D, 0.0D);
        setNoGravity(true);
        deployedTicks++;
        setPos(getX(), getHoveringY(), getZ());
        if (level().isClientSide()) {
            return;
        }

        if (deployedTicks % AURA_INTERVAL_TICKS == 0) {
            pulseAura();
        }

        if (deployedTicks >= AURA_DURATION_TICKS) {
            discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean(DEPLOYED_TAG, deployed);
        tag.putInt(DEPLOYED_TICKS_TAG, deployedTicks);
        tag.putDouble(BASE_Y_TAG, baseY);
        tag.putDouble(HOVER_Y_TAG, hoverY);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        deployed = tag.getBoolean(DEPLOYED_TAG);
        deployedTicks = tag.getInt(DEPLOYED_TICKS_TAG);
        baseY = tag.getDouble(BASE_Y_TAG);
        hoverY = tag.getDouble(HOVER_Y_TAG);
        if (deployed) {
            setNoGravity(true);
        }
    }

    private void deployAt(double x, double y, double z) {
        if (deployed) {
            return;
        }

        deployed = true;
        deployedTicks = 0;
        baseY = y;
        hoverY = y + HOVER_HEIGHT;
        setPos(x, y, z);
        setDeltaMovement(0.0D, 0.0D, 0.0D);
        setNoGravity(true);
        if (!level().isClientSide()) {
            level().playSound(null, blockPosition(), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.9F, 1.5F);
        }
    }

    private void pulseAura() {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, getX(), getY(), getZ(),
                    28, AURA_RADIUS * 0.45D, 1.2D, AURA_RADIUS * 0.45D, 0.04D);
        }

        List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class,
                getBoundingBox().inflate(AURA_RADIUS),
                target -> target.isAlive() && target != getOwner());

        for (LivingEntity target : targets) {
            MobEffectInstance frozen = target.getEffect(CoreEffects.FROZEN.get());
            if (frozen != null) {
                if (frozen.getDuration() <= FROZEN_RENEW_THRESHOLD_TICKS) {
                    target.addEffect(new MobEffectInstance(CoreEffects.FROZEN.get(), FROZEN_RENEW_TICKS,
                            frozen.getAmplifier(), false, true));
                }
                continue;
            }

            MobEffectInstance chilled = target.getEffect(CoreEffects.CHILLED.get());
            int chilledStack = chilled == null ? 0 : Math.min(FROZEN_CHILL_STACK, chilled.getAmplifier() + 1);
            EffectApplicationUtil.setChiller(target, getOwner() != null ? getOwner() : this);
            target.addEffect(new MobEffectInstance(CoreEffects.CHILLED.get(), CHILLED_TICKS, chilledStack, false, true));
        }
    }

    private double getHoveringY() {
        double riseProgress = 1.0D - Math.exp(-deployedTicks * RISE_DECAY);
        double risingY = baseY + (hoverY - baseY) * riseProgress;
        if (deployedTicks < 35) {
            return risingY;
        }

        return hoverY + Math.sin((deployedTicks - 35) * BOB_SPEED) * BOB_AMPLITUDE;
    }
}
