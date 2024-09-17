package net.turtleboi.ancientcurses.block.blockentity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.block.entity.ModBlockEntities;
import net.turtleboi.ancientcurses.trials.PlayerTrialData;
import net.turtleboi.ancientcurses.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CursedAltarBlock extends BaseEntityBlock {
    public static final VoxelShape SHAPE = BaseEntityBlock.box(0, 0, 0, 16, 12, 16);
    public static final List<BlockPos> SOUL_TORCH_OFFSETS = BlockPos.betweenClosedStream(-2, 0, -2, 2, 1, 2).filter((p_207914_) ->
            Math.abs(p_207914_.getX()) == 2 || Math.abs(p_207914_.getZ()) == 2).map(BlockPos::immutable).toList();

    public CursedAltarBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (!level.isClientSide()) {
            return;
        }

        for (BlockPos offset : SOUL_TORCH_OFFSETS) {
            BlockPos torchPos = pos.offset(offset);
            BlockState torchState = level.getBlockState(torchPos);
            if (random.nextInt(16) == 0 && isSoulTorch(level, torchPos)) {
                double particleX, particleY, particleZ;

                if (torchState.is(Blocks.SOUL_WALL_TORCH)) {
                    Direction direction = torchState.getValue(WallTorchBlock.FACING);
                    Direction oppositeDirection = direction.getOpposite();
                    particleX = torchPos.getX() + 0.5D + 0.27D * oppositeDirection.getStepX();
                    particleY = torchPos.getY() + 0.7D;
                    particleZ = torchPos.getZ() + 0.5D + 0.27D * oppositeDirection.getStepZ();
                } else {
                    particleX = torchPos.getX() + 0.5D;
                    particleY = torchPos.getY() + 0.7D;
                    particleZ = torchPos.getZ() + 0.5D;
                }

                double altarX = pos.getX() + 0.5D;
                double altarY = pos.getY() + 1.0D;
                double altarZ = pos.getZ() + 0.5D;

                double velocityX = (altarX - particleX) * 0.05;
                double velocityY = (altarY - particleY) * 0.05;
                double velocityZ = (altarZ - particleZ) * 0.05;

                level.addParticle(ParticleTypes.SCULK_SOUL, particleX, particleY, particleZ, velocityX, velocityY, velocityZ);
            }
        }
    }

    private boolean isSoulTorch(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.SOUL_TORCH) || level.getBlockState(pos).is(Blocks.SOUL_WALL_TORCH);
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

                for (Player player : level.players()) {
                    BlockPos playerAltarPos = PlayerTrialData.getAltarPos(player);
                    if (playerAltarPos != null && playerAltarPos.equals(pos)) {
                        PlayerTrialData.resetAltarAtPos(player, playerAltarPos);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof CursedAltarBlockEntity altarEntity) {
                if (PlayerTrialData.isPlayerCursed(player) && altarEntity.canPlayerUse(player)) {
                    player.sendSystemMessage(Component.literal("You are already cursed! Complete your trial before interacting again.").withStyle(ChatFormatting.RED));
                    return InteractionResult.FAIL;
                }

                if (altarEntity.hasPlayerCompletedTrial(player)){
                    player.sendSystemMessage(Component.literal("You've completed this trial!").withStyle(ChatFormatting.GREEN));
                    if (player.isShiftKeyDown() && altarEntity.canPlayerUse(player)) {
                        for (int i = 2; i >= 0; i--) {
                            ItemStack gem = altarEntity.getGemInSlot(i);
                            if (!gem.isEmpty()) {
                                if (player.getInventory().add(gem)) {
                                    altarEntity.setGemInSlot(i, ItemStack.EMPTY);
                                    altarEntity.setChanged();
                                } else {
                                    ejectItemsTowardPlayer(altarEntity, level, pos, player);
                                }
                                altarEntity.setPlayerCooldown(player);
                                return InteractionResult.SUCCESS;
                            }
                        }
                    } else {
                        ItemStack heldItem = player.getItemInHand(hand);
                        if (isPreciousGem(heldItem)) {
                            for (int i = 0; i < 3; i++) {
                                if (altarEntity.getGemInSlot(i).isEmpty()) {
                                    altarEntity.setGemInSlot(i, heldItem.split(1));
                                    altarEntity.setChanged();
                                    return InteractionResult.SUCCESS;
                                }
                            }
                        }
                    }
                } else {
                    if (!altarEntity.hasPlayerCompletedTrial(player) && altarEntity.canPlayerUse(player)) {
                        startTrial(player, altarEntity);
                        altarEntity.setPlayerCooldown(player);
                        return InteractionResult.SUCCESS;
                    } else if (!altarEntity.hasPlayerCompletedTrial(player) && !altarEntity.canPlayerUse(player)){
                        //player.sendSystemMessage(Component.literal("The altar is recharging.").withStyle(ChatFormatting.RED));
                        return InteractionResult.FAIL;
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    public void startTrial(Player player, CursedAltarBlockEntity altarEntity) {
        if (altarEntity.hasPlayerCompletedTrial(player)) {
            player.sendSystemMessage(Component.literal("You have already completed the trial for this altar.").withStyle(ChatFormatting.GREEN));
            return;
        }

        MobEffect randomCurse = CursedAltarBlockEntity.getRandomCurse();
        int randomAmplifier = CursedAltarBlockEntity.getRandomAmplifier(player);
        UUID playerUUID = player.getUUID();
        altarEntity.setPlayerTrialStatus(playerUUID, false);
        altarEntity.cursePlayer(player, randomCurse, randomAmplifier, altarEntity);
        player.displayClientMessage(Component.literal(
                "You have been cursed with " + randomCurse.getDisplayName().getString() + "!").withStyle(ChatFormatting.DARK_PURPLE), true); //debug code
    }

    private boolean isPreciousGem(ItemStack itemStack){
        return itemStack.is(ModTags.Items.PRECIOUS_GEMS);
    }

    private void ejectItemsTowardPlayer(CursedAltarBlockEntity altarEntity, Level level, BlockPos pos, Player player) {
        for (int i = 0; i < 3; i++) {
            ItemStack gem = altarEntity.getGemInSlot(i);
            if (!gem.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, gem.copy());
                double dX = player.getX() - pos.getX();
                double dY = (player.getY() + player.getEyeHeight()) - (pos.getY() + 1.5);
                double dZ = player.getZ() - pos.getZ();
                double velocityFactor = 0.1;
                itemEntity.setDeltaMovement(dX * velocityFactor, dY * velocityFactor, dZ * velocityFactor);

                level.addFreshEntity(itemEntity);

                altarEntity.setGemInSlot(i, ItemStack.EMPTY);
            }
        }
        altarEntity.setChanged();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CursedAltarBlockEntity(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide ? createTickerHelper(pBlockEntityType, ModBlockEntities.CURSED_ALTAR_BE.get(), CursedAltarBlockEntity::bookAnimationTick) : null;
    }

}
