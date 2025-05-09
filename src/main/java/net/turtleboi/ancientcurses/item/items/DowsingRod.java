package net.turtleboi.ancientcurses.item.items;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteProvider;
import net.turtleboi.ancientcurses.client.PlayerClientData;
import net.turtleboi.ancientcurses.entity.CursedNodeEntity;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.items.DowsingRodInfoPacketS2C;
import net.turtleboi.ancientcurses.rites.EmbersRite;
import net.turtleboi.ancientcurses.rites.Rite;
import net.turtleboi.ancientcurses.util.AltarSavedData;

public class DowsingRod extends Item {
    public DowsingRod(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide() || hand == InteractionHand.OFF_HAND || PlayerClientData.getItemUsed()) {
            return super.use(level, player, hand);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return super.use(level, player, hand);
        }

        serverPlayer.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData -> {
            Rite activeRite = riteData.getActiveRite();
            if (activeRite instanceof EmbersRite embersRite) {
                findNearestNode(serverPlayer, embersRite);
            } else {
                findNearestAltar(serverPlayer);
            }
        });

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }


    @Override
    public void onInventoryTick(ItemStack pItemStack, Level pLevel, Player pPlayer, int pSlotIndex, int pSelectedIndex) {
        super.onInventoryTick(pItemStack, pLevel, pPlayer, pSlotIndex, pSelectedIndex);

        if (pLevel.isClientSide() || !(pPlayer instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (!(pPlayer.getMainHandItem().getItem() instanceof DowsingRod) && PlayerClientData.getItemUsed()) {
            ModNetworking.sendToPlayer(new DowsingRodInfoPacketS2C(
                    false,
                    0,
                    0,
                    0,
                    0
            ), serverPlayer);
        }
    }

    private void findNearestAltar(ServerPlayer serverPlayer) {
        ServerLevel level = (ServerLevel) serverPlayer.level();
        BlockPos pPos = serverPlayer.blockPosition();
        AltarSavedData data = AltarSavedData.get(level);

        BlockPos bestAltar = null;
        double bestDistSq = Double.MAX_VALUE;
        for (BlockPos altarPos : data.getAltars()) {
            if (!(level.getBlockEntity(altarPos) instanceof CursedAltarBlockEntity altar)) continue;
            if (altar.hasPlayerCompletedRite(serverPlayer)) continue;
            double d2 = altarPos.distSqr(pPos);
            if (d2 < bestDistSq) {
                bestDistSq = d2; bestAltar = altarPos;
            }
        }

        if (bestAltar != null) {
            ModNetworking.sendToPlayer(new DowsingRodInfoPacketS2C(
                    true,
                    System.currentTimeMillis(),
                    bestAltar.getX(),
                    bestAltar.getY(),
                    bestAltar.getZ()
            ), serverPlayer);
        } else {
            serverPlayer.sendSystemMessage(Component.literal("No altar found nearby."));
            ModNetworking.sendToPlayer(new DowsingRodInfoPacketS2C(
                    false, 0, 0, 0, 0
            ), serverPlayer);
        }
    }

    private static void findActiveAltar(ServerPlayer serverPlayer, Rite rite) {
        BlockPos altarPos = rite.getAltar().getBlockPos();
        //serverPlayer.sendSystemMessage(Component.literal("All Rite complete! Returning you to the altar."));
        ModNetworking.sendToPlayer(new DowsingRodInfoPacketS2C(
                true,
                System.currentTimeMillis(),
                altarPos.getX(), altarPos.getY(), altarPos.getZ()
        ), serverPlayer);
    }

    public static void findNearestNode(ServerPlayer serverPlayer, EmbersRite embersRite) {
        BlockPos target = embersRite.findNearestIncompleteNode(serverPlayer);
        if (target != null) {
            ModNetworking.sendToPlayer(new DowsingRodInfoPacketS2C(
                    true,
                    System.currentTimeMillis(),
                    target.getX(),
                    target.getY(),
                    target.getZ()
            ), serverPlayer);
        } else {
            findActiveAltar(serverPlayer, embersRite);
        }
    }
}


