package net.turtleboi.ancientcurses.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import java.util.*;

public class CursedPortalEntity extends Entity {
    private static final EntityDataAccessor<Integer> TEXTURE_INDEX = SynchedEntityData.defineId(CursedPortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> TELEPORT_ENABLED = SynchedEntityData.defineId(CursedPortalEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int teleportCooldown = 100;
    private Map<UUID, Integer> playerCooldowns = new HashMap<>();

    private BlockPos altarPos;
    private CursedPortalEntity linkedPortal;
    private int textureTickCounter = 0;
    protected int age;

    public CursedPortalEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(TEXTURE_INDEX, 0);
        this.entityData.define(TELEPORT_ENABLED, false);
    }

    @Override
    public void tick() {
        super.tick();
        age = this.tickCount;

        if (this.level().isClientSide) {
            if (age % 20 == 0) {
                spawnPortalParticles();
            }
        }

        if (!this.level().isClientSide) {
            decrementCooldowns();
            textureTickCounter++;
            int ticksPerFrame = 2;
            if (textureTickCounter >= ticksPerFrame) {
                int newIndex = (this.getTextureIndex() + 1) % getMaxTextureIndex();
                this.setTextureIndex(newIndex);
                textureTickCounter = 0;
            }

            if (age == 0){
                playPortalSpawnSound();
            }

            if (age % 20 == 0) {
                spawnPortalParticles();
            }

            if (age % 100 == 0) {
                playPortalAmbientSound();
            }

            if (this.isTeleportEnabled()) {
                teleportNearbyPlayers();
            }

            if (age >= 620){
                this.discard();
            }
        }
    }

    private void spawnPortalParticles() {
        RandomSource random = this.level().getRandom();
        double portalX = this.getX();
        double portalZ = this.getZ();
        double portalHeightStart = this.getY();
        int numParticles = 8;

        for (int i = 0; i < numParticles; i++) {
            double randomY = portalHeightStart + random.nextDouble() * 2.0;
            double randomX = portalX + (random.nextDouble() - 0.5) * 1.25;
            double randomZ = portalZ + (random.nextDouble() - 0.5) * 1.25;
            double velocityX = (random.nextDouble() - 0.5) * 0.05;
            double velocityY = (random.nextDouble() - 0.5) * 0.05;
            double velocityZ = (random.nextDouble() - 0.5) * 0.05;
            this.level().addParticle(
                    new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.0F),
                    randomX,
                    randomY,
                    randomZ,
                    velocityX,
                    velocityY,
                    velocityZ);
        }
    }

