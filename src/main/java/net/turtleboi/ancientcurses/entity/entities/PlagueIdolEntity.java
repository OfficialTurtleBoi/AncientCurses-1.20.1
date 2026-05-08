package net.turtleboi.ancientcurses.entity.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.turtleboi.ancientcurses.item.ModItems;
import net.turtleboi.turtlecore.TurtleCore;
import net.turtleboi.turtlecore.network.CoreNetworking;
import net.turtleboi.turtlecore.network.packet.spell.PulseAuraS2CPacket;
import net.turtleboi.turtlecore.capabilities.party.PlayerPartyProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class PlagueIdolEntity extends PathfinderMob {
    private static final EntityDataAccessor<Integer> EYE_PULSE_TICKS = SynchedEntityData.defineId(PlagueIdolEntity.class, EntityDataSerializers.INT);
    private static final String AGE_TAG = "Age";
    private static final String OWNER_TAG = "Owner";
    private static final String ITEM_DAMAGE_TAG = "ItemDamage";
    private static final String DEPLOYED_DAMAGE_TAKEN_TAG = "DeployedDamageTaken";
    private static final String SPREAD_TRACKING_TAG = "AncientCursesPlagueIdolSpread";
    private static final String TRACKING_IDOL_TAG = "Idol";
    private static final String TRACKING_SOURCE_TAG = "Source";
    private static final String TRACKING_SOURCE_DURATION_TAG = "SourceDuration";
    private static final int PULSE_INTERVAL_TICKS = 20;
    private static final int AURA_DURATION_TICKS = 20;
    private static final int EYE_PULSE_RAMP_TICKS = 4;
    private static final int EYE_PULSE_FADE_TICKS = 8;
    private static final int EYE_PULSE_DURATION_TICKS = AURA_DURATION_TICKS + EYE_PULSE_FADE_TICKS;
    private static final int AURA_QUEUE_DELAY_TICKS = 10;
    private static final int AURA_PULSES = 1;
    private static final double SPREAD_RADIUS = 7.0D;
    private static final double SPREAD_DURATION_SCALE = 0.75D;
    private static final ResourceLocation AURA_TEXTURE =
            new ResourceLocation(TurtleCore.MOD_ID, "textures/spell_effects/aura.png");
    private int age;
    private int itemDamage;
    private float deployedDamageTaken;
    private UUID ownerUUID;
    private long nextPulseGameTime;
    private final Map<MobEffect, Long> pulseSuppressedUntilByEffect = new HashMap<>();
    private final Queue<PulseRequest> pendingPulseRequests = new ArrayDeque<>();
    private final Set<MobEffect> queuedPulseEffects = new HashSet<>();

    public PlagueIdolEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        setNoAi(true);
        setNoGravity(true);
        setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(EYE_PULSE_TICKS, 0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 0.0D);
    }

    @Override
    public void tick() {
        super.tick();
        age++;

        if (level().isClientSide()) {
            return;
        }

        tickEyePulse();
        processPendingPulseRequests();

        if (age % PULSE_INTERVAL_TICKS == 0) {
            spreadEffects();
        }
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(@NotNull Entity entity) {
    }

    @Override
    public void push(double x, double y, double z) {
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean canBeAffected(@NotNull MobEffectInstance effectInstance) {
        return false;
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (!level().isClientSide() && amount > 0.0F && !isInvulnerableTo(source)) {
            deployedDamageTaken += amount;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }

        if (level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!canPlayerPickUp(player)) {
            return InteractionResult.FAIL;
        }

            ItemStack idolStack = createReturnedStack();
            if (!player.addItem(idolStack)) {
                spawnAtLocation(idolStack);
            }

        level().playSound(null, blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.75F, 1.0F);
        discard();
        return InteractionResult.CONSUME;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource damageSource, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(damageSource, looting, recentlyHit);
        ItemStack returnedStack = createReturnedStack();
        if (!returnedStack.isEmpty()) {
            spawnAtLocation(returnedStack);
        }
    }

    @Override
    public void die(@NotNull DamageSource damageSource) {
        if (!level().isClientSide()) {
            level().playSound(null, blockPosition(), SoundEvents.WOOD_BREAK, SoundSource.HOSTILE, 0.8F, 0.65F);
        }

        super.die(damageSource);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(AGE_TAG, age);
        tag.putInt(ITEM_DAMAGE_TAG, itemDamage);
        tag.putFloat(DEPLOYED_DAMAGE_TAKEN_TAG, deployedDamageTaken);
        if (ownerUUID != null) {
            tag.putUUID(OWNER_TAG, ownerUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        age = tag.getInt(AGE_TAG);
        itemDamage = tag.getInt(ITEM_DAMAGE_TAG);
        deployedDamageTaken = tag.getFloat(DEPLOYED_DAMAGE_TAKEN_TAG);
        ownerUUID = tag.hasUUID(OWNER_TAG) ? tag.getUUID(OWNER_TAG) : null;
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public void setItemDamage(int itemDamage) {
        this.itemDamage = Math.max(0, itemDamage);
    }

    public float getEyePulseIntensity(float partialTick) {
        int pulseTicks = entityData.get(EYE_PULSE_TICKS);
        if (pulseTicks <= 0) {
            return 0.0F;
        }

        float elapsed = EYE_PULSE_DURATION_TICKS - pulseTicks + partialTick;
        if (elapsed < EYE_PULSE_RAMP_TICKS) {
            float rampProgress = Mth.clamp(elapsed / (float) EYE_PULSE_RAMP_TICKS, 0.0F, 1.0F);
            return Mth.clamp((float) Math.pow(rampProgress, 3.0D), 0.0F, 1.0F);
        }

        if (elapsed <= AURA_DURATION_TICKS) {
            return 1.0F;
        }

        float fadeProgress = Mth.clamp((elapsed - AURA_DURATION_TICKS) / (float) EYE_PULSE_FADE_TICKS, 0.0F, 1.0F);
        return Mth.clamp((float) Math.pow(1.0F - fadeProgress, 2.0D), 0.0F, 1.0F);
    }

    private void tickEyePulse() {
        int pulseTicks = entityData.get(EYE_PULSE_TICKS);
        if (pulseTicks > 0) {
            entityData.set(EYE_PULSE_TICKS, pulseTicks - 1);
        }
    }

    private ItemStack createReturnedStack() {
        ItemStack idolStack = new ItemStack(ModItems.PLAGUE_IDOL.get());
        int totalDamage = itemDamage + Mth.ceil(deployedDamageTaken);
        if (totalDamage >= idolStack.getMaxDamage()) {
            return ItemStack.EMPTY;
        }

        idolStack.setDamageValue(totalDamage);
        return idolStack;
    }

    private void spreadEffects() {
        Set<UUID> protectedUUIDs = getProtectedUUIDs();
        List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(SPREAD_RADIUS),
                entity -> entity.isAlive()
                        && !(entity instanceof PlagueIdolEntity)
                        && !protectedUUIDs.contains(entity.getUUID()));
        List<SpreadSource> spreadSources = new ArrayList<>();

        for (LivingEntity source : entities) {
            if (source.getActiveEffects().isEmpty()) {
                continue;
            }

            for (MobEffectInstance effectInstance : source.getActiveEffects()) {
                MobEffect effect = effectInstance.getEffect();
                if (!isSpreadable(effect) || wasAppliedByAnyPlagueIdol(source, effect)) {
                    continue;
                }

                int spreadDuration = (int) (effectInstance.getDuration() * SPREAD_DURATION_SCALE);
                if (spreadDuration <= 20) {
                    continue;
                }

                spreadSources.add(new SpreadSource(source, new MobEffectInstance(
                        effectInstance.getEffect(),
                        spreadDuration,
                        effectInstance.getAmplifier(),
                        effectInstance.isAmbient(),
                        true,
                        effectInstance.showIcon()), effectInstance.getDuration()));
            }
        }

        for (SpreadSource spreadSource : spreadSources) {
            for (LivingEntity target : entities) {
                if (target == spreadSource.source()
                        || !shouldApply(target, spreadSource)) {
                    continue;
                }

                if (target.addEffect(new MobEffectInstance(spreadSource.effect()))) {
                    markSpreadApplication(target, spreadSource);
                    queuePulseAuraIfReady(spreadSource.effect().getEffect());
                    aggroAffectedMob(target);
                }
            }
        }
    }

    private void aggroAffectedMob(LivingEntity target) {
        if (target instanceof Mob mob && target instanceof Enemy) {
            mob.setTarget(this);
            mob.setLastHurtByMob(this);
        }
    }

    private boolean shouldApply(LivingEntity target, SpreadSource spreadSource) {
        MobEffectInstance spreadEffect = spreadSource.effect();
        MobEffectInstance existingEffect = target.getEffect(spreadEffect.getEffect());
        return existingEffect == null
                || existingEffect.getAmplifier() < spreadEffect.getAmplifier()
                || (existingEffect.getDuration() < spreadEffect.getDuration()
                && !wasAppliedBySameSourceApplication(target, spreadSource));
    }

    private boolean isSpreadable(MobEffect effect) {
        return effect.getCategory() == MobEffectCategory.HARMFUL;
    }

    private boolean wasAppliedByAnyPlagueIdol(LivingEntity entity, MobEffect effect) {
        return getSpreadTracking(entity, effect)
                .map(tracking -> tracking.hasUUID(TRACKING_IDOL_TAG))
                .orElse(false);
    }

    private boolean wasAppliedBySameSourceApplication(LivingEntity entity, SpreadSource spreadSource) {
        return getSpreadTracking(entity, spreadSource.effect().getEffect())
                .map(tracking -> tracking.hasUUID(TRACKING_IDOL_TAG)
                        && tracking.hasUUID(TRACKING_SOURCE_TAG)
                        && tracking.contains(TRACKING_SOURCE_DURATION_TAG)
                        && tracking.getUUID(TRACKING_SOURCE_TAG).equals(spreadSource.source().getUUID())
                        && tracking.getInt(TRACKING_SOURCE_DURATION_TAG) >= spreadSource.sourceEffectDuration())
                .orElse(false);
    }

    private java.util.Optional<CompoundTag> getSpreadTracking(LivingEntity entity, MobEffect effect) {
        if (!entity.hasEffect(effect)) {
            clearSpreadTracking(entity, effect);
            return java.util.Optional.empty();
        }

        CompoundTag allTracking = entity.getPersistentData().getCompound(SPREAD_TRACKING_TAG);
        String effectKey = getEffectKey(effect);
        if (!allTracking.contains(effectKey)) {
            return java.util.Optional.empty();
        }

        return java.util.Optional.of(allTracking.getCompound(effectKey));
    }

    private void markSpreadApplication(LivingEntity entity, SpreadSource spreadSource) {
        CompoundTag persistentData = entity.getPersistentData();
        CompoundTag allTracking = persistentData.getCompound(SPREAD_TRACKING_TAG);
        CompoundTag effectTracking = new CompoundTag();
        effectTracking.putUUID(TRACKING_IDOL_TAG, getUUID());
        effectTracking.putUUID(TRACKING_SOURCE_TAG, spreadSource.source().getUUID());
        effectTracking.putInt(TRACKING_SOURCE_DURATION_TAG, spreadSource.sourceEffectDuration());
        allTracking.put(getEffectKey(spreadSource.effect().getEffect()), effectTracking);
        persistentData.put(SPREAD_TRACKING_TAG, allTracking);
    }

    private void clearSpreadTracking(LivingEntity entity, MobEffect effect) {
        CompoundTag persistentData = entity.getPersistentData();
        if (!persistentData.contains(SPREAD_TRACKING_TAG)) {
            return;
        }

        CompoundTag allTracking = persistentData.getCompound(SPREAD_TRACKING_TAG);
        allTracking.remove(getEffectKey(effect));
        if (allTracking.isEmpty()) {
            persistentData.remove(SPREAD_TRACKING_TAG);
        } else {
            persistentData.put(SPREAD_TRACKING_TAG, allTracking);
        }
    }

    private String getEffectKey(MobEffect effect) {
        ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(effect);
        return effectId != null ? effectId.toString() : effect.getDescriptionId();
    }

    private Set<UUID> getProtectedUUIDs() {
        Set<UUID> protectedUUIDs = new HashSet<>();
        if (ownerUUID == null || !(level() instanceof ServerLevel serverLevel)) {
            return protectedUUIDs;
        }

        protectedUUIDs.add(ownerUUID);
        ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(ownerUUID);
        if (owner != null) {
            owner.getCapability(PlayerPartyProvider.PLAYER_PARTY).ifPresent(party -> protectedUUIDs.addAll(party.getMemberUUIDs()));
        }

        return protectedUUIDs;
    }

    private boolean canPlayerPickUp(Player player) {
        if (ownerUUID == null) {
            return false;
        }

        if (ownerUUID.equals(player.getUUID())) {
            return true;
        }

        return getProtectedUUIDs().contains(player.getUUID());
    }

    private void queuePulseAuraIfReady(MobEffect effect) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        long gameTime = serverLevel.getGameTime();
        long suppressedUntil = pulseSuppressedUntilByEffect.getOrDefault(effect, 0L);
        if (gameTime < suppressedUntil || queuedPulseEffects.contains(effect)) {
            return;
        }

        pendingPulseRequests.add(new PulseRequest(effect, effect.getColor()));
        queuedPulseEffects.add(effect);
    }

    private void processPendingPulseRequests() {
        if (!(level() instanceof ServerLevel serverLevel) || pendingPulseRequests.isEmpty()) {
            return;
        }

        long gameTime = serverLevel.getGameTime();
        if (gameTime < nextPulseGameTime) {
            return;
        }

        PulseRequest pulseRequest = pendingPulseRequests.poll();
        queuedPulseEffects.remove(pulseRequest.effect());
        pulseSuppressedUntilByEffect.put(pulseRequest.effect(), gameTime + AURA_DURATION_TICKS);
        nextPulseGameTime = gameTime + AURA_DURATION_TICKS + AURA_QUEUE_DELAY_TICKS;
        doEffectPulse(serverLevel, pulseRequest.color());
    }

    private void doEffectPulse(ServerLevel serverLevel, int effectColor) {
        entityData.set(EYE_PULSE_TICKS, EYE_PULSE_DURATION_TICKS);
        CoreNetworking.sendToNear(
                new PulseAuraS2CPacket(
                        getId(),
                        serverLevel.getGameTime(),
                        AURA_DURATION_TICKS,
                        AURA_PULSES,
                        (float) SPREAD_RADIUS,
                        effectColor,
                        AURA_TEXTURE,
                        true),
                this);
        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.FROG_AMBIENT,
                SoundSource.AMBIENT,
                1.0F,
                this.level().random.nextFloat() * 0.2F + 0.5F
        );
    }

    private record SpreadSource(LivingEntity source, MobEffectInstance effect, int sourceEffectDuration) {
    }

    private record PulseRequest(MobEffect effect, int color) {
    }
}
