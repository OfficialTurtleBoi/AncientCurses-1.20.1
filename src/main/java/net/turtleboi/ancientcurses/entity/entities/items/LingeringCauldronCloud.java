package net.turtleboi.ancientcurses.entity.entities.items;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;
import net.turtleboi.ancientcurses.entity.ModEntities;
import net.turtleboi.ancientcurses.item.items.FathomlessCauldronItem;
import net.turtleboi.turtlecore.util.PartyUtils;

import java.util.List;
import java.util.UUID;

public class LingeringCauldronCloud extends Entity {
    public static final int TOTAL_DURATION = 600;
    private static final int EFFECT_TICK_INTERVAL = 10;
    private static final float INITIAL_RADIUS = 3.0F;
    private static final float MINIMUM_RADIUS = 0.5F;

    private static final EntityDataAccessor<Float> DATA_RADIUS =
            SynchedEntityData.defineId(LingeringCauldronCloud.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_COLOR =
            SynchedEntityData.defineId(LingeringCauldronCloud.class, EntityDataSerializers.INT);

    private UUID ownerUUID;
    private ItemStack cauldronStack = ItemStack.EMPTY;
    private int remainingTicks = TOTAL_DURATION;

    public LingeringCauldronCloud(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        setNoGravity(true);
    }

    public static LingeringCauldronCloud create(Level level, LivingEntity owner, ItemStack cauldron) {
        LingeringCauldronCloud cloud = new LingeringCauldronCloud(ModEntities.LINGERING_CAULDRON_CLOUD.get(), level);
        if (owner != null) {
            cloud.ownerUUID = owner.getUUID();
        }
        cloud.cauldronStack = cauldron.copy();
        cloud.setRadius(INITIAL_RADIUS);
        cloud.setColor(FathomlessCauldronItem.getFirstPotionColor(cauldron));
        return cloud;
    }

    @Override
    protected void defineSynchedData() {
        getEntityData().define(DATA_RADIUS, INITIAL_RADIUS);
        getEntityData().define(DATA_COLOR, 0x385DC6);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (DATA_RADIUS.equals(key)) {
            refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(getRadius() * 2.0F, 0.5F);
    }

    public float getRadius() {
        return getEntityData().get(DATA_RADIUS);
    }

    public void setRadius(float radius) {
        if (!level().isClientSide()) {
            getEntityData().set(DATA_RADIUS, Mth.clamp(radius, 0.0F, 32.0F));
        }
    }

    public int getColor() {
        return getEntityData().get(DATA_COLOR);
    }

    public void setColor(int color) {
        getEntityData().set(DATA_COLOR, color);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            spawnCloudParticles();
            return;
        }

        remainingTicks--;
        if (remainingTicks <= 0) {
            discard();
            return;
        }

        float currentRadius = INITIAL_RADIUS * (remainingTicks / (float) TOTAL_DURATION);
        if (currentRadius < MINIMUM_RADIUS) {
            discard();
            return;
        }

        setRadius(currentRadius);

        if (tickCount % EFFECT_TICK_INTERVAL != 0) {
            return;
        }

        List<MobEffectInstance> effects = FathomlessCauldronItem.getAllEffects(cauldronStack);
        if (effects.isEmpty()) {
            return;
        }

        LivingEntity owner = resolveOwner();

        AABB searchArea = new AABB(
                getX() - currentRadius, getY() - 0.5, getZ() - currentRadius,
                getX() + currentRadius, getY() + 0.5, getZ() + currentRadius
        );

        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, searchArea, LivingEntity::isAlive)) {
            double distanceSquared = target.distanceToSqr(getX(), getY(), getZ());
            if (distanceSquared >= currentRadius * currentRadius) {
                continue;
            }

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
                    effect.getEffect().applyInstantenousEffect(owner, owner, target, effect.getAmplifier(), 0.5);
                } else {
                    target.addEffect(new MobEffectInstance(
                            effect.getEffect(), effect.getDuration() / 4, effect.getAmplifier(),
                            effect.isAmbient(), effect.isVisible(), effect.showIcon()));
                }
            }
        }
    }

    private void spawnCloudParticles() {
        float radius = getRadius();
        int particleCount = Mth.ceil((float) Math.PI * radius * radius);
        int color = getColor();
        double red   = ((color >> 16) & 0xFF) / 255.0;
        double green = ((color >>  8) & 0xFF) / 255.0;
        double blue  =  (color        & 0xFF) / 255.0;

        for (int i = 0; i < particleCount; i++) {
            float angle    = random.nextFloat() * ((float) Math.PI * 2.0F);
            float distance = Mth.sqrt(random.nextFloat()) * radius;
            double particleX = getX() + Mth.cos(angle) * distance;
            double particleY = getY();
            double particleZ = getZ() + Mth.sin(angle) * distance;
            level().addAlwaysVisibleParticle(ParticleTypes.ENTITY_EFFECT, particleX, particleY, particleZ, red, green, blue);
        }
    }

    private LivingEntity resolveOwner() {
        if (ownerUUID == null || !(level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(ownerUUID);
        return player;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("OwnerUUID")) {
            ownerUUID = tag.getUUID("OwnerUUID");
        }
        if (tag.contains("CauldronItem")) {
            cauldronStack = ItemStack.of(tag.getCompound("CauldronItem"));
        }
        remainingTicks = tag.getInt("RemainingTicks");
        setRadius(tag.getFloat("Radius"));
        setColor(tag.getInt("Color"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
        }
        tag.put("CauldronItem", cauldronStack.save(new CompoundTag()));
        tag.putInt("RemainingTicks", remainingTicks);
        tag.putFloat("Radius", getRadius());
        tag.putInt("Color", getColor());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
