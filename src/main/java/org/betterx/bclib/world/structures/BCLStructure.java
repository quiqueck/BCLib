package org.betterx.bclib.world.structures;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

import com.mojang.serialization.Codec;
import org.betterx.bclib.api.v2.levelgen.structures.BCLStructureBuilder;
import org.betterx.bclib.api.v2.tag.TagAPI;
import org.betterx.bclib.util.MHelper;

import java.util.function.Function;

@Deprecated(forRemoval = true)
/**
 *  Please use the {@link org.betterx.bclib.api.v2.levelgen.structures.BCLStructure} and
 *  {@link BCLStructureBuilder} instead.
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
    public BCLStructure(ResourceLocation id,
                        Function<Structure.StructureSettings, S> structureBuilder,
                        GenerationStep.Decoration step,
                        int spacing,
                        int separation) {
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
    public BCLStructure(ResourceLocation id,
                        Function<Structure.StructureSettings, S> structureBuilder,
                        GenerationStep.Decoration step,
                        int spacing,
                        int separation,
                        boolean adaptNoise) {
        this(id,
                structureBuilder,
                step,
                spacing,
                separation,
                adaptNoise,
                Structure.simpleCodec(structureBuilder),
                TagAPI.makeStructureTag(id.getNamespace(), id.getPath())
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
    public BCLStructure(ResourceLocation id,
                        Function<Structure.StructureSettings, S> structureBuilder,
                        GenerationStep.Decoration step,
                        int spacing,
                        int separation,
                        boolean adaptNoise,
                        Codec<S> codec,
                        TagKey<Biome> biomeTag) {
        super(id, structureBuilder, step, new RandomSpreadStructurePlacement(spacing,
                separation,
                RandomSpreadType.LINEAR,
                MHelper.RANDOM.nextInt(8192)), codec, biomeTag, TerrainAdjustment.NONE);
    }

}
