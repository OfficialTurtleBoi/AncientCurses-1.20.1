package net.turtleboi.ancientcurses.entity.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.UUID;

public class VoodooSoulEntity extends AncientWraithEntity {
    public static final String ACTIVE_SOUL_TAG = "AncientCursesVoodooSoul";
    public static final String ARMOR_FRACTURE_UNTIL_TAG = "AncientCursesVoodooArmorFractureUntil";
    private static final String OWNER_TAG = "Owner";
    private static final String BODY_TAG = "Body";
    private static final String ACTIVATED_TAG = "Activated";
    private static final String ACTIVATION_TICKS_TAG = "ActivationTicks";
    private static final UUID ARMOR_FRACTURE_MODIFIER_UUID = UUID.fromString("02f9d9ab-4576-46d1-b8de-fdfd37f3f0df");
    private static final int TRAVEL_TICKS = 20;
    private static final int BODY_SLOW_DURATION_TICKS = 40;
    private static final int SOUL_DEBUFF_DURATION_TICKS = 20 * 10;
    private static final int BODY_SLOW_AMPLIFIER = 2;
    private static final int SOUL_DEATH_SLOW_AMPLIFIER = 2;
    private static final int SOUL_DEATH_WEAKNESS_AMPLIFIER = 1;

    @Nullable
    private UUID ownerUUID;
    @Nullable
    private UUID bodyUUID;
    private int activationTicks;
    private boolean activated;
    private boolean deathDebuffsApplied;

    public VoodooSoulEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        setNoGravity(true);
        xpReward = 0;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, true));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.ATTACK_SPEED, 1.6D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.05D);
    }

    public void setOwner(ServerPlayer player) {
        ownerUUID = player.getUUID();
    }

    public void setOriginalBody(LivingEntity body) {
        bodyUUID = body.getUUID();
    }

    public void setSoulHealth(float soulHealth) {
        AttributeInstance maxHealth = getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(soulHealth);
        }
        setHealth(soulHealth);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            return;
        }

        LivingEntity body = getBody();
        ServerPlayer owner = getOwnerPlayer();
        if (body == null || !body.isAlive() || owner == null || owner.isDeadOrDying()) {
            discard();
            return;
        }

        if (!activated) {
            moveTowardOwner(owner);
            return;
        }

        setNoGravity(false);
        setTarget(owner);
        if (tickCount % 20 == 0) {
            body.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, BODY_SLOW_DURATION_TICKS,
                    BODY_SLOW_AMPLIFIER, false, true));
        }
    }

    @Override
    public void die(@NotNull DamageSource damageSource) {
        if (!deathDebuffsApplied) {
            deathDebuffsApplied = true;
            LivingEntity body = getBody();
            if (body != null && body.isAlive()) {
                applySoulDeathDebuffs(body);
            }
        }
        super.die(damageSource);
    }

    @Override
    public void remove(@NotNull RemovalReason reason) {
        LivingEntity body = getBody();
        if (body != null && body.getPersistentData().hasUUID(ACTIVE_SOUL_TAG)
                && body.getPersistentData().getUUID(ACTIVE_SOUL_TAG).equals(getUUID())) {
            body.getPersistentData().remove(ACTIVE_SOUL_TAG);
        }
        super.remove(reason);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean canBeLeashed(@NotNull Player player) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUUID != null) {
            tag.putUUID(OWNER_TAG, ownerUUID);
        }
        if (bodyUUID != null) {
            tag.putUUID(BODY_TAG, bodyUUID);
        }
        tag.putBoolean(ACTIVATED_TAG, activated);
        tag.putInt(ACTIVATION_TICKS_TAG, activationTicks);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID(OWNER_TAG)) {
            ownerUUID = tag.getUUID(OWNER_TAG);
        }
        if (tag.hasUUID(BODY_TAG)) {
            bodyUUID = tag.getUUID(BODY_TAG);
        }
        activated = tag.getBoolean(ACTIVATED_TAG);
        activationTicks = tag.getInt(ACTIVATION_TICKS_TAG);
        setNoGravity(!activated);
    }

    public void mirrorDamageToBody(float amount) {
        LivingEntity body = getBody();
        if (body != null && body.isAlive()) {
            body.hurt(level().damageSources().magic(), amount);
        }
    }

    public static void applySoulDeathDebuffs(LivingEntity body) {
        body.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, SOUL_DEBUFF_DURATION_TICKS,
                SOUL_DEATH_WEAKNESS_AMPLIFIER, false, true));
        body.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 8,
                SOUL_DEATH_SLOW_AMPLIFIER, false, true));

        AttributeInstance armor = body.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            armor.removeModifier(ARMOR_FRACTURE_MODIFIER_UUID);
            armor.addTransientModifier(new AttributeModifier(ARMOR_FRACTURE_MODIFIER_UUID,
                    "Voodoo soul armor fracture", -0.5D, AttributeModifier.Operation.MULTIPLY_TOTAL));
            body.getPersistentData().putLong(ARMOR_FRACTURE_UNTIL_TAG, body.level().getGameTime() + SOUL_DEBUFF_DURATION_TICKS);
        }

        body.level().playSound(null, body.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 1.0F, 1.3F);
    }

    public static void tickArmorFracture(LivingEntity entity) {
        if (entity.level().isClientSide() || !entity.getPersistentData().contains(ARMOR_FRACTURE_UNTIL_TAG)) {
            return;
        }

        long removeAt = entity.getPersistentData().getLong(ARMOR_FRACTURE_UNTIL_TAG);
        if (entity.level().getGameTime() < removeAt) {
            return;
        }

        AttributeInstance armor = entity.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            armor.removeModifier(ARMOR_FRACTURE_MODIFIER_UUID);
        }
        entity.getPersistentData().remove(ARMOR_FRACTURE_UNTIL_TAG);
    }

    @Nullable
    private LivingEntity getBody() {
        if (!(level() instanceof ServerLevel serverLevel) || bodyUUID == null) {
            return null;
        }

        Entity entity = serverLevel.getEntity(bodyUUID);
        return entity instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    @Nullable
    private ServerPlayer getOwnerPlayer() {
        if (!(level() instanceof ServerLevel serverLevel) || ownerUUID == null) {
            return null;
        }
        return serverLevel.getServer().getPlayerList().getPlayer(ownerUUID);
    }

    private void moveTowardOwner(ServerPlayer owner) {
        activationTicks++;
        Vec3 target = owner.position()
                .add(owner.getLookAngle().scale(1.2D))
                .add(0.0D, owner.getBbHeight() * 0.6D, 0.0D);
        Vec3 toTarget = target.subtract(position());
        double progress = Mth.clamp(activationTicks / (double) TRAVEL_TICKS, 0.0D, 1.0D);
        setDeltaMovement(toTarget.scale(0.16D + progress * 0.22D));
        hasImpulse = true;
        hurtMarked = true;

        if (activationTicks >= TRAVEL_TICKS || toTarget.lengthSqr() < 0.35D) {
            activated = true;
            setNoGravity(false);
            setTarget(owner);
            level().playSound(null, blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 0.8F, 1.6F);
        }
    }
}
