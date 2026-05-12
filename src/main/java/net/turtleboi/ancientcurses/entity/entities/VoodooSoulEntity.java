package net.turtleboi.ancientcurses.entity.entities;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.items.VoodooSoulSyncS2C;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.UUID;

public class VoodooSoulEntity extends AncientWraithEntity {
    private static final EntityDataAccessor<Integer> BODY_ENTITY_ID = SynchedEntityData.defineId(VoodooSoulEntity.class, EntityDataSerializers.INT);
    public static final String ACTIVE_SOUL_TAG = "AncientCursesVoodooSoul";
    public static final String SOUL_CLONE_TAG = "AncientCursesVoodooSoulClone";
    public static final String ARMOR_FRACTURE_UNTIL_TAG = "AncientCursesVoodooArmorFractureUntil";
    private static final String OWNER_TAG = "AncientCursesVoodooOwner";
    private static final String BODY_TAG = "AncientCursesVoodooBody";
    private static final String ACTIVATED_TAG = "AncientCursesVoodooActivated";
    private static final String ACTIVATION_TICKS_TAG = "AncientCursesVoodooActivationTicks";
    private static final String LIFETIME_TICKS_TAG = "AncientCursesVoodooLifetimeTicks";
    private static final String RETURNING_TAG = "AncientCursesVoodooReturning";
    private static final String RETURN_TICKS_TAG = "AncientCursesVoodooReturnTicks";
    private static final String MIRRORED_DAMAGE_TAG = "AncientCursesVoodooMirroredDamage";
    private static final String SOUL_DAMAGE_CAP_TAG = "AncientCursesVoodooDamageCap";
    private static final UUID ARMOR_FRACTURE_MODIFIER_UUID = UUID.fromString("02f9d9ab-4576-46d1-b8de-fdfd37f3f0df");
    private static final int TRAVEL_TICKS = 20;
    private static final int ACTIVE_LIFETIME_TICKS = 20 * 20;
    private static final int RETURN_TICKS = 16;
    private static final double MAX_SCRIPTED_SOUL_SPEED = 1.2D;
    private static final int BODY_SLOW_DURATION_TICKS = 40;
    private static final int SOUL_DEBUFF_DURATION_TICKS = 20 * 10;
    private static final int BODY_SLOW_AMPLIFIER = 2;
    private static final int SOUL_DEATH_SLOW_AMPLIFIER = 2;
    private static final int SOUL_DEATH_WEAKNESS_AMPLIFIER = 1;

    public static final float BOSS_HEALTH_THRESHOLD = 100.0F;

    public static final float NORMAL_SOUL_HEALTH_SCALE = 0.40F;
    public static final float BOSS_SOUL_HEALTH_SCALE = 0.20F;
    private static final float NORMAL_SOUL_HEALTH_CAP = 80.0F;
    private static final float BOSS_SOUL_HEALTH_CAP = 50.0F;

    private static final double SOUL_SPEED_SCALE = 0.70;
    private static final double NORMAL_SOUL_SPEED_CAP = 0.52;
    private static final double BOSS_SOUL_SPEED_CAP = 0.28;

    private static final double SOUL_ATTACK_SCALE = 0.60;
    private static final double NORMAL_SOUL_ATTACK_CAP = 12.0;
    private static final double BOSS_SOUL_ATTACK_CAP = 8.0;

