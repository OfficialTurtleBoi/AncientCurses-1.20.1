package net.turtleboi.ancientcurses.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Set;

public class CursedPortalEntity extends Entity {
    private static final EntityDataAccessor<Integer> TEXTURE_INDEX = SynchedEntityData.defineId(CursedPortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> TELEPORT_ENABLED = SynchedEntityData.defineId(CursedPortalEntity.class, EntityDataSerializers.BOOLEAN);

    private BlockPos altarPos;
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
        if (!this.level().isClientSide) {
            textureTickCounter++;
            int ticksPerFrame = 2;
            if (textureTickCounter >= ticksPerFrame) {
                int newIndex = (this.getTextureIndex() + 1) % getMaxTextureIndex();
                this.setTextureIndex(newIndex);
                textureTickCounter = 0;
            }

            if (this.isTeleportEnabled()) {
                teleportNearbyPlayers();
            }

            if (age >= 620){
                this.discard();
            }
        }
    }

    private void teleportNearbyPlayers() {
        AABB detectionBox = this.getBoundingBox();
        List<Player> players = this.level().getEntitiesOfClass(Player.class, detectionBox);

        for (Player player : players) {
            if (player instanceof ServerPlayer serverPlayer) {
                teleportPlayerToAltar(serverPlayer, altarPos, this.level());
            }
        }
    }

    private void teleportPlayerToAltar(ServerPlayer player, BlockPos altarPos, Level level) {
        BlockPos playerPos = player.blockPosition();
        ServerLevel serverLevel = (ServerLevel) level;

        float yaw = calculateYaw(playerPos, altarPos);
        float pitch = calculatePitch(player, playerPos, altarPos);

        player.teleportTo(serverLevel, altarPos.getX() + 2.5, altarPos.getY(), altarPos.getZ() + 2.5, Set.of(), yaw, pitch);
    }

    private static float calculateYaw(BlockPos playerPos, BlockPos altarPos) {
        double deltaX = (altarPos.getX() + 0.5) - playerPos.getX();
        double deltaZ = (altarPos.getZ() + 0.5) - playerPos.getZ();
        return (float) (Math.atan2(deltaZ, deltaX) * (180.0 / Math.PI)) - 193.0F;
    }

    private static float calculatePitch(Player player, BlockPos playerPos, BlockPos altarPos) {
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
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.putBoolean("TeleportEnabled", this.isTeleportEnabled());
        if (this.altarPos != null) {
            pCompound.putLong("AltarPos", this.altarPos.asLong());
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
