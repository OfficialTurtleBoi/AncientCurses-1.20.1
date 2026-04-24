package net.turtleboi.ancientcurses.block.altar;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.ChunkPos;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;

import java.util.Comparator;
import java.util.UUID;

public final class LifecycleUtil {
    public static final TicketType<ChunkPos> CURSED_ALTAR_TICKET =
            TicketType.create("cursed_altar_ticket", Comparator.comparingLong(ChunkPos::toLong), 33);

    private static final EntityType<ArmorStand> OCCUPANT_TYPE = EntityType.ARMOR_STAND;

    private LifecycleUtil() {
    }

    public static void forceLoadChunk(CursedAltarBlockEntity altar) {
        if (!(altar.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        ChunkPos chunkPos = new ChunkPos(altar.getBlockPos());
        int ticketDistance = 1;
        serverLevel.getChunkSource().addRegionTicket(CURSED_ALTAR_TICKET, chunkPos, ticketDistance, chunkPos);
        altar.setChunkLoaded(true);
        altar.setChanged();
    }

    public static void releaseChunkLoad(CursedAltarBlockEntity altar) {
        if (!(altar.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        ChunkPos chunkPos = new ChunkPos(altar.getBlockPos());
        int ticketDistance = 1;
        serverLevel.getChunkSource().removeRegionTicket(CURSED_ALTAR_TICKET, chunkPos, ticketDistance, chunkPos);
        altar.setChunkLoaded(false);
        altar.setChanged();
    }

    public static void forceDimensionActive(CursedAltarBlockEntity altar) {
        if (!(altar.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        forceLoadChunk(altar);
        if (hasOccupantEntity(serverLevel, altar.getOccupantUuid())) {
            return;
        }

        ArmorStand occupant = OCCUPANT_TYPE.create(serverLevel);
        if (occupant == null) {
            return;
        }

        BlockPos pos = altar.getBlockPos();
        occupant.setPos(pos.getX() + 0.5, pos.getY() - 2, pos.getZ() + 0.5);
        occupant.setInvulnerable(true);
        occupant.setInvisible(true);
        occupant.setCustomName(Component.literal("Cursed Altar Occupant"));
        occupant.setCustomNameVisible(false);
        occupant.setNoGravity(true);
        serverLevel.addFreshEntity(occupant);
        altar.setOccupantUuid(occupant.getUUID());
    }

    public static void releaseDimensionActive(CursedAltarBlockEntity altar) {
        if (!(altar.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        UUID occupantUuid = altar.getOccupantUuid();
        if (occupantUuid != null) {
            Entity occupantEntity = serverLevel.getEntity(occupantUuid);
            if (occupantEntity != null) {
                occupantEntity.remove(Entity.RemovalReason.DISCARDED);
            }
            altar.setOccupantUuid(null);
        }

        releaseChunkLoad(altar);
    }

    public static boolean hasOccupantEntity(ServerLevel serverLevel, UUID occupantUuid) {
        if (occupantUuid == null) {
            return false;
        }
        return serverLevel.getEntity(occupantUuid) != null;
    }
}
