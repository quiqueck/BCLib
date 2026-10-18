package org.betterx.bclib.api.v2.levelgen.structures;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SingleEndPoolElement extends SinglePoolElement {
    public static final Codec<SingleEndPoolElement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SingleEndPoolElement.templateCodec(),
            SingleEndPoolElement.processorsCodec(),
            SingleEndPoolElement.projectionCodec()
    ).apply(instance, SingleEndPoolElement::new));

    public SingleEndPoolElement(
            Either<ResourceLocation, StructureTemplate> either,
            Holder<StructureProcessorList> holder,
            StructureTemplatePool.Projection projection
    ) {
        super(either, holder, projection);
    }

    public static Function<StructureTemplatePool.Projection, SingleEndPoolElement> end(
            ResourceLocation id,
            Holder<StructureProcessorList> holder
    ) {
        return projection -> new SingleEndPoolElement(
                Either.left(id),
                holder,
                projection
        );
    }

    @Override
    public boolean place(
            StructureTemplateManager structureTemplateManager,
            WorldGenLevel worldGenLevel,
            StructureManager structureManager,
            ChunkGenerator chunkGenerator,
            BlockPos blockPos,
            BlockPos blockPos2,
            Rotation rotation,
            BoundingBox boundingBox,
            RandomSource randomSource,
            boolean bl
    ) {
        //in the end, we don't want to generate anything below y=5
        if (blockPos.getY() < 5) return false;
        // Every chunk the piece overlaps asks this again, and the piece's origin may lie in a chunk another worldgen
        // thread is generating right now. Look at the terrain shape from the noise instead of the generated blocks,
        // so every chunk gets the same, race-free answer.
        final NoiseColumn column = chunkGenerator.getBaseColumn(
                blockPos.getX(),
                blockPos.getZ(),
                worldGenLevel,
                worldGenLevel.getLevel().getChunkSource().randomState()
        );
        boolean hasEmptySpace = column.getBlock(blockPos.getY() + 2).isAir() || column.getBlock(blockPos.getY()).isAir();
        if (!hasEmptySpace)
            return false;

        return super.place(
                structureTemplateManager,
                worldGenLevel,
                structureManager,
                chunkGenerator,
                blockPos,
                blockPos2,
                rotation,
                boundingBox,
                randomSource,
                bl
        );
    }

    @Override
    protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox boundingBox, boolean keepJigsaws) {
        final StructurePlaceSettings settings = super.getSettings(rotation, boundingBox, keepJigsaws);
        if (boundingBox != null) {
            // Only blocks inside the bounding box (the chunk being generated) are placed. Processing the others
            // anyway (e.g. the gravity of terrain matching pieces reads the height there) would read chunks that
            // other worldgen threads may be generating at the same time.
            final List<StructureProcessor> processors = new ArrayList<>(settings.getProcessors());
            settings.clearProcessors();
            for (StructureProcessor processor : processors) {
                settings.addProcessor(new InBoundsProcessor(processor, boundingBox));
            }
        }
        return settings;
    }

    private static class InBoundsProcessor extends StructureProcessor {
        private final StructureProcessor processor;
        private final BoundingBox boundingBox;

        InBoundsProcessor(StructureProcessor processor, BoundingBox boundingBox) {
            this.processor = processor;
            this.boundingBox = boundingBox;
        }

        @Nullable
        @Override
        public StructureTemplate.StructureBlockInfo processBlock(
                LevelReader level,
                BlockPos offset,
                BlockPos pivot,
                StructureTemplate.StructureBlockInfo original,
                StructureTemplate.StructureBlockInfo current,
                StructurePlaceSettings settings
        ) {
            final BlockPos pos = current.pos();
            if (pos.getX() < boundingBox.minX() || pos.getX() > boundingBox.maxX()
                    || pos.getZ() < boundingBox.minZ() || pos.getZ() > boundingBox.maxZ()) {
                return current;
            }
            return processor.processBlock(level, offset, pivot, original, current, settings);
        }

        @Override
        public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(
                ServerLevelAccessor level,
                BlockPos offset,
                BlockPos pivot,
                List<StructureTemplate.StructureBlockInfo> originals,
                List<StructureTemplate.StructureBlockInfo> processed,
                StructurePlaceSettings settings
        ) {
            return processor.finalizeProcessing(level, offset, pivot, originals, processed, settings);
        }

        @Override
        protected StructureProcessorType<?> getType() {
            // only used to serialize processor lists, this wrapper only lives during placement
            return StructureProcessorType.NOP;
        }
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return BCLStructurePoolElementTypes.END;
    }

    @Override
    public String toString() {
        return "SingleEnd[" + this.template + "]";
    }
}
