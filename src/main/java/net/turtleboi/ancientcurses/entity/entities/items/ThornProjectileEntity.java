package net.turtleboi.ancientcurses.entity.entities.items;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.turtleboi.ancientcurses.entity.ModEntities;
import org.jetbrains.annotations.NotNull;

public class ThornProjectileEntity extends AbstractArrow {
    private static final ResourceLocation ROOTED_ID = new ResourceLocation("turtlecore", "rooted");
    private static final int SLOW_TICKS = 60;
    private static final int ROOT_TICKS = 30;
    private static final float ROOT_CHANCE = 0.25F;
    private static final float TRAIL_SPREAD = 0.2F;
    private final float damageAmount;

    public ThornProjectileEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.damageAmount = 3.5F;
    }

    public ThornProjectileEntity(Level level) {
        this(ModEntities.THORN_PROJECTILE.get(), level);
    }

    public ThornProjectileEntity(Level level, LivingEntity owner, float damageAmount) {
        super(ModEntities.THORN_PROJECTILE.get(), owner, level);
        this.damageAmount = damageAmount;
        this.setBaseDamage(damageAmount);
        this.pickup = Pickup.DISALLOWED;
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            level().addParticle(
                    ParticleTypes.SPORE_BLOSSOM_AIR,
                    this.getRandomX(TRAIL_SPREAD),
                    this.getRandomY(),
                    this.getRandomZ(TRAIL_SPREAD),
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        Entity hitEntity = result.getEntity();
        Entity ownerEntity = getOwner();

        if (hitEntity == ownerEntity) {
            return;
        }

        if (hitEntity instanceof LivingEntity livingEntity) {
            livingEntity.hurt(this.damageSources().thrown(this, ownerEntity), damageAmount);
            livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOW_TICKS, 0));

            MobEffect rooted = ForgeRegistries.MOB_EFFECTS.getValue(ROOTED_ID);
            if (rooted != null && this.random.nextFloat() < ROOT_CHANCE) {
                livingEntity.addEffect(new MobEffectInstance(rooted, ROOT_TICKS, 0));
            }
        }

        level().playSound(
                null,
                this.blockPosition(),
                SoundEvents.GRASS_BREAK,
                SoundSource.NEUTRAL,
                0.8F,
                0.9F + level().getRandom().nextFloat() * 0.2F
        );
        discard();
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        Vec3 impact = result.getLocation().subtract(this.getX(), this.getY(), this.getZ());
        this.setDeltaMovement(impact);
        Vec3 settle = impact.normalize().scale(0.05F);
        this.setPosRaw(this.getX() - settle.x, this.getY() - settle.y, this.getZ() - settle.z);
        this.inGround = true;
        this.shakeTime = 7;

        level().playSound(
                null,
                this.blockPosition(),
                SoundEvents.GRASS_BREAK,
                SoundSource.NEUTRAL,
                0.8F,
                0.7F + level().getRandom().nextFloat() * 0.2F
        );
        discard();
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }
}
