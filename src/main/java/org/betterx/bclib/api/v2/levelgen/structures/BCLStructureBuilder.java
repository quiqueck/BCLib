package org.betterx.bclib.api.v2.levelgen.structures;

import org.betterx.bclib.api.v2.tag.TagAPI;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

import java.util.function.Function;

public class BCLStructureBuilder<S extends Structure> {
    private static final BCLStructureBuilder INSTANCE = new BCLStructureBuilder();

    private ResourceLocation structureID;
    private Function<Structure.StructureSettings, S> structureBuilder;

    private GenerationStep.Decoration step;

    private Codec<S> codec;

    private StructurePlacement placement;

    private TagKey<Biome> biomeTag;

    private TerrainAdjustment terrainAdjustment;

    private BCLStructureBuilder() {
    }

    public static <S extends Structure> BCLStructureBuilder<S> start(
            ResourceLocation structureID,
            Function<Structure.StructureSettings, S> structureBuilder
    ) {
        INSTANCE.structureID = structureID;
        INSTANCE.structureBuilder = structureBuilder;

        INSTANCE.step = GenerationStep.Decoration.SURFACE_STRUCTURES;
        INSTANCE.terrainAdjustment = TerrainAdjustment.NONE;
        INSTANCE.codec = null;
        INSTANCE.placement = null;
        INSTANCE.biomeTag = null;

        return INSTANCE;
    }

    public BCLStructureBuilder<S> step(TerrainAdjustment value) {
        this.terrainAdjustment = value;
        return this;
    }

    public BCLStructureBuilder<S> step(GenerationStep.Decoration value) {
        this.step = value;
        return this;
    }

    public BCLStructureBuilder<S> codec(Codec<S> value) {
        this.codec = value;
        return this;
    }

    public BCLStructureBuilder<S> placement(StructurePlacement value) {
        this.placement = value;
        return this;
    }

    public BCLStructureBuilder<S> randomPlacement(int spacing, int separation) {
        this.placement = new RandomSpreadStructurePlacement(
                spacing,
                separation,
                RandomSpreadType.LINEAR,
                13323129 + spacing + separation + structureID.toString().hashCode() % 10000
        );
        return this;
    }

    public BCLStructureBuilder<S> biomeTag(String modID, String path) {
        this.biomeTag = TagAPI.makeStructureTag(modID, path);
        return this;
    }

    public BCLStructureBuilder<S> biomeTag(TagKey<Biome> tag) {
        this.biomeTag = tag;
        return this;
    }

    public BCLStructure<S> build() {
        if (placement == null) {
            throw new IllegalStateException("Placement needs to be defined for " + this.structureID);
        }
        if (codec == null) codec(Structure.simpleCodec(structureBuilder));
        if (biomeTag == null) biomeTag(structureID.getNamespace(), structureID.getPath());

        return new BCLStructure<>(
                structureID,
                structureBuilder,
                step,
                placement,
                codec,
                biomeTag,
                terrainAdjustment
        );
    }
}
