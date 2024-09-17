package net.turtleboi.ancientcurses.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.turtleboi.ancientcurses.block.ModBlocks;

public class SconcedUnlitTorchItem extends BlockItem {
    public SconcedUnlitTorchItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        if (clickedFace.getAxis().isHorizontal()) {
            return ModBlocks.SCONCED_WALL_UNLIT_TORCH.get().defaultBlockState().setValue(WallTorchBlock.FACING, clickedFace);
        }
        return ModBlocks.SCONCED_UNLIT_TORCH.get().defaultBlockState();
    }
}
