package org.betterx.bclib.api.v2.levelgen.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public abstract class TemplateStructure extends Structure {
    protected final List<Config> configs;

    public static <T extends TemplateStructure> Codec<T> simpleTemplateCodec(BiFunction<StructureSettings, List<Config>, T> instancer) {
        return RecordCodecBuilder.create((instance) -> instance
                .group(
                        Structure.settingsCodec(instance),
                        ExtraCodecs.nonEmptyList(Config.CODEC.listOf())
                                   .fieldOf("configs")
                                   .forGetter((T ruinedPortalStructure) -> ruinedPortalStructure.configs)
                )
                .apply(instance, instancer)
        );
    }


    protected TemplateStructure(
            StructureSettings structureSettings,
            ResourceLocation location,
            int offsetY,
            StructurePlacementType type,
            float chance
    ) {
        this(structureSettings, List.of(new Config(location, offsetY, type, chance)));
    }

    protected TemplateStructure(
            StructureSettings structureSettings,
            List<Config> configs
    ) {
        super(structureSettings);
        this.configs = configs;
    }

    protected Config randomConfig(RandomSource random) {
        if (this.configs.size() > 1) {
            final float chanceSum = configs.parallelStream().map(c -> c.chance()).reduce(0.0f, (p, c) -> p + c);
            float rnd = random.nextFloat() * chanceSum;

            for (Config c : configs) {
                rnd -= c.chance();
                if (rnd <= 0) return c;
            }
        } else {
            return this.configs.get(0);
        }

        return null;
    }

    protected boolean isLavaPlaceable(BlockState state, BlockState before) {
        return (state == null || state.is(Blocks.AIR)) && before.is(Blocks.LAVA);
    }

    protected boolean isFloorPlaceable(BlockState state, BlockState before) {
        return (state == null || state.is(Blocks.AIR)) && before.isSolid();
    }

    protected int erosion(RandomSource rnd) {
        return 0;
    }

    protected boolean cover(RandomSource rnd) {
        return false;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext ctx) {
        final Config config = randomConfig(ctx.random());
        if (config == null) return Optional.empty();
        
        ChunkPos chunkPos = ctx.chunkPos();
        final int x = chunkPos.getMinBlockX();
        final int z = chunkPos.getMinBlockZ();
        if (!hasValidBiomeAtRandomHeight(ctx, x, z))
            return Optional.empty();

        WorldGenerationContext worldGenerationContext = new WorldGenerationContext(
                ctx.chunkGenerator(),
                ctx.heightAccessor()
        );

        StructureTemplate structureTemplate = ctx.structureTemplateManager().getOrCreate(config.location);

        final BiPredicate<BlockState, BlockState> isCorrectBase;
        final int searchStep;
        final int minBaseCount;
        final float minAirRatio = 0.6f;
        if (config.type == StructurePlacementType.LAVA) {
            isCorrectBase = this::isLavaPlaceable;
            minBaseCount = 5;
            searchStep = 1;
        } else if (config.type == StructurePlacementType.CEIL) {
            isCorrectBase = this::isFloorPlaceable;
            minBaseCount = 3;
            searchStep = -1;
        } else {
            isCorrectBase = this::isFloorPlaceable;
            minBaseCount = 3;
            searchStep = 1;
        }


        final int seaLevel =
                ctx.chunkGenerator().getSeaLevel()
                        + (searchStep > 0 ? 0 : (structureTemplate.getSize(Rotation.NONE).getY() + config.offsetY));
        final int maxHeight =
                worldGenerationContext.getGenDepth()
                        - 4
                        - (searchStep > 0 ? (structureTemplate.getSize(Rotation.NONE).getY() + config.offsetY) : 0);

        BlockPos halfSize = new BlockPos(
                structureTemplate.getSize().getX() / 2,
                0,
                structureTemplate.getSize().getZ() / 2
        );
        Rotation rotation = StructureNBT.getRandomRotation(ctx.random());
        Mirror mirror = StructureNBT.getRandomMirror(ctx.random());
        BlockPos.MutableBlockPos centerPos = new BlockPos.MutableBlockPos(
                x,
                0,
                z
        );
        BoundingBox boundingBox = structureTemplate.getBoundingBox(centerPos, rotation, halfSize, mirror);

        var noiseColumns = ImmutableList
                .of(
                        new BlockPos(boundingBox.getCenter().getX(), 0, boundingBox.getCenter().getZ()),
                        new BlockPos(boundingBox.minX(), 0, boundingBox.minZ()),
                        new BlockPos(boundingBox.maxX(), 0, boundingBox.minZ()),
                        new BlockPos(boundingBox.minX(), 0, boundingBox.maxZ()),
                        new BlockPos(boundingBox.maxX(), 0, boundingBox.maxZ())
                )
                .stream()
                .map(blockPos -> ctx.chunkGenerator().getBaseColumn(
                        blockPos.getX(),
                        blockPos.getZ(),
                        ctx.heightAccessor(),
                        ctx.randomState()
                )).toList();

        int y = noiseColumns
                .stream()
                .map(column -> findY(column, isCorrectBase, searchStep, seaLevel, maxHeight))
                .reduce(
                        searchStep > 0 ? Integer.MAX_VALUE : Integer.MIN_VALUE,
                        (p, c) -> searchStep > 0 ? Math.min(p, c) : Math.max(p, c)
                );

        if (y >= maxHeight || y < seaLevel) return Optional.empty();
        if (!BCLStructure.isValidBiome(ctx, y)) return Optional.empty();

        int baseCount = noiseColumns
                .stream()
                .map(column -> isCorrectBase.test(null, column.getBlock(y - searchStep)))
                .filter(b -> b)
                .map(b -> 1)
                .reduce(0, (p, c) -> p + c);

        if (baseCount < minBaseCount) return Optional.empty();

        float airRatio = noiseColumns
                .stream()
                .map(column -> airRatio(column, y, boundingBox.getYSpan(), searchStep))
                .reduce(0.0f, (p, c) -> p + c) / noiseColumns.size();

        if (airRatio < minAirRatio) return Optional.empty();

        centerPos.setY(y - (searchStep == 1 ? 0 : (structureTemplate.getSize(Rotation.NONE).getY())));

        int erosion = erosion(ctx.random());
        boolean cover = cover(ctx.random());
        // if (!structure.canGenerate(ctx.chunkGenerator()., centerPos))
        return Optional.of(new GenerationStub(
                centerPos,
                structurePiecesBuilder ->
                        structurePiecesBuilder.addPiece(
                                new TemplatePiece(
                                        ctx.structureTemplateManager(),
                                        config.location,
                                        centerPos.offset(
                                                0,
                                                config.offsetY,
                                                0
                                        ),
                                        rotation,
                                        mirror,
                                        halfSize,
                                        erosion,
                                        cover
                                ))
        ));

    }

    private boolean hasValidBiomeAtRandomHeight(GenerationContext ctx, int x, int z) {
        final int randomY = ctx.random()
                               .nextIntBetweenInclusive(
                                       ctx.heightAccessor().getMinBuildHeight(),
                                       ctx.heightAccessor().getMaxBuildHeight()
                               );

        Holder<Biome> holder = ctx.chunkGenerator()
                                  .getBiomeSource()
                                  .getNoiseBiome(
                                          QuartPos.fromBlock(x),
                                          QuartPos.fromBlock(randomY),
                                          QuartPos.fromBlock(z),
                                          ctx.randomState().sampler()
                                  );
        if (!ctx.validBiome().test(holder)) {
            return false;
        }
        return true;
    }

    private float airRatio(NoiseColumn column, int y, int height, int searchStep) {
        int airCount = 0;
        for (int i = y; i < y + height && i > y - height; i += searchStep) {
            BlockState state = column.getBlock(i);
            if (state.isAir() || state.canBeReplaced()) {
                airCount++;
            }
        }
        return airCount / (float) height;
    }


    private int findY(
            NoiseColumn column,
            BiPredicate<BlockState, BlockState> isCorrectBase,
            int searchStep,
            int seaLevel,
            int maxHeight
    ) {
        int y = searchStep > 0 ? seaLevel : maxHeight - 1;
        BlockState state = column.getBlock(y - searchStep);

        for (; y < maxHeight && y >= seaLevel; y += searchStep) {
            BlockState before = state;
            state = column.getBlock(y);
            if (isCorrectBase.test(state, before)) break;
        }
        return y;
    }


    public record Config(ResourceLocation location, int offsetY, StructurePlacementType type, float chance) {
        public static final Codec<Config> CODEC =
                RecordCodecBuilder.create((instance) ->
                        instance.group(
                                        ResourceLocation.CODEC
                                                .fieldOf("location")
                                                .forGetter((cfg) -> cfg.location),

                                        Codec
                                                .INT
                                                .fieldOf("offset_y")
                                                .orElse(0)
                                                .forGetter((cfg) -> cfg.offsetY),

                                        StructurePlacementType.CODEC
                                                .fieldOf("placement")
                                                .orElse(StructurePlacementType.FLOOR)
                                                .forGetter((cfg) -> cfg.type),
                                        Codec
                                                .FLOAT
                                                .fieldOf("chance")
                                                .orElse(1.0f)
                                                .forGetter((cfg) -> cfg.chance)
                                )
                                .apply(instance, Config::new)
                );
    }
}
