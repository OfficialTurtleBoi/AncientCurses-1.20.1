package net.turtleboi.ancientcurses.block.blockentity;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
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
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.block.entity.ModBlockEntities;
import net.turtleboi.ancientcurses.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            ItemStack heldItem = player.getItemInHand(hand);
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof CursedAltarBlockEntity altarEntity) {
                // Check if player is holding shift to remove gems in reverse order
                if (player.isShiftKeyDown()) {
                    // Eject all gems currently in the altar
                    ejectItemsTowardPlayer(altarEntity, level, pos, player);
                    return InteractionResult.SUCCESS;
                } else {
                    // Try to place the gem in an empty slot if it's a valid gem
                    if (isPreciousGem(heldItem)) {
                        for (int i = 0; i < 3; i++) {
                            if (altarEntity.getGemInSlot(i).isEmpty()) {
                                altarEntity.setGemInSlot(i, heldItem.split(1)); // Place 1 item in the slot
                                altarEntity.setChanged(); // Mark the block entity as changed
                                return InteractionResult.SUCCESS;
                            }
                        }
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    private boolean isPreciousGem(ItemStack itemStack){
        return itemStack.is(ModTags.Items.PRECIOUS_GEMS);
    }

    private void ejectItemsTowardPlayer(CursedAltarBlockEntity altarEntity, Level level, BlockPos pos, Player player) {
        // Eject all gems in the altar
        for (int i = 0; i < 3; i++) {
            ItemStack gem = altarEntity.getGemInSlot(i);
            if (!gem.isEmpty()) {
                // Spawn the item entity above the altar
                ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, gem.copy());

                // Calculate velocity toward the player
                double dX = player.getX() - pos.getX();
                double dY = (player.getY() + player.getEyeHeight()) - (pos.getY() + 1.5); // Eye height for better accuracy
                double dZ = player.getZ() - pos.getZ();
                double velocityFactor = 0.1; // Tweak this value for desired speed
                itemEntity.setDeltaMovement(dX * velocityFactor, dY * velocityFactor, dZ * velocityFactor);

                // Add the item to the level
                level.addFreshEntity(itemEntity);

                // Clear the slot in the altar
                altarEntity.setGemInSlot(i, ItemStack.EMPTY);
            }
        }
        altarEntity.setChanged(); // Mark block entity as changed
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
