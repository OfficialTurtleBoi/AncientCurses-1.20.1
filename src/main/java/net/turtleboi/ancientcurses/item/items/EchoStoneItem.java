package net.turtleboi.ancientcurses.item.items;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EchoStoneItem extends Item {
    private static final String SNAPSHOT_TAG = "EchoSnapshot";
    private static final String SNAPSHOT_DIMENSION_TAG = "Dimension";
    private static final String SNAPSHOT_X_TAG = "X";
    private static final String SNAPSHOT_Y_TAG = "Y";
    private static final String SNAPSHOT_Z_TAG = "Z";
    private static final String SNAPSHOT_HEALTH_TAG = "Health";
    private static final String LAST_SNAPSHOT_TICK_TAG = "LastSnapshotTick";
    private static final int SNAPSHOT_INTERVAL_TICKS = 200;
    private static final int COOLDOWN_TICKS = 500;

    public EchoStoneItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        if (!(player instanceof ServerPlayer serverPlayer) || player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        CompoundTag snapshot = stack.getTagElement(SNAPSHOT_TAG);
        if (snapshot == null) {
            player.displayClientMessage(Component.translatable("item.ancientcurses.echo_stone.no_snapshot")
                    .withStyle(ChatFormatting.DARK_GRAY), true);
            return InteractionResultHolder.fail(stack);
        }

        if (!rewindPlayer(serverPlayer, snapshot)) {
            player.displayClientMessage(Component.translatable("item.ancientcurses.echo_stone.no_snapshot")
                    .withStyle(ChatFormatting.DARK_GRAY), true);
            return InteractionResultHolder.fail(stack);
        }

        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.9F, 0.65F);
        saveSnapshot(stack, serverPlayer);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer player)) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        long gameTime = level.getGameTime();
        if (tag.contains(SNAPSHOT_TAG) && gameTime - tag.getLong(LAST_SNAPSHOT_TICK_TAG) < SNAPSHOT_INTERVAL_TICKS) {
            return;
        }

        tag.putLong(LAST_SNAPSHOT_TICK_TAG, gameTime);
        saveSnapshot(stack, player);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ancientcurses.echo_stone.tooltip")
                .withStyle(ChatFormatting.DARK_AQUA));
    }

    private static void saveSnapshot(ItemStack stack, ServerPlayer player) {
        CompoundTag snapshot = new CompoundTag();
        Vec3 position = player.position();
        snapshot.putString(SNAPSHOT_DIMENSION_TAG, player.level().dimension().location().toString());
        snapshot.putDouble(SNAPSHOT_X_TAG, position.x());
        snapshot.putDouble(SNAPSHOT_Y_TAG, position.y());
        snapshot.putDouble(SNAPSHOT_Z_TAG, position.z());
        snapshot.putFloat(SNAPSHOT_HEALTH_TAG, player.getHealth());
        stack.getOrCreateTag().put(SNAPSHOT_TAG, snapshot);
    }

    private static boolean rewindPlayer(ServerPlayer player, CompoundTag snapshot) {
        ResourceLocation dimensionLocation = ResourceLocation.tryParse(snapshot.getString(SNAPSHOT_DIMENSION_TAG));
        if (dimensionLocation == null) {
            return false;
        }

        MinecraftServer server = player.getServer();
        if (server == null) {
            return false;
        }

        ResourceKey<Level> dimensionKey = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimensionLocation);
        ServerLevel targetLevel = server.getLevel(dimensionKey);
        if (targetLevel == null) {
            return false;
        }

        player.teleportTo(targetLevel,
                snapshot.getDouble(SNAPSHOT_X_TAG),
                snapshot.getDouble(SNAPSHOT_Y_TAG),
                snapshot.getDouble(SNAPSHOT_Z_TAG),
                player.getYRot(),
                player.getXRot());
        player.setHealth(Math.min(player.getMaxHealth(), Math.max(1.0F, snapshot.getFloat(SNAPSHOT_HEALTH_TAG))));
        return true;
    }
}
