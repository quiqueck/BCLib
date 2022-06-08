package org.betterx.bclib.api.v2.levelgen.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

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
        return state.is(Blocks.AIR) && before.is(Blocks.LAVA);
    }

    protected boolean isFloorPlaceable(BlockState state, BlockState before) {
        return state.is(Blocks.AIR) && before.getMaterial().isSolid();
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext ctx) {

        WorldGenerationContext worldGenerationContext = new WorldGenerationContext(
                ctx.chunkGenerator(),
                ctx.heightAccessor()
        );
        final Config config = randomConfig(ctx.random());
        if (config == null) return Optional.empty();
        ChunkPos chunkPos = ctx.chunkPos();
        final int x = chunkPos.getMiddleBlockX();
        final int z = chunkPos.getMiddleBlockZ();
        NoiseColumn column = ctx.chunkGenerator().getBaseColumn(x, z, ctx.heightAccessor(), ctx.randomState());
        StructureTemplate structureTemplate = ctx.structureTemplateManager().getOrCreate(config.location);


        BiPredicate<BlockState, BlockState> isCorrectBase;
        int searchStep;
        if (config.type == StructurePlacementType.LAVA) {
            isCorrectBase = this::isLavaPlaceable;
            searchStep = 1;
        } else if (config.type == StructurePlacementType.CEIL) {
            isCorrectBase = this::isFloorPlaceable;
            searchStep = -1;
        } else {
            isCorrectBase = this::isFloorPlaceable;
            searchStep = 1;
        }


        final int seaLevel =
                ctx.chunkGenerator().getSeaLevel()
                        + (searchStep > 0 ? 0 : (structureTemplate.getSize(Rotation.NONE).getY() + config.offsetY));
        final int maxHeight =
                worldGenerationContext.getGenDepth()
                        - 4
                        - (searchStep > 0 ? (structureTemplate.getSize(Rotation.NONE).getY() + config.offsetY) : 0);
        int y = searchStep > 0 ? seaLevel : maxHeight - 1;
        BlockState state = column.getBlock(y - searchStep);

        for (; y < maxHeight && y >= seaLevel; y += searchStep) {
            BlockState before = state;
            state = column.getBlock(y);
            if (isCorrectBase.test(state, before)) break;
        }
        if (y >= maxHeight || y < seaLevel) return Optional.empty();
        if (!BCLStructure.isValidBiome(ctx, y)) return Optional.empty();

        BlockPos halfSize = new BlockPos(
                structureTemplate.getSize().getX() / 2,
                0,
                structureTemplate.getSize().getZ() / 2
        );
        Rotation rotation = StructureNBT.getRandomRotation(ctx.random());
        Mirror mirror = StructureNBT.getRandomMirror(ctx.random());
        BlockPos centerPos = new BlockPos(
                x,
                y - (searchStep == 1 ? 0 : (structureTemplate.getSize(Rotation.NONE).getY())),
                z
        );
        BoundingBox boundingBox = structureTemplate.getBoundingBox(centerPos, rotation, halfSize, mirror);


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
                                        halfSize
                                ))
        ));

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
