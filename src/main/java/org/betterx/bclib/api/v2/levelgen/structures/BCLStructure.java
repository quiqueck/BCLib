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
import org.jetbrains.annotations.NotNull;

public abstract class BCLStructure<S extends Structure> {
    public interface StructureBuilder<S extends Structure> {
        S apply(Structure.StructureSettings structureSettings);
    }

    public interface StructureCodecProvider<S extends Structure> {
        Codec<S> getCodec();
    }

    public interface StructureBuilderWithContext<S extends Structure> extends StructureBuilder<S> {
        default S apply(Structure.StructureSettings structureSettings) {
            return apply(structureSettings, null);
        }

        S apply(Structure.StructureSettings structureSettings, BootstapContext<Structure> ctx);
    }

    public static class Unbound<S extends Structure> extends BCLStructure<S> {
        private final StructureBuilder<S> structureBuilder;
        private final TerrainAdjustment terrainAdjustment;

        private Bound<S> registered;

        protected Unbound(
                @NotNull ResourceLocation id,
                @NotNull GenerationStep.Decoration step,
                @NotNull StructurePlacement placement,
                @NotNull Codec<S> codec,
                @NotNull TagKey<Biome> biomeTag,
                @NotNull StructureBuilder<S> structureBuilder,
                @NotNull TerrainAdjustment terrainAdjustment
        ) {
            super(
                    id,
                    ResourceKey.create(Registries.STRUCTURE, id),
                    ResourceKey.create(Registries.STRUCTURE_SET, id),
                    step,
                    placement,
                    //codec,
                    biomeTag,
                    BCLStructure.registerStructureType(id, codec)
            );

            registered = null;
            this.structureBuilder = structureBuilder;
            this.terrainAdjustment = terrainAdjustment;
        }


        public Bound<S> register(BootstapContext<Structure> bootstrapContext) {
            if (registered != null) return registered;
            final Structure.StructureSettings settings = structure(
                    bootstrapContext,
                    this.biomeTag,
                    this.featureStep,
                    terrainAdjustment
            );
            S baseStructure;
            if (structureBuilder instanceof StructureBuilderWithContext<S> sctx) {
                baseStructure = sctx.apply(settings, bootstrapContext);
            } else {
                baseStructure = structureBuilder.apply(settings);
            }

            Holder.Reference<Structure> structure = bootstrapContext.register(structureKey, baseStructure);
            BCLStructureBuilder.UNBOUND_STRUCTURES.remove(this);
            registered = new Bound<>(
                    this.id,
                    this.structureKey,
                    this.structureSetKey,
                    this.featureStep,
                    this.spreadConfig,
                    //this.STRUCTURE_CODEC,
                    this.biomeTag,
                    this.structureType,
                    baseStructure,
                    structure
            );
            return registered;
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
                //@NotNull Codec<S> codec,
                @NotNull TagKey<Biome> biomeTag,
                @NotNull StructureType<S> structureType,
                @NotNull S baseStructure,
                @NotNull Holder<Structure> structure
        ) {
            super(id, structureKey, structureSetKey, featureStep, placement, /**codec,**/biomeTag, structureType);

            this.baseStructure = baseStructure;
            this.structure = structure;
        }

        public Holder<Structure> getStructure() {
            return structure;
        }

        public Bound<S> register(BootstapContext<Structure> bootstrapContext) {
            return this;
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

    //public final Codec<S> STRUCTURE_CODEC;

    private static HolderSet<Biome> biomes(BootstapContext<Structure> bootstrapContext, TagKey<Biome> tagKey) {
        return bootstrapContext.lookup(Registries.BIOME).getOrThrow(tagKey);
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
            //@NotNull Codec<S> codec,
            @NotNull TagKey<Biome> biomeTag,
            @NotNull StructureType<S> structureType
    ) {
        this.id = id;
        this.featureStep = step;
        //this.STRUCTURE_CODEC = codec;
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

    private boolean registeredSet = false;

    public void registerSet(BootstapContext<StructureSet> bootstrapContext) {
        if (registeredSet) return;
        registeredSet = true;
        bootstrapContext.register(structureSetKey, new StructureSet(
                bootstrapContext.lookup(Registries.STRUCTURE).getOrThrow(structureKey),
                spreadConfig
        ));
        BCLStructureBuilder.UNBOUND_STRUCTURE_SETS.remove(this);
    }

    public abstract Bound<S> register(BootstapContext<Structure> bootstrapContext);
}
