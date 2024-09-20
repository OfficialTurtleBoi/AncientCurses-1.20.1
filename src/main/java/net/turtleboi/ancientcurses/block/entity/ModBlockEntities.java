package net.turtleboi.ancientcurses.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.block.ModBlocks;

public class ModBlockEntities{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AncientCurses.MOD_ID);

    public static final RegistryObject<BlockEntityType<CursedAltarBlockEntity>> CURSED_ALTAR_BE =
            BLOCK_ENTITIES.register("cursed_altar_be", () ->
                    BlockEntityType.Builder.of(CursedAltarBlockEntity::new,
                            ModBlocks.CURSED_ALTAR.get()).build(null));

    public static final RegistryObject<BlockEntityType<LapidaristTableBlockEntity>> LAPIDARIST_TABLE_BE =
            BLOCK_ENTITIES.register("lapidarist_table_be", () ->
                    BlockEntityType.Builder.of(LapidaristTableBlockEntity::new,
                            ModBlocks.LAPIDARIST_TABLE.get()).build(null));

    public static void register(IEventBus eventBus){
        BLOCK_ENTITIES.register(eventBus);
    }
}
