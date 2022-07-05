package org.betterx.bclib.api.v2.levelgen.structures;

import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeBuilder;
import org.betterx.bclib.mixin.common.StructuresAccessor;

import com.mojang.serialization.Codec;
import net.minecraft.core.*;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.StructureSets;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public class BCLStructure<S extends Structure> {
    private final Holder<Structure> structure;
    private final GenerationStep.Decoration featureStep;
    private final List<ResourceLocation> biomes = Lists.newArrayList();
    private final ResourceLocation id;
    public final TagKey<Biome> biomeTag;
    public final ResourceKey<Structure> structureKey;
    public final S baseStructure;
    public final ResourceKey<StructureSet> structureSetKey;
    public final StructurePlacement spreadConfig;

    public final StructureType<S> structureType;

    public final Codec<S> STRUCTURE_CODEC;


    private static HolderSet<Biome> biomes(TagKey<Biome> tagKey) {
        return BuiltinRegistries.BIOME.getOrCreateTag(tagKey);
    }

    private static Structure.StructureSettings structure(
            TagKey<Biome> tagKey,
            Map<MobCategory, StructureSpawnOverride> map,
            GenerationStep.Decoration decoration,
            TerrainAdjustment terrainAdjustment
    ) {
        return new Structure.StructureSettings(biomes(tagKey), map, decoration, terrainAdjustment);
    }

    private static Structure.StructureSettings structure(
            TagKey<Biome> tagKey,
            GenerationStep.Decoration decoration,
            TerrainAdjustment terrainAdjustment
    ) {
        return structure(tagKey, Map.of(), decoration, terrainAdjustment);
    }

    private static <S extends Structure> StructureType<S> registerStructureType(
            ResourceLocation id,
            Codec<S> codec
    ) {
        return Registry.register(Registry.STRUCTURE_TYPES, id, () -> codec);
    }

    protected BCLStructure(
            @NotNull ResourceLocation id,
            @NotNull Function<Structure.StructureSettings, S> structureBuilder,
            GenerationStep.Decoration step,
            @NotNull StructurePlacement placement,
            @NotNull Codec<S> codec,
            @NotNull TagKey<Biome> biomeTag,
            @NotNull TerrainAdjustment terrainAdjustment
    ) {
        this.id = id;
        this.featureStep = step;
        this.STRUCTURE_CODEC = codec;
        this.spreadConfig = placement;
        this.structureKey = ResourceKey.create(Registry.STRUCTURE_REGISTRY, id);
        this.structureSetKey = ResourceKey.create(Registry.STRUCTURE_SET_REGISTRY, id);

        this.structureType = registerStructureType(id, STRUCTURE_CODEC);

        this.biomeTag = biomeTag;
        this.baseStructure = structureBuilder.apply(structure(this.biomeTag, featureStep, terrainAdjustment));
        this.structure = StructuresAccessor.callRegister(structureKey, this.baseStructure);
        StructureSets.register(structureSetKey, this.structure, spreadConfig);
    }

    /**
     * runs the {@code PieceGeneratorSupplier.Context::validBiome} from the given context at
     * height=5 in the middle of the chunk.
     *
     * @param context The context to test with.
     * @return true, if this feature can spawn in the current biome
     */
    public static boolean isValidBiome(Structure.GenerationContext context) {
        return isValidBiome(context, 5);
    }

    /**
     * runs the {@code PieceGeneratorSupplier.Context::validBiome} from the given context at the
     * given height in the middle of the chunk.
     *
     * @param context The context to test with.
     * @param yPos    The Height to test for
     * @return true, if this feature can spawn in the current biome
     */
    public static boolean isValidBiome(Structure.GenerationContext context, int yPos) {
        BlockPos blockPos = context.chunkPos().getMiddleBlockPosition(yPos);
        return context.validBiome().test(
                context
                        .chunkGenerator()
                        .getBiomeSource()
                        .getNoiseBiome(
                                QuartPos.fromBlock(blockPos.getX()),
                                QuartPos.fromBlock(blockPos.getY()),
                                QuartPos.fromBlock(blockPos.getZ()),
                                context.randomState().sampler()
                        )
        );
    }

    public Holder<Structure> getStructure() {
        return structure;
    }

    public GenerationStep.Decoration getFeatureStep() {
        return featureStep;
    }

    /**
     * Get the structure ID;
     *
     * @return {@link ResourceLocation} id.
     */
    public ResourceLocation getID() {
        return id;
    }

    /**
     * Adds biome into internal biome list, used in {@link BCLBiomeBuilder}.
     *
     * @param biome {@link ResourceLocation} biome ID.
     */
    public void addInternalBiome(ResourceLocation biome) {
        biomes.add(biome);
    }

    /**
     * Get biome list where this structure feature can generate. Only represents biomes made with {@link BCLBiomeBuilder} and only
     * if structure was added during building process. Modification of this list will not affect structure generation.
     *
     * @return {@link List} of biome {@link ResourceLocation}.
     */
    public List<ResourceLocation> getBiomes() {
        return biomes;
    }
}
