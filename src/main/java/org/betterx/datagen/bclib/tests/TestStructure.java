package org.betterx.datagen.bclib.tests;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.util.MHelper;
import org.betterx.worlds.together.tag.v3.TagManager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.Map;
import java.util.Optional;

class TestStructurePiece extends StructurePiece {
    static final ResourceKey<StructurePieceType> KEY = ResourceKey.create(
            Registries.STRUCTURE_PIECE,
            BCLib.makeID("test_piece")
    );
    static final StructurePieceType INSTANCE = TestStructurePiece::new;

    protected TestStructurePiece(int genDepth, BoundingBox boundingBox) {
        super(INSTANCE, genDepth, boundingBox);
    }

    public TestStructurePiece(CompoundTag compoundTag) {
        super(INSTANCE, compoundTag);
    }

    public TestStructurePiece(
            StructurePieceSerializationContext context,
            CompoundTag tag
    ) {
        super(INSTANCE, tag);
    }

    @Override
    protected void addAdditionalSaveData(
            StructurePieceSerializationContext structurePieceSerializationContext,
            CompoundTag compoundTag
    ) {

    }

    @Override
    public void postProcess(
            WorldGenLevel worldGenLevel,
            StructureManager structureManager,
            ChunkGenerator chunkGenerator,
            RandomSource randomSource,
            BoundingBox boundingBox,
            ChunkPos chunkPos,
            BlockPos blockPos
    ) {

    }
}

public class TestStructure extends Structure {
    static final TagKey<Biome> TEST_STRUCTURE_TAG = TagManager.BIOMES.makeTag(BCLib.makeID("test_structure"));
    static final ResourceKey<StructureType<?>> TYPE_KEY = ResourceKey.create(
            Registries.STRUCTURE_TYPE,
            BCLib.makeID("test_type")
    );
    static final StructureType<TestStructure> TYPE = () -> Structure.simpleCodec(TestStructure::new);

    static final ResourceKey<Structure> KEY = ResourceKey.create(Registries.STRUCTURE, BCLib.makeID("test_structure"));

    protected TestStructure(StructureSettings structureSettings) {
        super(structureSettings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        BlockPos pos = getGenerationHeight(
                context.chunkPos(),
                context.chunkGenerator(),
                context.heightAccessor(),
                context.randomState()
        );
        if (pos.getY() >= 10) {
            return Optional.of(new Structure.GenerationStub(pos, (structurePiecesBuilder) -> {
                generatePieces(structurePiecesBuilder, context);
            }));
        }
        return Optional.empty();
    }

    private static BlockPos getGenerationHeight(
            ChunkPos chunkPos,
            ChunkGenerator chunkGenerator,
            LevelHeightAccessor levelHeightAccessor,
            RandomState rState
    ) {
        final int blockX = chunkPos.getBlockX(7);
        final int blockZ = chunkPos.getBlockZ(7);
        int z = chunkGenerator.getFirstOccupiedHeight(
                blockX, blockZ, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, rState
        );

        return new BlockPos.MutableBlockPos(blockX, z, blockZ);
    }

    protected void generatePieces(StructurePiecesBuilder structurePiecesBuilder, GenerationContext context) {
        final RandomSource random = context.random();
        final ChunkPos chunkPos = context.chunkPos();
        final ChunkGenerator chunkGenerator = context.chunkGenerator();
        final LevelHeightAccessor levelHeightAccessor = context.heightAccessor();
        final RandomState rState = context.randomState();

        int x = chunkPos.getBlockX(MHelper.randRange(4, 12, random));
        int z = chunkPos.getBlockZ(MHelper.randRange(4, 12, random));
        int y = chunkGenerator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, rState);
        if (y > 50) {
            structurePiecesBuilder.addPiece(new TestStructurePiece(
                    5,
                    new BoundingBox(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1)
            ));
        }
    }

    @Override
    public StructureType<?> type() {
        return TYPE;
    }

    public static void bootstrap(BootstapContext<Structure> bootstrapContext) {
        BCLib.LOGGER.info("Bootstrap Structure");
        Registry.register(BuiltInRegistries.STRUCTURE_PIECE, TestStructurePiece.KEY, TestStructurePiece.INSTANCE);
        Registry.register(BuiltInRegistries.STRUCTURE_TYPE, TYPE_KEY, TYPE);
        HolderSet<Biome> biomes = bootstrapContext.lookup(Registries.BIOME).getOrThrow(TEST_STRUCTURE_TAG);

        bootstrapContext.register(KEY, new TestStructure(new Structure.StructureSettings(
                biomes,
                Map.of(),
                GenerationStep.Decoration.SURFACE_STRUCTURES,
                TerrainAdjustment.BEARD_THIN
        )));
    }
}
