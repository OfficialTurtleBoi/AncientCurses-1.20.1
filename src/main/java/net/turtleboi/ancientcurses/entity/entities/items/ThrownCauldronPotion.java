package net.turtleboi.ancientcurses.entity.entities.items;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.turtleboi.ancientcurses.entity.ModEntities;
import net.turtleboi.ancientcurses.item.items.FathomlessCauldronItem;
import net.turtleboi.turtlecore.entity.weapons.BoltEntity;
import net.turtleboi.turtlecore.util.PartyUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ThrownCauldronPotion extends BoltEntity {
    private ItemStack cauldronStack = ItemStack.EMPTY;
    private boolean lingering;

    public ThrownCauldronPotion(EntityType<? extends BoltEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        this.xPower = 0;
        this.yPower = 0;
        this.zPower = 0;
        Vec3 velocity = getDeltaMovement();
        super.tick();
        Vec3 nextVelocity = velocity.multiply(0.98, 0.98, 0.98);
        if (!isNoGravity()) {
            nextVelocity = nextVelocity.add(0.0, -0.06, 0.0);
        }
        setDeltaMovement(nextVelocity);
    }

    @Override
    protected void onHit(@NotNull HitResult result) {
        if (level().isClientSide() || isRemoved()) {
            return;
        }

        Vec3 position = result.getLocation();
        List<MobEffectInstance> effects = FathomlessCauldronItem.getAllEffects(cauldronStack);

        if (lingering) {
            spawnLingeringCloud(position, effects);
        } else {
            applySplash(position, effects);
        }

        impact(position);
        discardBolt();
    }

    @Override
    protected void onHitEntity(@NotNull net.minecraft.world.phys.EntityHitResult result) {
    }

    @Override
    protected void onHitBlock(@NotNull net.minecraft.world.phys.BlockHitResult result) {
    }

    private void applySplash(Vec3 position, List<MobEffectInstance> effects) {
        if (effects.isEmpty()) {
            return;
        }

        LivingEntity owner = (getOwner() instanceof LivingEntity livingOwner) ? livingOwner : null;

        AABB searchArea = new AABB(
                position.x - 4, position.y - 2, position.z - 4,
                position.x + 4, position.y + 2, position.z + 4
        );

        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, searchArea, LivingEntity::isAlive)) {
            double distance = Math.sqrt(target.distanceToSqr(position.x, position.y, position.z));
            if (distance >= 4.0) {
                continue;
            }
            double distanceFactor = 1.0 - distance / 4.0;

            boolean targetIsAllied = target == owner || (owner != null && PartyUtils.isAlly(owner, target));

            for (MobEffectInstance effect : effects) {
                boolean effectIsHarmful = !effect.getEffect().isBeneficial();

                if (effectIsHarmful && targetIsAllied) {
                    continue;
                }
                if (!effectIsHarmful && !targetIsAllied && owner != null) {
                    continue;
                }

                if (effect.getEffect().isInstantenous()) {
                    effect.getEffect().applyInstantenousEffect(owner, owner, target,
                            effect.getAmplifier(), distanceFactor);
                } else {
                    int duration = (int) (distanceFactor * effect.getDuration() * 0.75 + 0.5);
                    if (duration > 0) {
                        target.addEffect(new MobEffectInstance(
                                effect.getEffect(), duration, effect.getAmplifier(),
                                effect.isAmbient(), effect.isVisible(), effect.showIcon()));
                    }
                }
            }
        }
    }

    private void spawnLingeringCloud(Vec3 position, List<MobEffectInstance> effects) {
        if (effects.isEmpty()) {
            return;
        }

        LivingEntity owner = (getOwner() instanceof LivingEntity livingOwner) ? livingOwner : null;

        LingeringCauldronCloud cloud = LingeringCauldronCloud.create(level(), owner, cauldronStack);
        cloud.setPos(position.x, position.y, position.z);
        level().addFreshEntity(cloud);
    }

    private void impact(Vec3 position) {
        int color = FathomlessCauldronItem.getFirstPotionColor(cauldronStack);
        int levelEventId = lingering ? 2007 : 2002;
        level().levelEvent(null, levelEventId, BlockPos.containing(position.x, position.y, position.z), color);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("CauldronItem", cauldronStack.save(new CompoundTag()));
        tag.putBoolean("Lingering", lingering);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        cauldronStack = ItemStack.of(tag.getCompound("CauldronItem"));
        lingering = tag.getBoolean("Lingering");
    }

    public static ThrownCauldronPotion create(Level level, LivingEntity shooter,
                                               ItemStack cauldron, boolean lingering) {
        ThrownCauldronPotion entity = new ThrownCauldronPotion(ModEntities.CAULDRON_POTION.get(), level);
        entity.setOwner(shooter);
        entity.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
        entity.cauldronStack = cauldron.copy();
        entity.lingering = lingering;
        entity.setColorRgb(FathomlessCauldronItem.getFirstPotionColor(cauldron));
        entity.setBoltPower(1.2F);
        entity.xPower = 0;
        entity.yPower = 0;
        entity.zPower = 0;
        return entity;
    }
}
