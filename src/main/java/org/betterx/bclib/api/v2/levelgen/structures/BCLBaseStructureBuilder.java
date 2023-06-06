package org.betterx.bclib.api.v2.levelgen.structures;

import org.betterx.worlds.together.tag.v3.TagManager;

import com.mojang.serialization.Codec;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

import java.util.concurrent.ConcurrentLinkedQueue;

abstract class BCLBaseStructureBuilder<S extends Structure, T extends BCLBaseStructureBuilder<S, T>> {
    static final ConcurrentLinkedQueue<BCLStructure.Unbound<?>> UNBOUND_STRUCTURES = new ConcurrentLinkedQueue<>();
    static final ConcurrentLinkedQueue<BCLStructure.Unbound<?>> UNBOUND_STRUCTURE_SETS = new ConcurrentLinkedQueue<>();

    protected final ResourceLocation structureID;
    protected BCLStructure.StructureBuilder<S> structureBuilder;

    private GenerationStep.Decoration step;

    private StructurePlacement placement;

    private TagKey<Biome> biomeTag;

    private TerrainAdjustment terrainAdjustment;

    protected BCLBaseStructureBuilder(
            ResourceLocation structureID,
            BCLStructure.StructureBuilder<S> structureBuilder
    ) {
        this.structureID = structureID;
        this.structureBuilder = structureBuilder;

        this.step = GenerationStep.Decoration.SURFACE_STRUCTURES;
        this.terrainAdjustment = TerrainAdjustment.NONE;
        this.placement = null;
        this.biomeTag = null;
    }

    public T adjustment(TerrainAdjustment value) {
        this.terrainAdjustment = value;
        return (T) this;
    }

    public T step(GenerationStep.Decoration value) {
        this.step = value;
        return (T) this;
    }

    public T placement(StructurePlacement value) {
        this.placement = value;
        return (T) this;
    }

    public T randomPlacement(int spacing, int separation) {
        this.placement = new RandomSpreadStructurePlacement(
                spacing,
                separation,
                RandomSpreadType.LINEAR,
                13323129 + spacing + separation + structureID.toString().hashCode() % 10000
        );
        return (T) this;
    }

    public T biomeTag(String modID, String path) {
        this.biomeTag = TagManager.BIOMES.makeStructureTag(modID, path);
        return (T) this;
    }

    public T biomeTag(TagKey<Biome> tag) {
        this.biomeTag = tag;
        return (T) this;
    }

    protected abstract Codec<S> getCodec();

    public BCLStructure<S> build() {
        if (placement == null) {
            throw new IllegalStateException("Placement needs to be defined for " + this.structureID);
        }

        if (structureBuilder == null) {
            throw new IllegalStateException("A structure builder needs to be defined for " + this.structureID);
        }

        if (biomeTag == null) biomeTag(structureID.getNamespace(), structureID.getPath());

        var res = new BCLStructure.Unbound<>(
                structureID,
                step,
                placement,
                getCodec(),
                biomeTag,
                structureBuilder,
                terrainAdjustment
        );
        UNBOUND_STRUCTURES.add(res);
        UNBOUND_STRUCTURE_SETS.add(res);
        return res;
    }

    static void registerUnbound(BootstapContext<Structure> context) {
        UNBOUND_STRUCTURES.forEach(s -> s.register(context));
        UNBOUND_STRUCTURES.clear();
    }

    static void registerUnboundSets(BootstapContext<StructureSet> context) {
        UNBOUND_STRUCTURE_SETS.forEach(s -> s.registerSet(context));
        UNBOUND_STRUCTURE_SETS.clear();
    }
}