    private void playPortalSpawnSound() {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD.get(), SoundSource.BLOCKS, 0.5F, this.level().random.nextFloat() * 0.4F + 0.8F);
    }

    private void playPortalAmbientSound() {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.5F, this.level().random.nextFloat() * 0.4F + 0.8F);
    }

    private void playPortalTravelSound(Player player) {
        this.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 0.25F, 1.0F);
    }

    private void decrementCooldowns() {
        playerCooldowns.entrySet().removeIf(entry -> entry.getValue() <= 0);
        playerCooldowns.replaceAll((uuid, cooldown) -> cooldown - 1);
    }

    private void teleportNearbyPlayers() {
        AABB detectionBox = this.getBoundingBox();
        List<Player> players = this.level().getEntitiesOfClass(Player.class, detectionBox);

        for (Player player : players) {
            if (player instanceof ServerPlayer serverPlayer) {
                UUID playerUUID = serverPlayer.getUUID();
                if (playerCooldowns.getOrDefault(playerUUID, 0) > 0) {
                    continue;
                }
                if (this.linkedPortal != null && this.linkedPortal.isTeleportEnabled()) {
                    teleportPlayerToPortal(serverPlayer, this.linkedPortal.blockPosition(), this.level());
                } else {
                    teleportPlayerToAltar(serverPlayer, altarPos, this.level());
                }
                playerCooldowns.put(playerUUID, teleportCooldown);
            }
        }
    }

    private void teleportPlayerToPortal(ServerPlayer player, BlockPos portalPos, Level level) {
        player.teleportTo(portalPos.getX(), portalPos.getY(), portalPos.getZ());
        playPortalTravelSound(player);
    }

    public void setLinkedPortal(CursedPortalEntity linkedPortal) {
        this.linkedPortal = linkedPortal;
    }

    public CursedPortalEntity getLinkedPortal() {
        return this.linkedPortal;
    }

    public static CursedPortalEntity spawnPortal(Level level, BlockPos altarPos) {
        CursedPortalEntity portal = new CursedPortalEntity(ModEntities.CURSED_PORTAL.get(), level);
        portal.setPos(altarPos.getX() + 0.5, altarPos.getY(), altarPos.getZ() + 0.5);
        portal.setTeleportEnabled(true);
        level.addFreshEntity(portal);
        return portal;
    }

    private void teleportPlayerToAltar(ServerPlayer player, BlockPos altarPos, Level level) {
        BlockPos playerPos = player.blockPosition();
        ServerLevel serverLevel = (ServerLevel) level;

        float yaw = calculateYawTowardAltar(playerPos, altarPos);
        float pitch = calculatePitchTowardAltar(player, playerPos, altarPos);

        player.teleportTo(serverLevel, altarPos.getX() + 2.5, altarPos.getY(), altarPos.getZ() + 2.5, Set.of(), yaw, pitch);
        playPortalTravelSound(player);
    }

    private static float calculateYawTowardAltar(BlockPos playerPos, BlockPos altarPos) {
        double deltaX = (altarPos.getX() + 0.5) - playerPos.getX();
        double deltaZ = (altarPos.getZ() + 0.5) - playerPos.getZ();
        return (float) (Math.atan2(deltaZ, deltaX) * (180.0 / Math.PI)) - 193.0F;
    }

    private static float calculatePitchTowardAltar(Player player, BlockPos playerPos, BlockPos altarPos) {
        double deltaX = (altarPos.getX() + 0.5) - playerPos.getX();
        double deltaZ = (altarPos.getZ() + 0.5) - playerPos.getZ();
        double deltaY = altarPos.getY() - (playerPos.getY() + player.getEyeHeight());

        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        return (float) -(Math.atan2(deltaY, horizontalDistance) * (180.0 / Math.PI));
    }

    public void setTeleportEnabled(boolean enabled) {
        this.entityData.set(TELEPORT_ENABLED, enabled);
    }

    public boolean isTeleportEnabled() {
        return this.entityData.get(TELEPORT_ENABLED);
    }

    public int getTextureIndex() {
        return this.entityData.get(TEXTURE_INDEX);
    }

    public void setTextureIndex(int index) {
        this.entityData.set(TEXTURE_INDEX, index);
    }

    public int getMaxTextureIndex() {
        return 13;
    }

    public void setAltarPos(BlockPos pos) {
        this.altarPos = pos;
    }

    public static CursedPortalEntity spawnPortalNearPlayer(Player player, BlockPos altarPos, Level level) {
        BlockPos portalPos = findRandomValidLocationNearPlayer(player, level);

        if (portalPos != null) {
            CursedPortalEntity portal = new CursedPortalEntity(ModEntities.CURSED_PORTAL.get(), level);
            portal.setPos(portalPos.getX() + 0.5, portalPos.getY() + 0.5, portalPos.getZ() + 0.5);
            portal.setAltarPos(altarPos);
            portal.setTeleportEnabled(true);
            level.addFreshEntity(portal);
            return portal;
        } else {
            player.sendSystemMessage(Component.literal("No space for a portal!").withStyle(ChatFormatting.RED));
            return null;
        }
    }

    private static BlockPos findRandomValidLocationNearPlayer(Player player, Level level) {
        RandomSource random = level.getRandom();
        BlockPos playerPos = player.blockPosition();

        for (int attempts = 0; attempts < 10; attempts++) {
            int offsetX = random.nextInt(7) - 3;
            int offsetZ = random.nextInt(7) - 3;
            int offsetY = random.nextInt(3) - 1;

            BlockPos potentialPos = playerPos.offset(offsetX, offsetY, offsetZ);

            if (isValidPortalPosition(level, potentialPos) && isFarEnoughFromPlayer(playerPos, potentialPos)) {
                return potentialPos;
            }
        }
        return null;
    }

    private static boolean isValidPortalPosition(Level level, BlockPos pos) {
        return level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir();
    }

    private static boolean isFarEnoughFromPlayer(BlockPos playerPos, BlockPos portalPos) {
        double distanceSquared = playerPos.distSqr(portalPos);
        return distanceSquared >= 4.0;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        this.setTeleportEnabled(pCompound.getBoolean("TeleportEnabled"));
        if (pCompound.contains("AltarPos")) {
            this.altarPos = BlockPos.of(pCompound.getLong("AltarPos"));
        }
        if (pCompound.hasUUID("LinkedPortalUUID")) {
            if (linkedPortal != null) {
                if (this.linkedPortal.getUUID() != null && this.level() instanceof ServerLevel serverLevel) {
                    Entity linkedEntity = serverLevel.getEntity(this.linkedPortal.getUUID());
                    if (linkedEntity instanceof CursedPortalEntity) {
                        this.linkedPortal = (CursedPortalEntity) linkedEntity;
                    }
                }
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.putBoolean("TeleportEnabled", this.isTeleportEnabled());
        if (this.altarPos != null) {
            pCompound.putLong("AltarPos", this.altarPos.asLong());
        }
        if (this.linkedPortal != null) {
            pCompound.putUUID("LinkedPortalUUID", this.linkedPortal.getUUID());
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
