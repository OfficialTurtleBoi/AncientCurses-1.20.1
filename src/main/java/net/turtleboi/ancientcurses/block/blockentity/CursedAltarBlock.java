package net.turtleboi.ancientcurses.block.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.server.level.ServerLevel;
import net.turtleboi.ancientcurses.block.altar.EnvironmentUtil;
import net.turtleboi.ancientcurses.block.altar.GemUtil;
import net.turtleboi.ancientcurses.block.altar.RitualUtil;
import net.turtleboi.ancientcurses.block.altar.RewardUtil;
import net.turtleboi.ancientcurses.block.altar.RiteUtil;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.block.entity.ModBlockEntities;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteProvider;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;
import net.turtleboi.ancientcurses.rite.Rite;
import net.turtleboi.ancientcurses.rite.util.RiteLocator;
import net.turtleboi.ancientcurses.util.AltarSavedData;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CursedAltarBlock extends BaseEntityBlock {
    public static final VoxelShape SHAPE = BaseEntityBlock.box(0, 0, 0, 16, 12, 16);

    private static InteractionResult consumeInteraction(Level level) {
        return level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    public CursedAltarBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        EnvironmentUtil.animateSoulLights(level, pos, random);

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof CursedAltarBlockEntity altarEntity
                && (RitualUtil.hasCompleteRecipe(altarEntity) || GemUtil.hasCompleteGemFusionRecipe(altarEntity))) {
            for (int i = 0; i < 2; i++) {
                double angle = random.nextDouble() * (Math.PI * 2);
                double radius = 0.15D + random.nextDouble() * 0.2D;
                level.addParticle(
                        ModParticleTypes.CURSED_FLAME_PARTICLE.get(),
                        pos.getX() + 0.5D + Math.cos(angle) * radius,
                        pos.getY() + 1.05D + random.nextDouble() * 0.2D,
                        pos.getZ() + 0.5D + Math.sin(angle) * radius,
                        0.0D,
                        0.01D + random.nextDouble() * 0.02D,
                        0.0D
                );
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.is(newState.getBlock())) {
            super.onRemove(state, level, pos, newState, isMoving);
            return;
        }

        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CursedAltarBlockEntity altarEntity) {
                for (int i = 0; i < altarEntity.itemStackHandler.getSlots(); i++) {
                    ItemStack stack = altarEntity.itemStackHandler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack.copy());
                        altarEntity.itemStackHandler.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }

                for (int i = 0; i < altarEntity.ritualStackHandler.getSlots(); i++) {
                    ItemStack stack = altarEntity.ritualStackHandler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack.copy());
                        altarEntity.ritualStackHandler.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }

                altarEntity.releaseDimensionActive();
            }

            if (level instanceof ServerLevel serverLevel) {
                AltarSavedData.get(serverLevel).removeAltar(pos);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof CursedAltarBlockEntity altarEntity)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (altarEntity.isAnimating() && !altarEntity.hasPendingGemFusion()) {
            return InteractionResult.FAIL;
        }

        Rite altarRite = altarEntity.getPlayerRite(player.getUUID());
        boolean riteCompletedAtAltar = altarRite != null && altarRite.isRiteCompleted(player);
        if (riteCompletedAtAltar && !altarEntity.hasPlayerCompletedRite(player)) {
            altarEntity.setPlayerRiteCompleted(player);
        }

        InteractionResult playerRiteDataResult = player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA)
                .map(riteData -> {
                    if (riteData.isPlayerCursed()) {
                        Rite playerRite = RiteLocator.resolveActiveRite(player, riteData);
                        if (riteCompletedAtAltar || altarEntity.hasPlayerCompletedRite(player)) {
                            return InteractionResult.PASS;
                        }
                        if (playerRite == null) {
                            return InteractionResult.FAIL;
                        }
                        if (playerRite.isRiteCompleted(player) || altarEntity.hasPlayerCompletedRite(player)) {
                            return InteractionResult.PASS;
                        }
                        if (RiteUtil.tryConcludeActiveRite(player, playerRite)) {
                            return InteractionResult.SUCCESS;
                        }
                        return InteractionResult.FAIL;
                        //player.sendSystemMessage(Component.literal("You are already cursed! Complete your rite before interacting again.").withStyle(ChatFormatting.RED));
                    }

                    if (riteData.getCurrentAltarPos() != null &&
                            !Objects.equals(riteData.getCurrentAltarPos(), altarEntity.getBlockPos())) {
                        // player.sendSystemMessage(Component.literal("You have an altar! Complete your rite before interacting again.").withStyle(ChatFormatting.RED));
                        return InteractionResult.FAIL;
                    }

                    return InteractionResult.PASS;
                }).orElse(InteractionResult.PASS);

        if (playerRiteDataResult == InteractionResult.FAIL) {
            return InteractionResult.FAIL;
        }

        if (altarEntity.hasPlayerCompletedRite(player) || riteCompletedAtAltar){
            RewardUtil.rewardPlayer(player, altarEntity, level, pos);
            if (altarEntity.hasPendingGemFusion()) {
                return consumeInteraction(level);
            }
            //player.sendSystemMessage(Component.literal("You've completed this rite!").withStyle(ChatFormatting.GREEN));
            if (player.isShiftKeyDown() && altarEntity.canPlayerUse(player)) {
                for (int i = altarEntity.ritualStackHandler.getSlots() - 1; i >= 0; i--) {
                    ItemStack ritualItem = altarEntity.getRitualItemInSlot(i);
                    if (!ritualItem.isEmpty()) {
                        if (player.getInventory().add(ritualItem)) {
                            altarEntity.setRitualItemInSlot(i, ItemStack.EMPTY);
                            altarEntity.setChanged();
                        } else {
                            RewardUtil.ejectItemsTowardPlayer(level, pos, player, Collections.singletonList(ritualItem));
                        }
                        GemUtil.playGemPlaceSound(player.level(), player.blockPosition(), i);
                        return consumeInteraction(level);
                    }
                }

                for (int i = 2; i >= 0; i--) {
                    ItemStack gem = altarEntity.getGemInSlot(i);
                    if (!gem.isEmpty()) {
                        if (player.getInventory().add(gem)) {
                            altarEntity.setGemInSlot(i, ItemStack.EMPTY);
                            altarEntity.setChanged();
                        } else {
                            RewardUtil.ejectItemsTowardPlayer(level, pos, player, Collections.singletonList(gem));
                        }
                        GemUtil.playGemPlaceSound(player.level(), player.blockPosition(), i);
                        return consumeInteraction(level);
                    }
                }
            } else if (altarEntity.canPlayerUse(player)){
                //player.sendSystemMessage(Component.literal("Right click!"));
                ItemStack heldItem = player.getItemInHand(hand);

                if (RitualUtil.isRitualItem(altarEntity, player, heldItem)) {
                    int ritualSlot = RitualUtil.getNextRitualSlot(altarEntity, player, heldItem);
                    if (ritualSlot != -1) {
                        GemUtil.playGemPlaceSound(player.level(), player.blockPosition(), ritualSlot);
                        ItemStack itemToPlace = player.getAbilities().instabuild ? heldItem.copyWithCount(1) : heldItem.split(1);
                        altarEntity.setRitualItemInSlot(ritualSlot, itemToPlace);
                        altarEntity.setChanged();
                        level.sendBlockUpdated(pos, state, state, 3);
                        return consumeInteraction(level);
                    }
                }

                if (GemUtil.isGemFusionIngredient(altarEntity, heldItem)) {
                    int ritualSlot = GemUtil.getNextGemFusionSlot(altarEntity, heldItem);
                    if (ritualSlot != -1) {
                        GemUtil.playGemPlaceSound(player.level(), player.blockPosition(), ritualSlot);
                        ItemStack gemToPlace = player.getAbilities().instabuild ? heldItem.copyWithCount(1) : heldItem.split(1);
                        altarEntity.setRitualItemInSlot(ritualSlot, gemToPlace);
                        altarEntity.setChanged();
                        level.sendBlockUpdated(pos, state, state, 3);
                        return consumeInteraction(level);
                    }
                }

                if (GemUtil.hasCompleteGemFusionRecipe(altarEntity)) {
                    if (GemUtil.tryStartGemFusion(altarEntity, player)) {
                        level.sendBlockUpdated(pos, state, state, 3);
                        return consumeInteraction(level);
                    }
                }

                if (RitualUtil.matchesRitual(altarEntity, player)) {
                    if (RitualUtil.tryStartMatchingRitual(player, altarEntity)) {
                        return consumeInteraction(level);
                    }
                }

            } else {
                //.sendSystemMessage(Component.literal("Too bad so sad!"));
            }
        } else {
            if (!altarEntity.hasPlayerCompletedRite(player) && altarEntity.canPlayerUse(player)) {
                EnvironmentUtil.convertSoulLights(altarEntity, level);
                RiteUtil.startRite(player, altarEntity);
                return consumeInteraction(level);
            } else if (!altarEntity.hasPlayerCompletedRite(player) && !altarEntity.canPlayerUse(player)){
                //player.sendSystemMessage(Component.literal("The altar is recharging.").withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.PASS;
    }

    public static int SoulTorchAround(BlockGetter world, BlockPos centerPos ) {
        return EnvironmentUtil.countSoulTorchesAround(world, centerPos);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CursedAltarBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide) {
            return createTickerHelper(pBlockEntityType, ModBlockEntities.CURSED_ALTAR_BE.get(), CursedAltarBlockEntity::bookAnimationTick);
        } else {
            return createTickerHelper(pBlockEntityType, ModBlockEntities.CURSED_ALTAR_BE.get(), CursedAltarBlockEntity::serverTick);
        }
    }
}
