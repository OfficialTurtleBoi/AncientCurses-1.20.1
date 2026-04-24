package net.turtleboi.ancientcurses.worldgen.structures;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.turtleboi.ancientcurses.AncientCurses;

import java.util.List;

public class ModStructureSets {
    public static final ResourceKey<StructureSet> CURSED_ALTAR =
            ResourceKey.create(Registries.STRUCTURE_SET, new ResourceLocation(AncientCurses.MOD_ID, "cursed_altar"));

    public static void bootstrap(BootstapContext<StructureSet> ctx) {
        var structures = ctx.lookup(Registries.STRUCTURE);

        Holder<Structure> cursedAltar = structures.getOrThrow(ModStructures.CURSED_ALTAR);

        StructureSet set = new StructureSet(
                List.of(new StructureSet.StructureSelectionEntry(cursedAltar, 1)),
                new RandomSpreadStructurePlacement(
                        18,
                        6,
                        RandomSpreadType.LINEAR,
                        16_421_364
                )
        );

        ctx.register(CURSED_ALTAR, set);
    }
}