    private static final String BODY_SPEED_TAG = "AncientCursesVoodooBodySpeed";
    private static final String BODY_ATTACK_TAG = "AncientCursesVoodooBodyAttack";
    private static final String BODY_IS_BOSS_TAG = "AncientCursesVoodooBodyIsBoss";

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
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3D, true));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.ATTACK_SPEED, 2.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(BODY_ENTITY_ID, -1);
    }

    public void setOwner(ServerPlayer player) {
        ownerUUID = player.getUUID();
    }

    public void setOriginalBody(LivingEntity body) {
        bodyUUID = body.getUUID();
        entityData.set(BODY_ENTITY_ID, body.getId());
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
        entityData.set(BODY_ENTITY_ID, body.getId());

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

    public int getBodyEntityId() {
        return entityData.get(BODY_ENTITY_ID);
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

    public static void markSoulClone(Mob soul, ServerPlayer owner, LivingEntity body) {
        CompoundTag data = soul.getPersistentData();
        data.putBoolean(SOUL_CLONE_TAG, true);
        data.putUUID(OWNER_TAG, owner.getUUID());
        data.putUUID(BODY_TAG, body.getUUID());
        data.putBoolean(ACTIVATED_TAG, false);
        data.putInt(ACTIVATION_TICKS_TAG, 0);
        data.putInt(LIFETIME_TICKS_TAG, 0);
        data.putBoolean(RETURNING_TAG, false);
        data.putInt(RETURN_TICKS_TAG, 0);
        data.putFloat(MIRRORED_DAMAGE_TAG, 0.0F);

        boolean isBoss = body.getMaxHealth() >= BOSS_HEALTH_THRESHOLD;
        data.putBoolean(BODY_IS_BOSS_TAG, isBoss);

        AttributeInstance speedAttr = body.getAttribute(Attributes.MOVEMENT_SPEED);
        double bodySpeed = speedAttr != null ? speedAttr.getBaseValue() : 0.25;
        double scaledSpeed = bodySpeed * SOUL_SPEED_SCALE;
        scaledSpeed = isBoss ? Math.min(scaledSpeed, BOSS_SOUL_SPEED_CAP) : Math.min(scaledSpeed, NORMAL_SOUL_SPEED_CAP);
        data.putDouble(BODY_SPEED_TAG, Math.max(0.18, scaledSpeed));

        AttributeInstance attackAttr = body.getAttribute(Attributes.ATTACK_DAMAGE);
        double bodyAttack = attackAttr != null ? attackAttr.getBaseValue() : 3.0;
        double scaledAttack = bodyAttack * SOUL_ATTACK_SCALE;
        scaledAttack = isBoss ? Math.min(scaledAttack, BOSS_SOUL_ATTACK_CAP) : Math.min(scaledAttack, NORMAL_SOUL_ATTACK_CAP);
        data.putDouble(BODY_ATTACK_TAG, Math.max(2.0, scaledAttack));

        soul.setPersistenceRequired();
    }

    public static float computeSoulHealth(LivingEntity body) {
        boolean isBoss = body.getMaxHealth() >= BOSS_HEALTH_THRESHOLD;
        float scale = isBoss ? BOSS_SOUL_HEALTH_SCALE : NORMAL_SOUL_HEALTH_SCALE;
        float cap = isBoss ? BOSS_SOUL_HEALTH_CAP : NORMAL_SOUL_HEALTH_CAP;
        return Math.min(cap, Math.max(1.0F, body.getMaxHealth() * scale));
    }

    public static void applySoulAttributes(LivingEntity soul) {
        CompoundTag data = soul.getPersistentData();

        if (data.contains(BODY_SPEED_TAG)) {
            AttributeInstance attr = soul.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attr != null) {
                attr.setBaseValue(data.getDouble(BODY_SPEED_TAG));
            }
        }

        if (data.contains(BODY_ATTACK_TAG)) {
            AttributeInstance attr = soul.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attr != null) {
                attr.setBaseValue(data.getDouble(BODY_ATTACK_TAG));
            }
        }
    }

    public static void syncSoulCloneToClients(LivingEntity soul, boolean active) {
        if (soul.level() instanceof ServerLevel serverLevel) {
            VoodooSoulSyncS2C packet = new VoodooSoulSyncS2C(soul.getUUID(), active);
            for (ServerPlayer player : serverLevel.players()) {
                ModNetworking.sendToPlayer(packet, player);
            }
        }
    }

    public static void syncSoulCloneToPlayer(LivingEntity soul, ServerPlayer player, boolean active) {
        ModNetworking.sendToPlayer(new VoodooSoulSyncS2C(soul.getUUID(), active), player);
    }

    public static boolean isSoulClone(Entity entity) {
        return entity instanceof LivingEntity livingEntity
                && livingEntity.getPersistentData().getBoolean(SOUL_CLONE_TAG);
    }

    public static void tickLinkedBody(LivingEntity body) {
        if (!body.level().isClientSide() && body.getPersistentData().hasUUID(ACTIVE_SOUL_TAG)) {
            clampUnsafeSoulMotion(body);
        }
    }

    public static void setSoulHealth(LivingEntity soul, float soulHealth) {
        AttributeInstance maxHealth = soul.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(soulHealth);
        }
        soul.setHealth(soulHealth);
        soul.getPersistentData().putFloat(SOUL_DAMAGE_CAP_TAG, soulHealth);
    }

    public static void beginSoulTravel(LivingEntity soul, ServerPlayer owner) {
        CompoundTag data = soul.getPersistentData();
        data.putInt(ACTIVATION_TICKS_TAG, 1);
        moveSoulTowardOwner(soul, owner, 1);
    }

    public static void tickSoulClone(LivingEntity soul) {
        if (!(soul.level() instanceof ServerLevel serverLevel) || !isSoulClone(soul)) {
            return;
        }

        LivingEntity body = getLinkedBody(serverLevel, soul);
        ServerPlayer owner = getLinkedOwner(serverLevel, soul);
        if (body == null || !body.isAlive() || owner == null || owner.isDeadOrDying()) {
            clearBodySoulTag(body, soul);
            syncSoulCloneToClients(soul, false);
            soul.discard();
            return;
        }
        clampUnsafeSoulMotion(soul);
        clampUnsafeSoulMotion(body);

        CompoundTag data = soul.getPersistentData();
        if (data.getBoolean(RETURNING_TAG)) {
            returnSoulToBody(soul, body, data);
            return;
        }

        if (!data.getBoolean(ACTIVATED_TAG)) {
            int activationTicks = data.getInt(ACTIVATION_TICKS_TAG) + 1;
            data.putInt(ACTIVATION_TICKS_TAG, activationTicks);
            if (moveSoulTowardOwner(soul, owner, activationTicks)) {
                data.putBoolean(ACTIVATED_TAG, true);
                soul.setNoGravity(false);
                soul.setDeltaMovement(Vec3.ZERO);
                applySoulAttributes(soul);
                if (soul instanceof Mob mob) {
                    mob.setNoAi(false);
                    mob.setTarget(owner);
                }
                soul.level().playSound(null, soul.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 0.8F, 1.6F);
            }
            return;
        }

        int lifetimeTicks = data.getInt(LIFETIME_TICKS_TAG) + 1;
        data.putInt(LIFETIME_TICKS_TAG, lifetimeTicks);
        if (lifetimeTicks >= ACTIVE_LIFETIME_TICKS) {
            data.putBoolean(RETURNING_TAG, true);
            data.putInt(RETURN_TICKS_TAG, 0);
            soul.level().playSound(null, soul.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 0.7F, 0.9F);
            returnSoulToBody(soul, body, data);
            return;
        }

        soul.setNoGravity(false);
        if (soul instanceof Mob mob) {
            mob.setNoAi(false);
            if (soul.tickCount % 20 == 0 || mob.getTarget() == null || !mob.getTarget().isAlive()) {
                mob.setTarget(owner);
            }
        }
        if (soul.tickCount % 20 == 0) {
            body.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, BODY_SLOW_DURATION_TICKS,
                    BODY_SLOW_AMPLIFIER, false, true));
        }
    }

    private static void returnSoulToBody(LivingEntity soul, LivingEntity body, CompoundTag data) {
        int returnTicks = data.getInt(RETURN_TICKS_TAG) + 1;
        data.putInt(RETURN_TICKS_TAG, returnTicks);
        if (soul instanceof Mob mob) {
            mob.setNoAi(true);
            mob.setTarget(null);
        }

        Vec3 target = body.position().add(0.0D, body.getBbHeight() * 0.5D, 0.0D);
        Vec3 toTarget = target.subtract(soul.position());
        double progress = Mth.clamp(returnTicks / (double) RETURN_TICKS, 0.0D, 1.0D);
        soul.setDeltaMovement(getScriptedSoulVelocity(toTarget, 0.28D + progress * 0.42D));
        soul.setNoGravity(true);
        soul.hasImpulse = true;
        soul.hurtMarked = true;

        if (returnTicks >= RETURN_TICKS || toTarget.lengthSqr() < 0.5D) {
            mergeSoulIntoBody(soul, body);
        }
    }

    private static void mergeSoulIntoBody(LivingEntity soul, LivingEntity body) {
        clearBodySoulTag(body, soul);
        syncSoulCloneToClients(soul, false);
        spawnSoulMergeParticles(body);
        body.level().playSound(null, body.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 0.8F, 1.2F);
        soul.discard();
    }

    private static boolean moveSoulTowardOwner(LivingEntity soul, ServerPlayer owner, int activationTicks) {
        Vec3 target = owner.position()
                .add(owner.getLookAngle().scale(1.2D))
                .add(0.0D, owner.getBbHeight() * 0.6D, 0.0D);
        Vec3 toTarget = target.subtract(soul.position());
        double progress = Mth.clamp(activationTicks / (double) TRAVEL_TICKS, 0.0D, 1.0D);
        soul.setDeltaMovement(getScriptedSoulVelocity(toTarget, 0.22D + progress * 0.28D));
        soul.setNoGravity(true);
        soul.hasImpulse = true;
        soul.hurtMarked = true;
        return activationTicks >= TRAVEL_TICKS || toTarget.lengthSqr() < 0.35D;
    }

    private static Vec3 getScriptedSoulVelocity(Vec3 toTarget, double speedScale) {
        if (!isFinite(toTarget) || toTarget.lengthSqr() < 0.0001D) {
            return Vec3.ZERO;
        }

        Vec3 movement = toTarget.scale(speedScale);
        if (movement.lengthSqr() > toTarget.lengthSqr()) {
            movement = toTarget;
        }
        if (movement.lengthSqr() > MAX_SCRIPTED_SOUL_SPEED * MAX_SCRIPTED_SOUL_SPEED) {
            movement = movement.normalize().scale(MAX_SCRIPTED_SOUL_SPEED);
        }
        return movement;
    }

    private static void clampUnsafeSoulMotion(LivingEntity soul) {
        Vec3 movement = soul.getDeltaMovement();
        if (!isFinite(movement)) {
            soul.setDeltaMovement(Vec3.ZERO);
            return;
        }

        double maxMovement = 4.0D;
        if (movement.lengthSqr() > maxMovement * maxMovement) {
            soul.setDeltaMovement(movement.normalize().scale(maxMovement));
            soul.hasImpulse = true;
            soul.hurtMarked = true;
        }
    }

    private static boolean isFinite(Vec3 vector) {
        return Double.isFinite(vector.x) && Double.isFinite(vector.y) && Double.isFinite(vector.z);
    }

    public static void mirrorSoulDamageToBody(LivingEntity soul, float amount) {
        LivingEntity body = getLinkedBody(soul);
        if (body == null || !body.isAlive()) {
            return;
        }

        CompoundTag data = soul.getPersistentData();
        float damageCap = data.contains(SOUL_DAMAGE_CAP_TAG) ? data.getFloat(SOUL_DAMAGE_CAP_TAG) : soul.getMaxHealth();
        float mirroredDamage = data.getFloat(MIRRORED_DAMAGE_TAG);
        float damageToMirror = Math.min(amount, Math.max(0.0F, damageCap - mirroredDamage));
        if (damageToMirror > 0.0F) {
            data.putFloat(MIRRORED_DAMAGE_TAG, mirroredDamage + damageToMirror);
            body.hurt(soul.level().damageSources().magic(), damageToMirror);
        }
    }

    public static void handleSoulDeath(LivingEntity soul) {
        LivingEntity body = getLinkedBody(soul);
        spawnSoulDeathParticles(soul);
        clearBodySoulTag(body, soul);
        if (body != null && body.isAlive()) {
            applySoulDeathDebuffs(body);
        }
    }

    private static void spawnSoulDeathParticles(LivingEntity soul) {
        if (!(soul.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        double x = soul.getX();
        double y = soul.getY() + soul.getBbHeight() * 0.5D;
        double z = soul.getZ();
        double spreadX = Math.max(0.25D, soul.getBbWidth() * 0.6D);
        double spreadY = Math.max(0.35D, soul.getBbHeight() * 0.45D);
        double spreadZ = Math.max(0.25D, soul.getBbWidth() * 0.6D);
        serverLevel.sendParticles(ParticleTypes.SOUL, x, y, z, 28, spreadX, spreadY, spreadZ, 0.12D);
        serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 18,
                spreadX * 0.8D, spreadY * 0.7D, spreadZ * 0.8D, 0.08D);
    }

    private static void spawnSoulMergeParticles(LivingEntity body) {
        if (!(body.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        double x = body.getX();
        double y = body.getY() + body.getBbHeight() * 0.5D;
        double z = body.getZ();
        double spreadX = Math.max(0.25D, body.getBbWidth() * 0.4D);
        double spreadY = Math.max(0.35D, body.getBbHeight() * 0.35D);
        double spreadZ = Math.max(0.25D, body.getBbWidth() * 0.4D);
        serverLevel.sendParticles(ParticleTypes.SOUL, x, y, z, 18, spreadX, spreadY, spreadZ, 0.06D);
    }

    @Nullable
    private static LivingEntity getLinkedBody(LivingEntity soul) {
        if (!(soul.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        return getLinkedBody(serverLevel, soul);
    }

    @Nullable
    private static LivingEntity getLinkedBody(ServerLevel serverLevel, LivingEntity soul) {
        CompoundTag data = soul.getPersistentData();
        if (!data.hasUUID(BODY_TAG)) {
            return null;
        }

        Entity entity = serverLevel.getEntity(data.getUUID(BODY_TAG));
        return entity instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    @Nullable
    private static ServerPlayer getLinkedOwner(ServerLevel serverLevel, LivingEntity soul) {
        CompoundTag data = soul.getPersistentData();
        if (!data.hasUUID(OWNER_TAG)) {
            return null;
        }
        return serverLevel.getServer().getPlayerList().getPlayer(data.getUUID(OWNER_TAG));
    }

    private static void clearBodySoulTag(@Nullable LivingEntity body, LivingEntity soul) {
        if (body != null && body.getPersistentData().hasUUID(ACTIVE_SOUL_TAG)
                && body.getPersistentData().getUUID(ACTIVE_SOUL_TAG).equals(soul.getUUID())) {
            body.getPersistentData().remove(ACTIVE_SOUL_TAG);
        }
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
            applySoulAttributes(this);
            setTarget(owner);
            level().playSound(null, blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 0.8F, 1.6F);
        }
    }
}
