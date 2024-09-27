package net.turtleboi.ancientcurses.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.turtleboi.ancientcurses.item.ModItems;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class ThrownCursedPearl extends ThrowableItemProjectile {
    public ThrownCursedPearl(EntityType<? extends ThrowableItemProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public ThrownCursedPearl(Level pLevel) {
        super(ModEntities.CURSED_PEARL.get(), pLevel);
    }

    public ThrownCursedPearl(Level pLevel, LivingEntity pShooter) {
        super(ModEntities.CURSED_PEARL.get(), pShooter, pLevel);
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return ModItems.CURSED_PEARL.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        pResult.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 0.0F);
    }

    @Override
    protected void onHit(HitResult pResult) {
        super.onHit(pResult);
        for (int i = 0; i < 32; ++i) {
            this.level().addParticle(
                    new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.0F),
                    this.getX(),
                    this.getY() + this.random.nextDouble() * 2.0D, this.getZ(),
                    this.random.nextGaussian(),
                    0.0D,
                    this.random.nextGaussian());
        }

        if (!this.level().isClientSide && !this.isRemoved()) {
            Entity owner = this.getOwner();

            if (owner instanceof ServerPlayer player) {
                if (player.connection.isAcceptingMessages() && player.level() == this.level()) {
                    CursedPortalEntity startPortal = CursedPortalEntity.spawnPortal(this.level(), player.blockPosition());
                    CursedPortalEntity endPortal = CursedPortalEntity.spawnPortal(this.level(), this.blockPosition());
                    startPortal.setLinkedPortal(endPortal);
                    endPortal.setLinkedPortal(startPortal);
                    this.discard();
                    }
                }
            }
        this.discard();
    }

    @Override
    public void tick() {
        Entity owner = this.getOwner();
        if (owner instanceof LivingEntity && !owner.isAlive()) {
            this.discard();
        } else {
            super.tick();
        }
    }
}
