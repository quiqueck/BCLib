package org.betterx.bclib.api.v2.levelgen.structures;

import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeBuilder;

import com.mojang.serialization.Codec;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
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
    public static class Unbound<S extends Structure> extends BCLStructure<S> {
        private final Function<Structure.StructureSettings, S> structureBuilder;
        private final TerrainAdjustment terrainAdjustment;

        protected Unbound(
                @NotNull ResourceLocation id,
                @NotNull GenerationStep.Decoration step,
                @NotNull StructurePlacement placement,
                @NotNull Codec<S> codec,
                @NotNull TagKey<Biome> biomeTag,
                @NotNull Function<Structure.StructureSettings, S> structureBuilder,
                @NotNull TerrainAdjustment terrainAdjustment
        ) {
            super(
                    id,
                    ResourceKey.create(Registries.STRUCTURE, id),
                    ResourceKey.create(Registries.STRUCTURE_SET, id),
                    step,
                    placement,
                    codec,
                    biomeTag,
                    BCLStructure.registerStructureType(id, codec)
            );

            this.structureBuilder = structureBuilder;
            this.terrainAdjustment = terrainAdjustment;
        }


        public Bound<S> register(BootstapContext<Structure> bootstrapContext) {
            S baseStructure = structureBuilder.apply(structure(
                    bootstrapContext,
                    this.biomeTag,
                    this.featureStep,
                    terrainAdjustment
            ));
            Holder.Reference<Structure> structure = bootstrapContext.register(structureKey, baseStructure);
            return new Bound<>(
                    this.id,
                    this.structureKey,
                    this.structureSetKey,
                    this.featureStep,
                    this.spreadConfig,
                    this.STRUCTURE_CODEC,
                    this.biomeTag,
                    this.structureType,
                    baseStructure,
                    structure
            );
        }
    }

    public static class Bound<S extends Structure> extends BCLStructure<S> {
        public final S baseStructure;
        public final Holder<Structure> structure;

        private Bound(
                @NotNull ResourceLocation id,
                @NotNull ResourceKey<Structure> structureKey,
                @NotNull ResourceKey<StructureSet> structureSetKey,
                @NotNull GenerationStep.Decoration featureStep,
                @NotNull StructurePlacement placement,
                @NotNull Codec<S> codec,
                @NotNull TagKey<Biome> biomeTag,
                @NotNull StructureType<S> structureType,
                @NotNull S baseStructure,
                @NotNull Holder<Structure> structure
        ) {
            super(id, structureKey, structureSetKey, featureStep, placement, codec, biomeTag, structureType);

            this.baseStructure = baseStructure;
            this.structure = structure;
        }

        public Holder<Structure> getStructure() {
            return structure;
        }
    }


    protected final GenerationStep.Decoration featureStep;
    protected final List<ResourceLocation> biomes = Lists.newArrayList();
    protected final ResourceLocation id;
    public final TagKey<Biome> biomeTag;
    public final ResourceKey<Structure> structureKey;

    public final ResourceKey<StructureSet> structureSetKey;
    public final StructurePlacement spreadConfig;

    public final StructureType<S> structureType;

    public final Codec<S> STRUCTURE_CODEC;

    private static HolderSet<Biome> biomes(BootstapContext<Structure> bootstrapContext, TagKey<Biome> tagKey) {
        //TODO:1.19.3 Refactor
        return null; //BuiltInRegistries.BIOME.getOrCreateTag(tagKey);
    }

    private static Structure.StructureSettings structure(
            BootstapContext<Structure> bootstrapContext,
            TagKey<Biome> tagKey,
            Map<MobCategory, StructureSpawnOverride> map,
            GenerationStep.Decoration decoration,
            TerrainAdjustment terrainAdjustment
    ) {
        return new Structure.StructureSettings(biomes(bootstrapContext, tagKey), map, decoration, terrainAdjustment);
    }

    private static Structure.StructureSettings structure(
            BootstapContext<Structure> bootstrapContext,
            TagKey<Biome> tagKey,
            GenerationStep.Decoration decoration,
            TerrainAdjustment terrainAdjustment
    ) {
        return structure(bootstrapContext, tagKey, Map.of(), decoration, terrainAdjustment);
    }

    private static <S extends Structure> StructureType<S> registerStructureType(
            ResourceLocation id,
            Codec<S> codec
    ) {
        final ResourceKey<StructureType<?>> key = ResourceKey.create(Registries.STRUCTURE_TYPE, id);
        return (StructureType<S>) Registry.register(
                BuiltInRegistries.STRUCTURE_TYPE,
                key,
                () -> (Codec<Structure>) codec
        );
    }

    protected BCLStructure(
            @NotNull ResourceLocation id,
            @NotNull ResourceKey<Structure> structureKey,
            @NotNull ResourceKey<StructureSet> structureSetKey,
            @NotNull GenerationStep.Decoration step,
            @NotNull StructurePlacement placement,
            @NotNull Codec<S> codec,
            @NotNull TagKey<Biome> biomeTag,
            @NotNull StructureType<S> structureType
    ) {
        this.id = id;
        this.featureStep = step;
        this.STRUCTURE_CODEC = codec;
        this.spreadConfig = placement;
        this.structureKey = structureKey;
        this.structureSetKey = structureSetKey;
        this.biomeTag = biomeTag;

        this.structureType = structureType;
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

    public void registerSet(BootstapContext<StructureSet> bootstrapContext) {
        bootstrapContext.register(structureSetKey, new StructureSet(
                bootstrapContext.lookup(Registries.STRUCTURE).getOrThrow(structureKey),
                spreadConfig
        ));
    }
}
