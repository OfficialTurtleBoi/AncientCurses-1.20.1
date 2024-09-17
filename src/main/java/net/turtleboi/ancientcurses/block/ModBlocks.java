package net.turtleboi.ancientcurses.block;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.block.blockentity.CursedAltarBlock;
import net.turtleboi.ancientcurses.item.ModItems;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, AncientCurses.MOD_ID);

    public static final RegistryObject<Block> CURSED_ALTAR = registerBlock("cursed_altar",
            () -> new CursedAltarBlock(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN).noOcclusion()));

    public static final RegistryObject<Block> SCONCED_TORCH = BLOCKS.register("sconced_torch",
            () -> new SconcedTorch(BlockBehaviour.Properties.copy(Blocks.TORCH), ParticleTypes.FLAME));

    public static final RegistryObject<Block> SCONCED_WALL_TORCH = BLOCKS.register("sconced_wall_torch",
            () -> new SconcedWallTorch(BlockBehaviour.Properties.copy(Blocks.WALL_TORCH), ParticleTypes.FLAME));

    public static final RegistryObject<Block> SCONCED_SOUL_TORCH = BLOCKS.register("sconced_soul_torch",
            () -> new SconcedSoulTorch(BlockBehaviour.Properties.copy(Blocks.SOUL_TORCH), ParticleTypes.SOUL_FIRE_FLAME));

    public static final RegistryObject<Block> SCONCED_WALL_SOUL_TORCH = BLOCKS.register("sconced_wall_soul_torch",
            () -> new SconcedWallSoulTorch(BlockBehaviour.Properties.copy(Blocks.SOUL_WALL_TORCH), ParticleTypes.SOUL_FIRE_FLAME));

    public static final RegistryObject<Block> SCONCED_REDSTONE_TORCH = BLOCKS.register("sconced_redstone_torch",
            () -> new SconcedRedstoneTorch(BlockBehaviour.Properties.copy(Blocks.REDSTONE_TORCH), ParticleTypes.ELECTRIC_SPARK));

    public static final RegistryObject<Block> SCONCED_WALL_REDSTONE_TORCH = BLOCKS.register("sconced_wall_redstone_torch",
            () -> new SconcedWallRedstoneTorch(BlockBehaviour.Properties.copy(Blocks.REDSTONE_TORCH), ParticleTypes.ELECTRIC_SPARK));

    public static final RegistryObject<Block> SCONCED_CURSED_TORCH = BLOCKS.register("sconced_cursed_torch",
            () -> new SconcedCursedTorch(BlockBehaviour.Properties.copy(Blocks.TORCH), ParticleTypes.SOUL_FIRE_FLAME));

    public static final RegistryObject<Block> SCONCED_WALL_CURSED_TORCH = BLOCKS.register("sconced_wall_cursed_torch",
            () -> new SconcedWallCursedTorch(BlockBehaviour.Properties.copy(Blocks.TORCH), ParticleTypes.SOUL_FIRE_FLAME));

    public static final RegistryObject<Block> SCONCED_UNLIT_TORCH = BLOCKS.register("sconced_unlit_torch",
            () -> new SconcedUnlitTorch(BlockBehaviour.Properties.copy(Blocks.TORCH), ParticleTypes.SOUL_FIRE_FLAME));

    public static final RegistryObject<Block> SCONCED_WALL_UNLIT_TORCH = BLOCKS.register("sconced_wall_unlit_torch",
            () -> new SconcedWallUnlitTorch(BlockBehaviour.Properties.copy(Blocks.TORCH), ParticleTypes.SOUL_FIRE_FLAME));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static <T extends Block> RegistryObject<T> registerBlockWithoutItem(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }


    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
