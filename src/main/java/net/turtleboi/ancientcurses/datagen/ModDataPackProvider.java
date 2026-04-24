package net.turtleboi.ancientcurses.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.registries.ForgeRegistries;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.worldgen.ModBiomeModifiers;
import net.turtleboi.ancientcurses.worldgen.ModConfiguredFeatures;
import net.turtleboi.ancientcurses.worldgen.ModPlacedFeatures;
import net.turtleboi.ancientcurses.worldgen.structures.ModStructurePools;
import net.turtleboi.ancientcurses.worldgen.structures.ModStructureSets;
import net.turtleboi.ancientcurses.worldgen.structures.ModStructures;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModDataPackProvider extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap)
            .add(ForgeRegistries.Keys.BIOME_MODIFIERS, ModBiomeModifiers::bootstrap)
            .add(Registries.TEMPLATE_POOL, ModStructurePools::bootstrap)
            .add(Registries.STRUCTURE, ModStructures::bootstrap)
            .add(Registries.STRUCTURE_SET, ModStructureSets::bootstrap);

    public ModDataPackProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(AncientCurses.MOD_ID));
    }
}
