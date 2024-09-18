package net.turtleboi.ancientcurses.world.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CurseAltarStructure extends Structure {

    // Define the codec for the structure (helps serialize it into the game registry)
    public static final Codec<CurseAltarStructure> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    settingsCodec(instance)  // Include other parameters if necessary
            ).apply(instance, CurseAltarStructure::new)
    );
    //public static final Codec<CurseAltarStructure> CODEC = RecordCodecBuilder.<CurseAltarStructure>mapCodec(instance ->
    //        instance.group(
    //                Structure.settingsCodec(instance),
    //                StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
    //                ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName),
    //                Codec.intRange(0, 30).fieldOf("size").forGetter(structure -> structure.size),
    //                HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
    //                Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
    //                Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter)
    //        ).apply(instance, CurseAltarStructure::new)
    //).codec();

    public CurseAltarStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext pContext) {
        return Optional.empty();
    }

    //private final Holder<StructureTemplatePool> startPool;
    //private final Optional<ResourceLocation> startJigsawName;
    //private final int size;
    //private final HeightProvider startHeight;
    //private final Optional<Heightmap.Types> projectStartToHeightmap;
    //private final int maxDistanceFromCenter;

    //public CurseAltarStructure(Structure.StructureSettings config,
    //                           Holder<StructureTemplatePool> startPool,
    //                           Optional<ResourceLocation> startJigsawName,
    //                           int size,
    //                           HeightProvider startHeight,
    //                           Optional<Heightmap.Types> projectStartToHeightmap,
    //                           int maxDistanceFromCenter)
    //{
    //    super(config);
    //    this.startPool = startPool;
    //    this.startJigsawName = startJigsawName;
    //    this.size = size;
    //    this.startHeight = startHeight;
    //    this.projectStartToHeightmap = projectStartToHeightmap;
    //    this.maxDistanceFromCenter = maxDistanceFromCenter;
    //}

    // Extra conditions for spawning your structure
    private static boolean extraSpawningChecks(Structure.GenerationContext context) {
        // Example: restrict spawning to areas where the ground is not higher than Y level 100
        ChunkPos chunkPos = context.chunkPos();
        return context.chunkGenerator().getFirstOccupiedHeight(
                chunkPos.getMinBlockX(),
                chunkPos.getMinBlockZ(),
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                context.heightAccessor(),
                context.randomState()) < 100;
    }

    //@Override
    //public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
    //    // First check if extra spawning conditions are met
    //    if (!CurseAltarStructure.extraSpawningChecks(context)) {
    //        return Optional.empty();
    //    }
//
    //    // Get the Y position for structure spawning
    //    int startY = this.startHeight.sample(context.random(), new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
//
    //    // Convert chunk coordinates to actual block coordinates
    //    ChunkPos chunkPos = context.chunkPos();
    //    BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), startY, chunkPos.getMinBlockZ());
//
    //    // Set up the structure's pieces using the Jigsaw placement system
    //    Optional<Structure.GenerationStub> structurePiecesGenerator = JigsawPlacement.addPieces(
    //            context,
    //            this.startPool,
    //            this.startJigsawName,
    //            this.size,
    //            blockPos,
    //            false,
    //            this.projectStartToHeightmap,
    //            this.maxDistanceFromCenter
    //    );
//
    //    return structurePiecesGenerator;
    //}



    @Override
    public StructureType<?> type() {
        return ModStructures.CURSED_ALTAR.get();
    }
}
