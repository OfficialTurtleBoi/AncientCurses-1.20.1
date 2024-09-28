package net.turtleboi.ancientcurses.block.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.turtleboi.ancientcurses.block.ModBlocks;

public class SconcedSoulTorchBlock extends TorchBlock {
    public SconcedSoulTorchBlock(Properties pProperties, ParticleOptions pFlameParticle) {
        super(pProperties, pFlameParticle);
    }
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos blockPos,
                                 Player player, InteractionHand hand, BlockHitResult result) {
        ItemStack heldItem = player.getItemInHand(hand);


        if (heldItem.isEmpty()) {

            if (hand == InteractionHand.MAIN_HAND && player.getAbilities().mayBuild) {
                spawnParticles(level, blockPos);
                level.playSound((Player) null, blockPos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                Block newBlock = ModBlocks.SCONCED_UNLIT_SOUL_TORCH.get();
                BlockState newState = newBlock.defaultBlockState();

                level.setBlock(blockPos, newState, 3);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return InteractionResult.PASS;
    }

    private void spawnParticles(LevelAccessor world, BlockPos pos) {
        for (int i = 0; i < 10; i++) {
            double d0 = (double)pos.getX() + world.getRandom().nextDouble()/10+0.5;
            double d1 = (double)pos.getY()+ 0.65 + world.getRandom().nextDouble()/10;
            double d2 = (double)pos.getZ() + world.getRandom().nextDouble()/10+0.5;

            world.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }
    }
}
