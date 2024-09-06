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
                .add(ModItems.PERFECT_AMETHYST.get());

        this.tag(ModTags.Items.PRECIOUS_GEMS)
                .add(ModItems.PERFECT_DIAMOND.get());

        this.tag(ModTags.Items.PRECIOUS_GEMS)
                .add(ModItems.PERFECT_EMERALD.get());

        this.tag(ModTags.Items.PRECIOUS_GEMS)
                .add(ModItems.PERFECT_RUBY.get());

        this.tag(ModTags.Items.PRECIOUS_GEMS)
                .add(ModItems.PERFECT_SAPPHIRE.get());

        this.tag(ModTags.Items.PRECIOUS_GEMS)
                .add(ModItems.PERFECT_TOPAZ.get());
    }
}