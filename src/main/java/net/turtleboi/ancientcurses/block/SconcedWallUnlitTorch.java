package net.turtleboi.ancientcurses.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SconcedWallUnlitTorch extends WallTorchBlock {
    public SconcedWallUnlitTorch(Properties pProperties, ParticleOptions pFlameParticle) {
        super(pProperties, pFlameParticle);
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {

    }
}
