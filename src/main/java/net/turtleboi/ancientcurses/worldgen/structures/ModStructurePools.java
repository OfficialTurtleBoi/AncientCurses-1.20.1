package net.turtleboi.ancientcurses.worldgen.structures;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.turtleboi.ancientcurses.AncientCurses;

import java.util.List;
import java.util.function.Function;

public class ModStructurePools {
    public static final ResourceKey<StructureTemplatePool> CURSED_ALTAR_START =
            ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(AncientCurses.MOD_ID, "cursed_altar/start_pool"));

    public static void bootstrap(BootstapContext<StructureTemplatePool> bootstapContext) {
        Holder<StructureTemplatePool> empty = bootstapContext.lookup(Registries.TEMPLATE_POOL).getOrThrow(Pools.EMPTY);
        List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>> entries =
                List.of(Pair.of(StructurePoolElement.single("ancientcurses:cursedaltar_template"), 1));

        bootstapContext.register(
                CURSED_ALTAR_START,
                new StructureTemplatePool(
                        empty,
                        entries,
                        StructureTemplatePool.Projection.RIGID)
        );
    }
}
