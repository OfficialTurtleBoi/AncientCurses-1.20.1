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
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.items.DowsingRodInfoPacketS2C;
import net.turtleboi.ancientcurses.rite.Rite;
import net.turtleboi.ancientcurses.rite.util.RiteLocator;
import net.turtleboi.ancientcurses.util.AltarSavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DowsingRod extends Item {
    public DowsingRod(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide() || hand == InteractionHand.OFF_HAND) {
            return super.use(level, player, hand);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return super.use(level, player, hand);
        }

        if (UseState.isActive(serverPlayer)) {
            UseState.clear(serverPlayer);
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }

        if (UseState.isOnCooldown(serverPlayer)) {
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }

        Rite activeRite = RiteLocator.findActiveRite(serverPlayer);
        if (activeRite != null) {
            BlockPos target = activeRite.getGuidanceTarget(serverPlayer);
            if (target != null) {
                UseState.activate(serverPlayer, target);
            } else {
                UseState.clear(serverPlayer);
            }
        } else {
            findNearestAltar(serverPlayer);
        }

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }


    @Override
    public void onInventoryTick(ItemStack pItemStack, Level pLevel, Player pPlayer, int pSlotIndex, int pSelectedIndex) {
        super.onInventoryTick(pItemStack, pLevel, pPlayer, pSlotIndex, pSelectedIndex);

        if (pLevel.isClientSide() || !(pPlayer instanceof ServerPlayer serverPlayer)) {
            return;
        }

        UseState.clearIfInactiveItem(serverPlayer);
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
            UseState.activate(serverPlayer, bestAltar);
        } else {
            serverPlayer.sendSystemMessage(Component.literal("No altar found nearby."));
            UseState.clear(serverPlayer);
        }
    }

    public static final class UseState {
        private static final int USE_COOLDOWN_TICKS = 5;
        private static final Set<UUID> ACTIVE_PLAYERS = new HashSet<>();
        private static final Map<UUID, Long> NEXT_USE_TICK = new HashMap<>();

        private UseState() {
        }

        public static boolean isActive(ServerPlayer player) {
            return ACTIVE_PLAYERS.contains(player.getUUID());
        }

        public static boolean isOnCooldown(ServerPlayer player) {
            return player.level().getGameTime() < NEXT_USE_TICK.getOrDefault(player.getUUID(), 0L);
        }

        public static void activate(ServerPlayer player, BlockPos target) {
            if (target == null) {
                clear(player);
                return;
            }

            ACTIVE_PLAYERS.add(player.getUUID());
            putOnCooldown(player);
            sendTarget(player, target);
        }

        public static void updateActiveTarget(ServerPlayer player, BlockPos target) {
            if (!isActive(player)) {
                return;
            }

            if (target == null) {
                clear(player);
                return;
            }

            sendTarget(player, target);
        }

        public static void clear(ServerPlayer player) {
            if (!ACTIVE_PLAYERS.remove(player.getUUID())) {
                return;
            }

            putOnCooldown(player);
            ModNetworking.sendToPlayer(new DowsingRodInfoPacketS2C(false, 0, 0, 0, 0), player);
        }

        public static void clearIfInactiveItem(ServerPlayer player) {
            if (isActive(player) && !(player.getMainHandItem().getItem() instanceof DowsingRod)) {
                clear(player);
            }
        }

        public static void clearWithoutPacket(ServerPlayer player) {
            ACTIVE_PLAYERS.remove(player.getUUID());
            NEXT_USE_TICK.remove(player.getUUID());
        }

        private static void sendTarget(ServerPlayer player, BlockPos target) {
            ModNetworking.sendToPlayer(new DowsingRodInfoPacketS2C(
                    true,
                    System.currentTimeMillis(),
                    target.getX(),
                    target.getY(),
                    target.getZ()
            ), player);
        }

        private static void putOnCooldown(ServerPlayer player) {
            NEXT_USE_TICK.put(player.getUUID(), player.level().getGameTime() + USE_COOLDOWN_TICKS);
        }
    }
}


