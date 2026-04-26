package net.turtleboi.ancientcurses.block.blocks;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.turtleboi.ancientcurses.block.ModBlocks;

public class SconcedWallRedstoneTorchBlock extends RedstoneWallTorchBlock {
    public SconcedWallRedstoneTorchBlock(Properties pProperties, ParticleOptions pFlameParticle) {
        super(pProperties);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return ModBlocks.SCONCED_REDSTONE_TORCH.get().asItem().getDefaultInstance();
    }
}
