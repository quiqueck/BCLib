package org.betterx.bclib.api.v2.levelgen.structures;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Optional;
import java.util.function.Function;

public class SingleEndPoolElement extends SinglePoolElement {
    public static final MapCodec<SingleEndPoolElement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SingleEndPoolElement.templateCodec(),
            SingleEndPoolElement.processorsCodec(),
            SingleEndPoolElement.projectionCodec()
    ).apply(instance, SingleEndPoolElement::new));

    public SingleEndPoolElement(
            Either<ResourceLocation, StructureTemplate> either,
            Holder<StructureProcessorList> holder,
            StructureTemplatePool.Projection projection
    ) {
        this(either, holder, projection, Optional.empty());
    }

    public SingleEndPoolElement(
            Either<ResourceLocation, StructureTemplate> either,
            Holder<StructureProcessorList> holder,
            StructureTemplatePool.Projection projection,
            Optional<LiquidSettings> liquidSettings
    ) {
        super(either, holder, projection, liquidSettings);
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
            LiquidSettings liquidSettings,
            boolean bl
    ) {
        //in the end, we don't want to generate anything below y=5
        if (blockPos.getY() < 5) return false;
        boolean hasEmptySpace = worldGenLevel.isEmptyBlock(blockPos.above(2)) || worldGenLevel.isEmptyBlock(blockPos);
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
                liquidSettings,
                bl
        );
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
