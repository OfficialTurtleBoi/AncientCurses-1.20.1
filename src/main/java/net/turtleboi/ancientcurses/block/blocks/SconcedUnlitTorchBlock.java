package net.turtleboi.ancientcurses.block.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SconcedUnlitTorchBlock extends TorchBlock {
    public SconcedUnlitTorchBlock(Properties pProperties, ParticleOptions pFlameParticle) {
        super(pProperties, pFlameParticle);
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {

    }
}