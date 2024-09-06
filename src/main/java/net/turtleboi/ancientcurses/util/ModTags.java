package net.turtleboi.ancientcurses.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.turtleboi.ancientcurses.AncientCurses;

public class ModTags {
    public static class Blocks {
        private static TagKey<Block> tag(String name){
            return BlockTags.create(new ResourceLocation(AncientCurses.MOD_ID, name));
        }
    }

    public static class Items {
        public static final TagKey<Item> PRECIOUS_GEMS = tag("precious_gems");

        private static TagKey<Item> tag(String name){
            return ItemTags.create(new ResourceLocation(AncientCurses.MOD_ID, name));
        }
    }
}
