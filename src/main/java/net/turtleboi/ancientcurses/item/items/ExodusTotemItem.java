package net.turtleboi.ancientcurses.item.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.turtleboi.turtlecore.capabilities.party.PlayerPartyProvider;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ExodusTotemItem extends Item {
    private static final String DESTINATION_TAG = "ExodusDestination";
    private static final String DESTINATION_DIMENSION_TAG = "Dimension";
    private static final String DESTINATION_X_TAG = "X";
    private static final String DESTINATION_Y_TAG = "Y";
    private static final String DESTINATION_Z_TAG = "Z";
    private static final int CHANNEL_TICKS = 80;
    private static final int MAX_USE_TICKS = 72000;
    private static final int COOLDOWN_TICKS = 1200;
    private static final double PARTY_RADIUS = 8.0D;

    public ExodusTotemItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!hasDestination(stack)) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.translatable("item.ancientcurses.exodus_totem.unbound")
                        .withStyle(ChatFormatting.DARK_GRAY), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        if (!level.isClientSide()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 0.9F, 0.75F);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        return stack;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            int chargeTicks = getUseDuration(stack) - timeLeft;
            if (chargeTicks >= CHANNEL_TICKS) {
                if (teleportPartyToDestination(stack, player)) {
                    player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
                }
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return MAX_USE_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag destination = stack.getTagElement(DESTINATION_TAG);
        if (destination == null) {
            tooltip.add(Component.translatable("item.ancientcurses.exodus_totem.unbound")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        tooltip.add(Component.translatable("item.ancientcurses.exodus_totem.bound",
                destination.getInt(DESTINATION_X_TAG),
                destination.getInt(DESTINATION_Y_TAG),
                destination.getInt(DESTINATION_Z_TAG))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    public static boolean bindToLodestone(Player player, InteractionHand hand, BlockState state, BlockPos pos) {
        if (!player.isShiftKeyDown() || !state.is(Blocks.LODESTONE)) {
            return false;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof ExodusTotemItem)) {
            return false;
        }

        if (!player.level().isClientSide()) {
            if (isBoundTo(stack, player.level(), pos)) {
                return true;
            }

            CompoundTag destination = new CompoundTag();
            destination.putString(DESTINATION_DIMENSION_TAG, player.level().dimension().location().toString());
            destination.putInt(DESTINATION_X_TAG, pos.getX());
            destination.putInt(DESTINATION_Y_TAG, pos.getY() + 1);
            destination.putInt(DESTINATION_Z_TAG, pos.getZ());
            stack.getOrCreateTag().put(DESTINATION_TAG, destination);
            player.level().playSound(null, pos, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.displayClientMessage(Component.translatable("item.ancientcurses.exodus_totem.bound_message")
                    .withStyle(ChatFormatting.LIGHT_PURPLE), true);
        }

        return true;
    }

    private static boolean isBoundTo(ItemStack stack, Level level, BlockPos pos) {
        CompoundTag destination = stack.getTagElement(DESTINATION_TAG);
        if (destination == null) {
            return false;
        }

        return destination.getString(DESTINATION_DIMENSION_TAG).equals(level.dimension().location().toString())
                && destination.getInt(DESTINATION_X_TAG) == pos.getX()
                && destination.getInt(DESTINATION_Y_TAG) == pos.getY() + 1
                && destination.getInt(DESTINATION_Z_TAG) == pos.getZ();
    }

    public static void cancelChannel(Player player) {
        if (player.isUsingItem() && player.getUseItem().getItem() instanceof ExodusTotemItem) {
            player.stopUsingItem();
        }
    }

    private static boolean hasDestination(ItemStack stack) {
        return stack.getTagElement(DESTINATION_TAG) != null;
    }

    private static boolean teleportPartyToDestination(ItemStack stack, ServerPlayer player) {
        CompoundTag destination = stack.getTagElement(DESTINATION_TAG);
        if (destination == null) {
            return false;
        }

        MinecraftServer server = player.getServer();
        ResourceLocation dimensionLocation = ResourceLocation.tryParse(destination.getString(DESTINATION_DIMENSION_TAG));
        if (server == null || dimensionLocation == null) {
            return false;
        }

        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimensionLocation);
        ServerLevel targetLevel = server.getLevel(dimension);
        if (targetLevel == null) {
            return false;
        }

        double x = destination.getInt(DESTINATION_X_TAG) + 0.5D;
        double y = destination.getInt(DESTINATION_Y_TAG);
        double z = destination.getInt(DESTINATION_Z_TAG) + 0.5D;
        for (ServerPlayer targetPlayer : getTeleportTargets(player)) {
            targetPlayer.teleportTo(targetLevel, x, y, z, targetPlayer.getYRot(), targetPlayer.getXRot());
            targetLevel.playSound(null, BlockPos.containing(x, y, z),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.9F, 0.75F);
        }

        return true;
    }

    private static Set<ServerPlayer> getTeleportTargets(ServerPlayer player) {
        Set<ServerPlayer> targets = new HashSet<>();
        targets.add(player);
        AABB area = player.getBoundingBox().inflate(PARTY_RADIUS);
        MinecraftServer server = player.getServer();
        if (server == null) {
            return targets;
        }

        player.getCapability(PlayerPartyProvider.PLAYER_PARTY).ifPresent(party -> {
            for (UUID memberUUID : party.getMemberUUIDs()) {
                ServerPlayer partyMember = server.getPlayerList().getPlayer(memberUUID);
                if (partyMember != null && partyMember.level() == player.level() && area.contains(partyMember.position())) {
                    targets.add(partyMember);
                }
            }
        });

        return targets;
    }
}
