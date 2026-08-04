package org.betterx.bclib.api.v2.levelgen.structures.templatesystem;

import org.betterx.bclib.util.BlocksHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public class DestructionStructureProcessor extends StructureProcessor {
    private int chance = 4;

    public void setChance(int chance) {
        this.chance = chance;
    }

    /**
     * Decides per block whether to remove it, from a random seeded on that block's world position.
     * <p>
     * It must not come from {@code MHelper.RANDOM}, which is what this used to use: that is a
     * {@code ThreadLocalRandomSource} with no seed, which cannot be given one, and which is advanced by
     * every block this processor inspects. A structure therefore eroded differently on every run and
     * could not be rebuilt from the world seed - the same defect already fixed on the nether surface
     * materials and in the placement noise filter.
     * <p>
     * The world position rather than the template-local one, so two copies of the same template placed
     * in different spots do not crumble in identical patterns; {@link Mth#getSeed} mixes the three
     * coordinates, which a raw position hash would not do well enough to avoid visible banding.
     */
    @Override
    public StructureBlockInfo processBlock(
            LevelReader worldView,
            BlockPos pos,
            BlockPos blockPos,
            StructureBlockInfo structureBlockInfo,
            StructureBlockInfo structureBlockInfo2,
            StructurePlaceSettings structurePlacementData
    ) {
        final RandomSource random = RandomSource.create(Mth.getSeed(pos));
        if (!BlocksHelper.isInvulnerable(
                structureBlockInfo2.state(),
                worldView,
                structureBlockInfo2.pos()
        ) && random.nextInt(chance) == 0) {
            return null;
        }
        return structureBlockInfo2;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.RULE;
    }
}
