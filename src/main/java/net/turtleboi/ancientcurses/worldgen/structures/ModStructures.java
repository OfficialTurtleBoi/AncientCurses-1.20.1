package net.turtleboi.ancientcurses.worldgen.structures;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.worldgen.structures.custom.CurseAltarStructure;

import java.util.Map;
import java.util.Optional;

public class ModStructures {
    public static final ResourceKey<Structure> CURSED_ALTAR =
            ResourceKey.create(Registries.STRUCTURE, new ResourceLocation(AncientCurses.MOD_ID, "cursed_altar"));

    public static void bootstrap(BootstapContext<Structure> bootstapContext) {
        var biomes = bootstapContext.lookup(Registries.BIOME);
        var pools  = bootstapContext.lookup(Registries.TEMPLATE_POOL);

        // biomes: "#ancientcurses:has_structure/cursed_altar"
        TagKey<Biome> tag = TagKey.create(Registries.BIOME, new ResourceLocation(AncientCurses.MOD_ID, "has_structure/cursed_altar"));
        HolderSet<Biome> biomeSelector = biomes.getOrThrow(tag);
        Structure.StructureSettings settings = new Structure.StructureSettings(
                biomeSelector,
                Map.of(),
                GenerationStep.Decoration.UNDERGROUND_STRUCTURES,
                TerrainAdjustment.BEARD_BOX
        );

        var startPool = pools.getOrThrow(ModStructurePools.CURSED_ALTAR_START);
        HeightProvider startHeight = UniformHeight.of(VerticalAnchor.aboveBottom(5), VerticalAnchor.absolute(0));
        var projectTo = Optional.<Heightmap.Types>empty();
        var structure = new CurseAltarStructure(
                settings,
                startPool,
                Optional.empty(),
                1,
                startHeight,
                projectTo,
                80
        );

        bootstapContext.register(CURSED_ALTAR, structure);
    }
}
