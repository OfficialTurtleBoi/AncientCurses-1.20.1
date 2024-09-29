package net.turtleboi.ancientcurses.block.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.turtleboi.ancientcurses.block.ModBlocks;

public class SconcedWallUnlitSoulTorchBlock extends WallTorchBlock {
    public SconcedWallUnlitSoulTorchBlock(Properties pProperties, ParticleOptions pFlameParticle) {
        super(pProperties, pFlameParticle);
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {

    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos blockPos,
                                 Player player, InteractionHand hand, BlockHitResult result) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.getItem() == Items.FLINT_AND_STEEL) {

            if (hand == InteractionHand.MAIN_HAND) {
                if (!player.getAbilities().instabuild) {
                    heldItem.hurtAndBreak(1, player, (entity) -> {
                        entity.broadcastBreakEvent(hand);
                    });
                }
                level.playSound((Player) null, blockPos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
                Block newBlock = ModBlocks.SCONCED_WALL_SOUL_TORCH.get();
                BlockState newState = newBlock.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING));

                level.setBlock(blockPos, newState, 3);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        } else {
            if (heldItem.getItem() == Items.FIRE_CHARGE) {
                if (hand == InteractionHand.MAIN_HAND) {
                    if (!player.getAbilities().instabuild) {
                        heldItem.shrink(1);
                    }
                    level.playSound((Player) null, blockPos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
                    Block newBlock = ModBlocks.SCONCED_WALL_SOUL_TORCH.get();
                    BlockState newState = newBlock.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING));

                    level.setBlock(blockPos, newState, 3);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
        return InteractionResult.PASS;
    }

    private void spawnParticles(LevelAccessor world, BlockPos pos, BlockState pstate) {
        for (int i = 0; i < 10; i++) {
            Direction $$4 = (Direction)pstate.getValue(FACING);
            double $$5 = (double)pos.getX() + 0.5;
            double $$6 = (double)pos.getY() + 0.7;
            double $$7 = (double)pos.getZ() + 0.5;
            double $$8 = 0.22;
            double $$9 = 0.27;
            Direction $$10 = $$4.getOpposite();

            world.addParticle(ParticleTypes.SMOKE, $$5 + 0.27 * (double)$$10.getStepX(), $$6 + 0.22, $$7 + 0.27 * (double)$$10.getStepZ(), 0.0D, 0.0D, 0.0D);
        }
    }
}