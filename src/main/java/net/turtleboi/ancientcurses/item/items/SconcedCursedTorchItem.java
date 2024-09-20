package net.turtleboi.ancientcurses.item.items;

import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.turtleboi.ancientcurses.block.ModBlocks;

public class SconcedCursedTorchItem extends BlockItem {
    public SconcedCursedTorchItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        if (clickedFace.getAxis().isHorizontal()) {
            return ModBlocks.SCONCED_WALL_CURSED_TORCH.get().defaultBlockState().setValue(WallTorchBlock.FACING, clickedFace);
        }
        return ModBlocks.SCONCED_CURSED_TORCH.get().defaultBlockState();
    }
}