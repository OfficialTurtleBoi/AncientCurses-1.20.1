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
import net.turtleboi.ancientcurses.client.PlayerClientData;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.items.BeaconInfoPacketS2C;
import net.turtleboi.ancientcurses.network.packets.items.DowsingRodInfoPacketS2C;
import net.turtleboi.ancientcurses.util.AltarSavedData;

public class DowsingRod extends Item {
    public DowsingRod(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pLevel.isClientSide() || !(pPlayer instanceof ServerPlayer serverPlayer) || pUsedHand == InteractionHand.OFF_HAND) {
            return super.use(pLevel, pPlayer, pUsedHand);
        }

        ServerLevel serverLevel = (ServerLevel) serverPlayer.level();
        BlockPos playerPos = serverPlayer.blockPosition();
        AltarSavedData data = AltarSavedData.get(serverLevel);

        BlockPos altarFoundPos = null;
        double altarDistSqr = Double.MAX_VALUE;

        for (BlockPos altarPos : data.getAltars()) {
            if (serverLevel.getBlockEntity(altarPos) instanceof CursedAltarBlockEntity cursedAltar) {
                if (cursedAltar.hasPlayerCompletedRite(serverPlayer)) {
                    continue;
                }

                double d2 = altarPos.distSqr(playerPos);
                if (d2 < altarDistSqr) {
                    altarDistSqr = d2;
                    altarFoundPos    = altarPos;
                }
            }
        }

        if (altarFoundPos == null) {
            serverPlayer.sendSystemMessage(Component.literal("No altar found nearby."));
        } else {
            if (!PlayerClientData.getItemUsed()) {
                ModNetworking.sendToPlayer(new DowsingRodInfoPacketS2C(
                        true,
                        System.currentTimeMillis(),
                        altarFoundPos.getX(),
                        altarFoundPos.getY(),
                        altarFoundPos.getZ()
                ), serverPlayer);
            }

            double distance = Math.sqrt(altarDistSqr);
            //serverPlayer.sendSystemMessage(Component.literal(
            //        String.format("Nearest unfinished altar is at [%d, %d, %d] (%.1f blocks)",
            //                altarFoundPos.getX(), altarFoundPos.getY(), altarFoundPos.getZ(), distance)
            //));
        }

        return InteractionResultHolder.success(pPlayer.getItemInHand(pUsedHand));
    }

    @Override
    public void onInventoryTick(ItemStack pItemStack, Level pLevel, Player pPlayer, int pSlotIndex, int pSelectedIndex) {
        super.onInventoryTick(pItemStack, pLevel, pPlayer, pSlotIndex, pSelectedIndex);

        if (pLevel.isClientSide() || !(pPlayer instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (pPlayer.getMainHandItem() != pItemStack && PlayerClientData.getItemUsed()) {
            ModNetworking.sendToPlayer(new DowsingRodInfoPacketS2C(
                    false,
                    0,
                    0,
                    0,
                    0
            ), serverPlayer);
        }
    }
}
