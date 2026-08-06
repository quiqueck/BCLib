package org.betterx.bclib.api.v2.levelgen.structures.templatesystem;

import org.betterx.bclib.util.BlocksHelper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

/**
 * A {@link StructureProcessor} that randomly removes blocks, to make a placed structure look ruined.
 * <p>
 * 26.2 changes reflected here:
 * <ul>
 *     <li>{@code StructureProcessor} became an <em>interface</em>, so this class implements it.</li>
 *     <li>{@code getType()} was replaced by {@code codec()}, which returns the {@link MapCodec}
 *     directly rather than a {@code KeyDispatchDataCodec} wrapper. Up to 26.1 this class returned
 *     {@code StructureProcessorType.RULE} - the type of an unrelated vanilla processor - which was
 *     only ever a placeholder: this processor is never serialized, it is instantiated directly in
 *     Java (BetterEnd's {@code NBTFeature.DESTRUCTION}) and it is not registered in
 *     {@code BuiltInRegistries.STRUCTURE_PROCESSOR}. A real codec over the one field it has is
 *     supplied instead, which at least does not misidentify it; it still cannot round-trip through
 *     JSON until someone registers it.</li>
 *     <li>{@code processBlock}'s fourth parameter is no longer the original
 *     {@code StructureBlockInfo} but only its {@code BlockPos}, and the fifth (the processed info)
 *     is now the only {@code StructureBlockInfo}. The body only ever used the processed info, so it
 *     maps onto the surviving parameter unchanged.</li>
 * </ul>
 */
public class DestructionStructureProcessor implements StructureProcessor {
    public static final MapCodec<DestructionStructureProcessor> CODEC = Codec
            .INT
            .optionalFieldOf("chance", 4)
            .xmap(
                    chance -> {
                        DestructionStructureProcessor processor = new DestructionStructureProcessor();
                        processor.setChance(chance);
                        return processor;
                    },
                    processor -> processor.chance
            );

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
            BlockPos targetPosition,
            BlockPos referencePos,
            BlockPos originalPos,
            StructureBlockInfo processedBlockInfo,
            StructurePlaceSettings structurePlacementData
    ) {
        final RandomSource random = RandomSource.create(Mth.getSeed(targetPosition));
        if (!BlocksHelper.isInvulnerable(
                processedBlockInfo.state(),
                worldView,
                processedBlockInfo.pos()
        ) && random.nextInt(chance) == 0) {
            return null;
        }
        return processedBlockInfo;
    }

    @Override
    public MapCodec<DestructionStructureProcessor> codec() {
        return CODEC;
    }
}
