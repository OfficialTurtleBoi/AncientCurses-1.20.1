package net.turtleboi.ancientcurses.block.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            ItemStack heldItem = player.getItemInHand(hand);

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CursedAltarBlockEntity altarEntity) {
                if (heldItem.is(Items.DIAMOND) || heldItem.is(Items.EMERALD) || heldItem.is(Items.AMETHYST_SHARD)) {
                    if (altarEntity.getHoveringItem().isEmpty()) {
                        // Place the item on the altar
                        altarEntity.setHoveringItem(heldItem.copy()); // Store a copy of the item
                        heldItem.shrink(1); // Remove one item from the player's hand
                        return InteractionResult.SUCCESS;
                    }
                } else {
                    // Return the hovering item to the player if one is on the altar
                    if (!altarEntity.getHoveringItem().isEmpty() && player.isShiftKeyDown()) {
                        player.addItem(altarEntity.getHoveringItem());
                        altarEntity.setHoveringItem(ItemStack.EMPTY); // Clear the altar's hovering item
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }

        return InteractionResult.PASS; // Allow other interactions to happen
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
