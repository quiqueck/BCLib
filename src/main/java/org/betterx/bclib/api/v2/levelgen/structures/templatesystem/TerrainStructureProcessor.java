package org.betterx.bclib.api.v2.levelgen.structures.templatesystem;

import org.betterx.bclib.api.v2.levelgen.biomes.BiomeAPI;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public class TerrainStructureProcessor extends StructureProcessor {
    private final Block defaultBlock;

    public TerrainStructureProcessor(Block defaultBlock) {
        this.defaultBlock = defaultBlock;
    }

    @Override
    public StructureBlockInfo processBlock(
            LevelReader worldView,
            BlockPos pos,
            BlockPos blockPos,
            StructureBlockInfo structureBlockInfo,
            StructureBlockInfo structureBlockInfo2,
            StructurePlaceSettings structurePlacementData
    ) {
        BlockPos bpos = structureBlockInfo2.pos();
        if (structureBlockInfo2.state().is(defaultBlock) && worldView.isEmptyBlock(bpos.above())) {
            final BlockState top = BiomeAPI.findTopMaterial(worldView.getBiome(pos))
                                           .orElse(defaultBlock.defaultBlockState());
            return new StructureBlockInfo(bpos, top, structureBlockInfo2.nbt());
        }
        return structureBlockInfo2;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.RULE;
    }
}
