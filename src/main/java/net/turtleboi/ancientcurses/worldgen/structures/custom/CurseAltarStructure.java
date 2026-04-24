package net.turtleboi.ancientcurses.worldgen.structures.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.turtleboi.ancientcurses.config.AncientCursesConfig;
import net.turtleboi.ancientcurses.worldgen.structures.ModStructureTypes;

import java.util.Optional;
import java.util.Random;

public class CurseAltarStructure extends Structure {
    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int size;
    private final HeightProvider startHeight;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;

    public static final Codec<CurseAltarStructure> CODEC = RecordCodecBuilder.<CurseAltarStructure>mapCodec(instance ->
            instance.group(
                    Structure.settingsCodec(instance),
                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
                    ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName),
                    Codec.intRange(0, 30).fieldOf("size").forGetter(structure -> structure.size),
                    HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
                    Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter)
            ).apply(instance, CurseAltarStructure::new)
    ).codec();

    public CurseAltarStructure(Structure.StructureSettings config,
                               Holder<StructureTemplatePool> startPool,
                               Optional<ResourceLocation> startJigsawName,
                               int size,
                               HeightProvider startHeight,
                               Optional<Heightmap.Types> projectStartToHeightmap,
                               int maxDistanceFromCenter)
    {
        super(config);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.size = size;
        this.startHeight = startHeight;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
    }

    private static boolean extraSpawningChecks(Structure.GenerationContext context) {
        double spawnChance = AncientCursesConfig.CURSED_ALTAR_SPAWN_CHANCE.get();
        if (spawnChance <= 0.0D) {
            return false;
        }
        if (spawnChance >= 1.0D) {
            return true;
        }

        long seed = context.seed();
        int chunkX = context.chunkPos().x;
        int chunkZ = context.chunkPos().z;
        Random random = new Random(seed + (chunkX * 341873128712L) + (chunkZ * 132897987541L));

        return !(random.nextDouble() >= spawnChance);
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        if (!CurseAltarStructure.extraSpawningChecks(context)) {
            return Optional.empty();
        }

        ChunkPos chunkPos = context.chunkPos();
        BlockPos surfaceCheckPos = getSurfacePlacementPos(context, chunkPos);
        BlockPos undergroundCheckPos = getUndergroundPlacementPos(context, chunkPos, surfaceCheckPos.getY());

        boolean surfaceValid = hasSurfaceClearance(context, surfaceCheckPos) && !nearFluid(context, surfaceCheckPos);
        boolean undergroundValid = hasUndergroundClearance(context, undergroundCheckPos, surfaceCheckPos.getY())
                && !nearFluid(context, undergroundCheckPos);

        BlockPos placementCheckPos = pickPlacementCheckPos(context, surfaceCheckPos, surfaceValid, undergroundCheckPos, undergroundValid);
        if (placementCheckPos == null) {
            return Optional.empty();
        }

        BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), placementCheckPos.getY(), chunkPos.getMinBlockZ());

        if (isBlacklistedBiome(context, placementCheckPos)) {
            return Optional.empty();
        }

        return JigsawPlacement.addPieces(
                context,
                this.startPool,
                this.startJigsawName,
                this.size,
                blockPos,
                false,
                this.projectStartToHeightmap,
                this.maxDistanceFromCenter
        );
    }

    private BlockPos getSurfacePlacementPos(Structure.GenerationContext generationContext, ChunkPos chunkPos) {
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();
        if (generationContext.randomState() == null) {
            return new BlockPos(centerX, this.startHeight.sample(generationContext.random(),
                    new WorldGenerationContext(generationContext.chunkGenerator(), generationContext.heightAccessor())), centerZ);
        }

        int surfaceY = generationContext.chunkGenerator().getFirstOccupiedHeight(
                centerX,
                centerZ,
                Heightmap.Types.WORLD_SURFACE_WG,
                generationContext.heightAccessor(),
                generationContext.randomState()
        );
        return new BlockPos(centerX, surfaceY, centerZ);
    }

    private BlockPos getUndergroundPlacementPos(Structure.GenerationContext generationContext, ChunkPos chunkPos, int surfaceY) {
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();
        int minBuildY = generationContext.heightAccessor().getMinBuildHeight() + 5;
        int maxUndergroundY = Math.max(minBuildY, surfaceY - 10);
        int minUndergroundY = Math.max(minBuildY, surfaceY - 36);

        if (maxUndergroundY <= minUndergroundY) {
            return new BlockPos(centerX, minUndergroundY, centerZ);
        }

        int undergroundY = minUndergroundY + generationContext.random().nextInt(maxUndergroundY - minUndergroundY + 1);
        return new BlockPos(centerX, undergroundY, centerZ);
    }

    private BlockPos pickPlacementCheckPos(Structure.GenerationContext context,
                                           BlockPos surfacePos,
                                           boolean surfaceValid,
                                           BlockPos undergroundPos,
                                           boolean undergroundValid) {
        if (surfaceValid && undergroundValid) {
            return context.random().nextDouble() < AncientCursesConfig.CURSED_ALTAR_SURFACE_BIAS.get()
                    ? surfacePos
                    : undergroundPos;
        }
        if (surfaceValid) {
            return surfacePos;
        }
        if (undergroundValid) {
            return undergroundPos;
        }
        return null;
    }

    private static boolean nearFluid(Structure.GenerationContext generationContext, BlockPos originPos) {
        int detectionRadius = AncientCursesConfig.CURSED_ALTAR_WATER_CHECK_RADIUS.get();
        if (detectionRadius <= 0) {
            return false;
        }

        ChunkGenerator chunkGenerator = generationContext.chunkGenerator();
        LevelHeightAccessor heightAccessor = generationContext.heightAccessor();
        RandomState randomState = generationContext.randomState();

        int minY = Math.max(heightAccessor.getMinBuildHeight(), originPos.getY() - 1);
        int maxY = Math.min(heightAccessor.getMaxBuildHeight() - 1, originPos.getY() + 6);

        for (int dx = -detectionRadius; dx <= detectionRadius; dx++) {
            int x = originPos.getX() + dx;
            for (int dz = -detectionRadius; dz <= detectionRadius; dz++) {
                int z = originPos.getZ() + dz;
                NoiseColumn baseColumn = chunkGenerator.getBaseColumn(x, z, heightAccessor, randomState);
                for (int y = minY; y <= maxY; y++) {
                    BlockState state = baseColumn.getBlock(y);
                    if (!state.getFluidState().isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean hasSurfaceClearance(Structure.GenerationContext generationContext, BlockPos originPos) {
        return hasClearance(generationContext, originPos, 0, 3);
    }

    private static boolean hasUndergroundClearance(Structure.GenerationContext generationContext, BlockPos originPos, int surfaceY) {
        if (originPos.getY() >= surfaceY - 8) {
            return false;
        }

        return hasClearance(generationContext, originPos, 1, 3);
    }

    private static boolean hasClearance(Structure.GenerationContext generationContext, BlockPos originPos, int clearanceRadius, int requiredAirHeight) {
        ChunkGenerator chunkGenerator = generationContext.chunkGenerator();
        LevelHeightAccessor heightAccessor = generationContext.heightAccessor();
        RandomState randomState = generationContext.randomState();

        int maxY = heightAccessor.getMaxBuildHeight() - 1;

        for (int dx = -clearanceRadius; dx <= clearanceRadius; dx++) {
            int x = originPos.getX() + dx;
            for (int dz = -clearanceRadius; dz <= clearanceRadius; dz++) {
                int z = originPos.getZ() + dz;
                NoiseColumn baseColumn = chunkGenerator.getBaseColumn(x, z, heightAccessor, randomState);

                for (int y = originPos.getY() + 1; y <= Math.min(maxY, originPos.getY() + requiredAirHeight); y++) {
                    if (!baseColumn.getBlock(y).isAir()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private static boolean isBlacklistedBiome(Structure.GenerationContext generationContext, BlockPos blockPos) {
        var rawList = AncientCursesConfig.CURSED_ALTAR_BIOME_BLACKLIST.get();
        if (rawList == null || rawList.isEmpty()) return false;

        var biomeSource = generationContext.chunkGenerator().getBiomeSource();
        var sampler = generationContext.randomState().sampler();
        int qx = QuartPos.fromBlock(blockPos.getX());
        int qy = QuartPos.fromBlock(blockPos.getY());
        int qz = QuartPos.fromBlock(blockPos.getZ());
        Holder<Biome> biome = biomeSource.getNoiseBiome(qx, qy, qz, sampler);

        for (String entry : rawList) {
            if (entry == null || entry.isBlank()) continue;

            if (entry.charAt(0) == '#') {
                String tagId = entry.substring(1).trim();
                ResourceLocation resourceLocation = ResourceLocation.tryParse(tagId);
                if (resourceLocation != null) {
                    TagKey<Biome> tag = TagKey.create(Registries.BIOME, resourceLocation);
                    if (biome.is(tag)) {
                        return true;
                    }
                }
            } else {
                ResourceLocation resourceLocation = ResourceLocation.tryParse(entry.trim());
                if (resourceLocation != null) {
                    ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, resourceLocation);
                    if (biome.is(key)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }



    @Override
    public StructureType<?> type() {
        return ModStructureTypes.CURSED_ALTAR.get();
    }
}
