package net.turtleboi.ancientcurses.item.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;

public class DowsingRod extends Item {
    public DowsingRod(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pLevel.isClientSide() || !(pPlayer instanceof ServerPlayer serverPlayer)) {
            return super.use(pLevel, pPlayer, pUsedHand);
        }

        ServerLevel serverLevel = (ServerLevel) serverPlayer.level();
        BlockPos playerPos = serverPlayer.blockPosition();

        int maxRadiusChunks = 16;

        int playerX = playerPos.getX() >> 4;
        int playerZ = playerPos.getZ() >> 4;

        BlockPos altarFoundPos = null;
        double altarDistSqr = Double.MAX_VALUE;

        for (int searchArea = 0; searchArea <= maxRadiusChunks; searchArea++) {
            for (int dx = -searchArea; dx <= searchArea; dx++) {
                for (int dz = -searchArea; dz <= searchArea; dz++) {
                    if (Math.abs(dx) != searchArea && Math.abs(dz) != searchArea) continue;
                    int cx = playerX + dx, cz = playerZ + dz;
                    LevelChunk chunk = serverLevel.getChunk(cx, cz);
                    for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                        if (!(blockEntity instanceof CursedAltarBlockEntity altarBlockEntity)) continue;
                        if (altarBlockEntity.hasPlayerCompletedRite(serverPlayer)) continue;

                        BlockPos altarPos = blockEntity.getBlockPos();
                        double distSqr = altarPos.distSqr(playerPos);
                        if (distSqr < altarDistSqr) {
                            altarDistSqr = distSqr;
                            altarFoundPos = altarPos;
                        }
                    }
                }
            }
            if (altarFoundPos != null) break;
        }

        if (altarFoundPos == null) {
            BlockPos nearestUnloadedAltar = serverLevel.findNearestMapStructure(
                    TagKey.create(
                            Registries.STRUCTURE,
                            new ResourceLocation(AncientCurses.MOD_ID, "cursed_altar")),
                    playerPos,
                    maxRadiusChunks,
                    true);

            if (nearestUnloadedAltar != null) {
                int y = serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE, nearestUnloadedAltar.getX(), nearestUnloadedAltar.getZ());
                altarFoundPos = new BlockPos(nearestUnloadedAltar.getX(), y, nearestUnloadedAltar.getZ());
                altarDistSqr = altarFoundPos.distSqr(playerPos);
            }
        }

        if (altarFoundPos == null) {
            serverPlayer.sendSystemMessage(Component.literal("No unfinished altar found nearby."));
        } else {
            double distance = Math.sqrt(altarDistSqr);
            serverPlayer.sendSystemMessage(Component.literal(
                    String.format("Nearest unfinished altar is at [%d, %d, %d] (%.1f blocks)",
                            altarFoundPos.getX(), altarFoundPos.getY(), altarFoundPos.getZ(), distance)
            ));
        }

        return InteractionResultHolder.sidedSuccess(pPlayer.getItemInHand(pUsedHand), false);
    }

}
