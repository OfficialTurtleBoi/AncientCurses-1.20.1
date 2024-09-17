package net.turtleboi.ancientcurses.datagen.loot;

import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;
import net.turtleboi.ancientcurses.block.ModBlocks;
import net.turtleboi.ancientcurses.item.ModItems;

import java.util.Set;

public class ModBlockLootTables extends BlockLootSubProvider {
    public ModBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        this.dropSelf(ModBlocks.CURSED_ALTAR.get());
        this.add(ModBlocks.SCONCED_TORCH.get(),
                block -> createSingleItemTable(ModItems.SCONCED_TORCH_ITEM.get()));
        this.add(ModBlocks.SCONCED_WALL_TORCH.get(),
                block -> createSingleItemTable(ModItems.SCONCED_TORCH_ITEM.get()));
        this.add(ModBlocks.SCONCED_SOUL_TORCH.get(),
                block -> createSingleItemTable(ModItems.SCONCED_SOUL_TORCH_ITEM.get()));
        this.add(ModBlocks.SCONCED_WALL_SOUL_TORCH.get(),
                block -> createSingleItemTable(ModItems.SCONCED_SOUL_TORCH_ITEM.get()));
        this.add(ModBlocks.SCONCED_REDSTONE_TORCH.get(),
                block -> createSingleItemTable(ModItems.SCONCED_REDSTONE_TORCH_ITEM.get()));
        this.add(ModBlocks.SCONCED_WALL_REDSTONE_TORCH.get(),
                block -> createSingleItemTable(ModItems.SCONCED_REDSTONE_TORCH_ITEM.get()));
        this.add(ModBlocks.SCONCED_CURSED_TORCH.get(),
                block -> createSingleItemTable(ModItems.SCONCED_CURSED_TORCH_ITEM.get()));
        this.add(ModBlocks.SCONCED_WALL_CURSED_TORCH.get(),
                block -> createSingleItemTable(ModItems.SCONCED_CURSED_TORCH_ITEM.get()));
        this.add(ModBlocks.SCONCED_UNLIT_TORCH.get(),
                block -> createSingleItemTable(ModItems.SCONCED_UNLIT_TORCH_ITEM.get()));
        this.add(ModBlocks.SCONCED_WALL_UNLIT_TORCH.get(),
                block -> createSingleItemTable(ModItems.SCONCED_UNLIT_TORCH_ITEM.get()));
    }

    protected LootTable.Builder createCopperLikeOreDrops(Block pBlock, Item item) {
        return createSilkTouchDispatchTable(pBlock,
                this.applyExplosionDecay(pBlock,
                        LootItem.lootTableItem(item)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F)))
                                .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
