package net.turtleboi.ancientcurses.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.item.ModItems;
import net.turtleboi.ancientcurses.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagGenerator extends ItemTagsProvider {
    public ModItemTagGenerator(PackOutput p_275343_, CompletableFuture<HolderLookup.Provider> p_275729_,
                               CompletableFuture<TagLookup<Block>> p_275322_, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_275343_, p_275729_, p_275322_, AncientCurses.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        this.tag(ItemTags.MUSIC_DISCS)
                .add(ModItems.DEPRECOPHOBIA_MUSIC_DISC.get());

        this.tag(ModTags.Items.PRECIOUS_GEMS)
                .add(ModItems.BROKEN_AMETHYST.get())
                .add(ModItems.BROKEN_DIAMOND.get())
                .add(ModItems.BROKEN_EMERALD.get())
                .add(ModItems.BROKEN_RUBY.get())
                .add(ModItems.BROKEN_SAPPHIRE.get())
                .add(ModItems.BROKEN_TOPAZ.get())
                .add(ModItems.POLISHED_AMETHYST.get())
                .add(ModItems.POLISHED_DIAMOND.get())
                .add(ModItems.POLISHED_EMERALD.get())
                .add(ModItems.POLISHED_RUBY.get())
                .add(ModItems.POLISHED_SAPPHIRE.get())
                .add(ModItems.POLISHED_TOPAZ.get())
                .add(ModItems.PERFECT_AMETHYST.get())
                .add(ModItems.PERFECT_DIAMOND.get())
                .add(ModItems.PERFECT_EMERALD.get())
                .add(ModItems.PERFECT_RUBY.get())
                .add(ModItems.PERFECT_SAPPHIRE.get())
                .add(ModItems.PERFECT_TOPAZ.get());

        this.tag(ModTags.Items.MINOR_GEMS)
                .add(ModItems.BROKEN_AMETHYST.get())
                .add(ModItems.BROKEN_DIAMOND.get())
                .add(ModItems.BROKEN_EMERALD.get())
                .add(ModItems.BROKEN_RUBY.get())
                .add(ModItems.BROKEN_SAPPHIRE.get())
                .add(ModItems.BROKEN_TOPAZ.get());
    }
}
