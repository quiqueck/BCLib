package org.betterx.bclib.world.structures;

import org.betterx.bclib.api.v2.levelgen.structures.BCLStructureBuilder;
import org.betterx.worlds.together.tag.v3.TagManager;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

import java.util.function.Function;

@Deprecated(forRemoval = true)
/**
 *  Please use the {@link org.betterx.bclib.api.v2.levelgen.structures.BCLStructure} and
 *  {@link BCLStructureBuilder} instead.
 * @deprecated Use {@link org.betterx.bclib.api.v2.levelgen.structures.BCLStructure} instead
 */
public class BCLStructure<S extends Structure> extends org.betterx.bclib.api.v2.levelgen.structures.BCLStructure<S> {

    @Deprecated(forRemoval = true)
    /**
     * Please use the {@link BCLStructureBuilder} instead:
     *
     * BCLStructureBuilder
     *             .start(id, structureBuilder)
     *             .step(step)
     *             .randomPlacement(spacing, separation)
     *             .build();
     */
    public BCLStructure(
            ResourceLocation id,
            Function<Structure.StructureSettings, S> structureBuilder,
            GenerationStep.Decoration step,
            int spacing,
            int separation
    ) {
        this(id, structureBuilder, step, spacing, separation, false);
    }

    @Deprecated(forRemoval = true)
    /**
     * Please use the {@link BCLStructureBuilder} instead:
     *
     * BCLStructureBuilder
     *             .start(id, structureBuilder)
     *             .step(step)
     *             .randomPlacement(spacing, separation)
     *             .build();
     */
    public BCLStructure(
            ResourceLocation id,
            Function<Structure.StructureSettings, S> structureBuilder,
            GenerationStep.Decoration step,
            int spacing,
            int separation,
            boolean adaptNoise
    ) {
        this(
                id,
                structureBuilder,
                step,
                spacing,
                separation,
                adaptNoise,
                Structure.simpleCodec(structureBuilder),
                TagManager.BIOMES.makeStructureTag(id.getNamespace(), id.getPath())
        );
    }

    @Deprecated(forRemoval = true)
    /**
     *
     * Please use the {@link BCLStructureBuilder} instead:
     *
     * BCLStructureBuilder
     *             .start(id, structureBuilder)
     *             .step(step)
     *             .randomPlacement(spacing, separation)
     *             .codec(codec)
     *             .biomeTag(biomeTag)
     *             .build();
     *
     */
    public BCLStructure(
            ResourceLocation id,
            Function<Structure.StructureSettings, S> structureBuilder,
            GenerationStep.Decoration step,
            int spacing,
            int separation,
            boolean adaptNoise,
            Codec<S> codec,
            TagKey<Biome> biomeTag
    ) {
        super(id, structureBuilder, step, new RandomSpreadStructurePlacement(
                spacing,
                separation,
                RandomSpreadType.LINEAR,
                id.toString().hashCode()
        ), codec, biomeTag, TerrainAdjustment.NONE);
    }

}
