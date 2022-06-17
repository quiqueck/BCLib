package org.betterx.bclib.api.v2.levelgen.biomes;

import org.betterx.bclib.api.v2.levelgen.features.BCLFeature;
import org.betterx.bclib.api.v2.levelgen.structures.BCLStructure;
import org.betterx.bclib.api.v2.levelgen.surface.SurfaceRuleBuilder;
import org.betterx.bclib.entity.BCLEntityWrapper;
import org.betterx.bclib.mixin.common.BiomeGenerationSettingsAccessor;
import org.betterx.bclib.util.CollectionsUtil;
import org.betterx.bclib.util.ColorUtil;
import org.betterx.bclib.util.Pair;
import org.betterx.bclib.util.TriFunction;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.biome.Biome.BiomeBuilder;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class BCLBiomeBuilder {
    @FunctionalInterface
    public interface BiomeSupplier<T> extends TriFunction<ResourceLocation, Biome, BCLBiomeSettings, T> {
    }

    private static final BCLBiomeBuilder INSTANCE = new BCLBiomeBuilder();
    private static final SurfaceRules.ConditionSource SURFACE_NOISE = SurfaceRules.noiseCondition(
            Noises.SOUL_SAND_LAYER,
            -0.012
    );

    private final List<Pair<GenerationStep.Carving, Holder<? extends ConfiguredWorldCarver<?>>>> carvers = new ArrayList<>(
            1);
    private BiomeGenerationSettings.Builder generationSettings;
    private BiomeSpecialEffects.Builder effectsBuilder;
    private MobSpawnSettings.Builder spawnSettings;
    private SurfaceRules.RuleSource surfaceRule;
    private Precipitation precipitation;
    private ResourceLocation biomeID;

    private final Set<TagKey<Biome>> tags = Sets.newHashSet();

    private final List<Climate.ParameterPoint> parameters = Lists.newArrayList();

    //BiomeTags.IS_NETHER
    private float temperature;
    private float fogDensity;
    private float genChance;
    private float downfall;
    private float height;
    private int edgeSize;
    private BCLBiome edge;
    private boolean vertical;


    /**
     * Starts new biome building process.
     *
     * @param biomeID {@link ResourceLocation} biome identifier.
     * @return prepared {@link BCLBiomeBuilder} instance.
     */
    public static BCLBiomeBuilder start(ResourceLocation biomeID) {
        INSTANCE.biomeID = biomeID;
        INSTANCE.precipitation = Precipitation.NONE;
        INSTANCE.generationSettings = null;
        INSTANCE.effectsBuilder = null;
        INSTANCE.spawnSettings = null;
        INSTANCE.temperature = 1.0F;
        INSTANCE.fogDensity = 1.0F;
        INSTANCE.edgeSize = 0;
        INSTANCE.downfall = 1.0F;
        INSTANCE.genChance = 1.0F;
        INSTANCE.height = 0.1F;
        INSTANCE.vertical = false;
        INSTANCE.edge = null;
        INSTANCE.carvers.clear();
        INSTANCE.parameters.clear();
        INSTANCE.tags.clear();
        return INSTANCE;
    }

    public BCLBiomeBuilder addNetherClimateParamater(float temperature, float humidity) {
        parameters.add(Climate.parameters(temperature, humidity, 0, 0, 0, 0, 0));
        return this;
    }

    /**
     * Set biome {@link Precipitation}. Affect biome visual effects (rain, snow, none).
     *
     * @param precipitation {@link Precipitation}
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder precipitation(Precipitation precipitation) {
        this.precipitation = precipitation;
        return this;
    }

    /**
     * Set biome temperature, affect plant color, biome generation and ice formation.
     *
     * @param temperature biome temperature.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder temperature(float temperature) {
        this.temperature = temperature;
        return this;
    }

    /**
     * Set biome wetness (same as downfall). Affect plant color and biome generation.
     *
     * @param wetness biome wetness (downfall).
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder wetness(float wetness) {
        this.downfall = wetness;
        return this;
    }

    /**
     * Adds mob spawning to biome.
     *
     * @param entityType    {@link EntityType} mob type.
     * @param weight        spawn weight.
     * @param minGroupCount minimum mobs in group.
     * @param maxGroupCount maximum mobs in group.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public <M extends Mob> BCLBiomeBuilder spawn(
            EntityType<M> entityType,
            int weight,
            int minGroupCount,
            int maxGroupCount
    ) {
        getSpawns().addSpawn(
                entityType.getCategory(),
                new SpawnerData(entityType, weight, minGroupCount, maxGroupCount)
        );
        return this;
    }

    /**
     * Adds mob spawning to biome.
     *
     * @param wrapper       {@link BCLEntityWrapper} mob type.
     * @param weight        spawn weight.
     * @param minGroupCount minimum mobs in group.
     * @param maxGroupCount maximum mobs in group.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public <M extends Mob> BCLBiomeBuilder spawn(
            BCLEntityWrapper<M> wrapper,
            int weight,
            int minGroupCount,
            int maxGroupCount
    ) {
        if (wrapper.canSpawn()) {
            return spawn(wrapper.type(), weight, minGroupCount, maxGroupCount);
        }

        return this;
    }

    /**
     * Adds ambient particles to thr biome.
     *
     * @param particle    {@link ParticleOptions} particles (or {@link net.minecraft.core.particles.ParticleType}).
     * @param probability particle spawn probability, should have low value (example: 0.01F).
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder particles(ParticleOptions particle, float probability) {
        getEffects().ambientParticle(new AmbientParticleSettings(particle, probability));
        return this;
    }

    /**
     * Sets sky color for the biome. Color is in ARGB int format.
     *
     * @param color ARGB color as integer.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder skyColor(int color) {
        getEffects().skyColor(color);
        return this;
    }

    /**
     * Sets sky color for the biome. Color represented as red, green and blue channel values.
     *
     * @param red   red color component [0-255]
     * @param green green color component [0-255]
     * @param blue  blue color component [0-255]
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder skyColor(int red, int green, int blue) {
        red = Mth.clamp(red, 0, 255);
        green = Mth.clamp(green, 0, 255);
        blue = Mth.clamp(blue, 0, 255);
        return skyColor(ColorUtil.color(red, green, blue));
    }

    /**
     * Sets fog color for the biome. Color is in ARGB int format.
     *
     * @param color ARGB color as integer.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder fogColor(int color) {
        getEffects().fogColor(color);
        return this;
    }

    /**
     * Sets fog color for the biome. Color represented as red, green and blue channel values.
     *
     * @param red   red color component [0-255]
     * @param green green color component [0-255]
     * @param blue  blue color component [0-255]
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder fogColor(int red, int green, int blue) {
        red = Mth.clamp(red, 0, 255);
        green = Mth.clamp(green, 0, 255);
        blue = Mth.clamp(blue, 0, 255);
        return fogColor(ColorUtil.color(red, green, blue));
    }

    /**
     * Sets fog density for the biome.
     *
     * @param density fog density as a float, default value is 1.0F.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder fogDensity(float density) {
        this.fogDensity = density;
        return this;
    }

    /**
     * Sets generation chance for this biome.
     *
     * @param genChance
     * @return same {@link BCLBiomeBuilder}.
     */
    public BCLBiomeBuilder genChance(float genChance) {
        this.genChance = genChance;
        return this;
    }

    /**
     * Sets edge size for this biome.
     *
     * @param edgeSize size of the Edge (in Blocks)
     * @return same {@link BCLBiomeBuilder}.
     */
    public BCLBiomeBuilder edgeSize(int edgeSize) {
        this.edgeSize = edgeSize;
        return this;
    }

    /**
     * Sets edge-Biome for this biome.
     *
     * @param edge The Edge Biome
     * @return same {@link BCLBiomeBuilder}.
     */
    public BCLBiomeBuilder edge(BCLBiome edge) {
        this.edge = edge;
        return this;
    }


    /**
     * Sets edge-Biome for this biome.
     *
     * @param edge     The Edge Biome
     * @param edgeSize size of the Edge (in Blocks)
     * @return same {@link BCLBiomeBuilder}.
     */
    public BCLBiomeBuilder edge(BCLBiome edge, int edgeSize) {
        this.edge(edge);
        this.edgeSize(edgeSize);
        return this;
    }

    /**
     * Sets water color for the biome. Color is in ARGB int format.
     *
     * @param color ARGB color as integer.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder waterColor(int color) {
        getEffects().waterColor(color);
        return this;
    }

    /**
     * Sets water color for the biome. Color represented as red, green and blue channel values.
     *
     * @param red   red color component [0-255]
     * @param green green color component [0-255]
     * @param blue  blue color component [0-255]
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder waterColor(int red, int green, int blue) {
        red = Mth.clamp(red, 0, 255);
        green = Mth.clamp(green, 0, 255);
        blue = Mth.clamp(blue, 0, 255);
        return waterColor(ColorUtil.color(red, green, blue));
    }

    /**
     * Sets underwater fog color for the biome. Color is in ARGB int format.
     *
     * @param color ARGB color as integer.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder waterFogColor(int color) {
        getEffects().waterFogColor(color);
        return this;
    }

    /**
     * Sets underwater fog color for the biome. Color represented as red, green and blue channel values.
     *
     * @param red   red color component [0-255]
     * @param green green color component [0-255]
     * @param blue  blue color component [0-255]
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder waterFogColor(int red, int green, int blue) {
        red = Mth.clamp(red, 0, 255);
        green = Mth.clamp(green, 0, 255);
        blue = Mth.clamp(blue, 0, 255);
        return waterFogColor(ColorUtil.color(red, green, blue));
    }

    /**
     * Sets water and underwater fig color for the biome. Color is in ARGB int format.
     *
     * @param color ARGB color as integer.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder waterAndFogColor(int color) {
        return waterColor(color).waterFogColor(color);
    }

    /**
     * Sets water and underwater fig color for the biome. Color is in ARGB int format.
     *
     * @param red   red color component [0-255]
     * @param green green color component [0-255]
     * @param blue  blue color component [0-255]
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder waterAndFogColor(int red, int green, int blue) {
        red = Mth.clamp(red, 0, 255);
        green = Mth.clamp(green, 0, 255);
        blue = Mth.clamp(blue, 0, 255);
        return waterAndFogColor(ColorUtil.color(red, green, blue));
    }

    /**
     * Sets grass color for the biome. Color is in ARGB int format.
     *
     * @param color ARGB color as integer.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder grassColor(int color) {
        getEffects().grassColorOverride(color);
        return this;
    }

    /**
     * Sets grass color for the biome. Color represented as red, green and blue channel values.
     *
     * @param red   red color component [0-255]
     * @param green green color component [0-255]
     * @param blue  blue color component [0-255]
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder grassColor(int red, int green, int blue) {
        red = Mth.clamp(red, 0, 255);
        green = Mth.clamp(green, 0, 255);
        blue = Mth.clamp(blue, 0, 255);
        return grassColor(ColorUtil.color(red, green, blue));
    }

    /**
     * Sets leaves and plants color for the biome. Color is in ARGB int format.
     *
     * @param color ARGB color as integer.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder foliageColor(int color) {
        getEffects().foliageColorOverride(color);
        return this;
    }

    /**
     * Sets leaves and plants color for the biome. Color represented as red, green and blue channel values.
     *
     * @param red   red color component [0-255]
     * @param green green color component [0-255]
     * @param blue  blue color component [0-255]
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder foliageColor(int red, int green, int blue) {
        red = Mth.clamp(red, 0, 255);
        green = Mth.clamp(green, 0, 255);
        blue = Mth.clamp(blue, 0, 255);
        return foliageColor(ColorUtil.color(red, green, blue));
    }

    /**
     * Sets grass, leaves and all plants color for the biome. Color is in ARGB int format.
     *
     * @param color ARGB color as integer.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder plantsColor(int color) {
        return grassColor(color).foliageColor(color);
    }

    /**
     * Sets grass, leaves and all plants color for the biome. Color represented as red, green and blue channel values.
     *
     * @param red   red color component [0-255]
     * @param green green color component [0-255]
     * @param blue  blue color component [0-255]
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder plantsColor(int red, int green, int blue) {
        red = Mth.clamp(red, 0, 255);
        green = Mth.clamp(green, 0, 255);
        blue = Mth.clamp(blue, 0, 255);
        return plantsColor(ColorUtil.color(red, green, blue));
    }

    /**
     * Sets biome music, used for biomes in the Nether and End.
     *
     * @param music {@link Music} to use.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder music(Music music) {
        getEffects().backgroundMusic(music);
        return this;
    }

    /**
     * Sets biome music, used for biomes in the Nether and End.
     *
     * @param music {@link SoundEvent} to use.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder music(SoundEvent music) {
        return music(new Music(music, 600, 2400, true));
    }

    /**
     * Sets biome ambient loop sound. Can be used for biome environment.
     *
     * @param loopSound {@link SoundEvent} to use as a loop.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder loop(SoundEvent loopSound) {
        getEffects().ambientLoopSound(loopSound);
        return this;
    }

    /**
     * Sets biome mood sound. Can be used for biome environment.
     *
     * @param mood                {@link SoundEvent} to use as a mood.
     * @param tickDelay           delay between sound events in ticks.
     * @param blockSearchExtent   block search radius (for area available for sound).
     * @param soundPositionOffset offset in sound.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder mood(SoundEvent mood, int tickDelay, int blockSearchExtent, float soundPositionOffset) {
        getEffects().ambientMoodSound(new AmbientMoodSettings(mood, tickDelay, blockSearchExtent, soundPositionOffset));
        return this;
    }

    /**
     * Sets biome mood sound. Can be used for biome environment.
     *
     * @param mood {@link SoundEvent} to use as a mood.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder mood(SoundEvent mood) {
        return mood(mood, 6000, 8, 2.0F);
    }

    /**
     * Sets biome additionsl ambient sounds.
     *
     * @param additions {@link SoundEvent} to use.
     * @param intensity sound intensity. Default is 0.0111F.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder additions(SoundEvent additions, float intensity) {
        getEffects().ambientAdditionsSound(new AmbientAdditionsSettings(additions, intensity));
        return this;
    }

    /**
     * Sets biome additionsl ambient sounds.
     *
     * @param additions {@link SoundEvent} to use.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder additions(SoundEvent additions) {
        return additions(additions, 0.0111F);
    }

    /**
     * Adds new feature to the biome.
     *
     * @param decoration {@link Decoration} feature step.
     * @param feature    {@link PlacedFeature}.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder feature(Decoration decoration, Holder<PlacedFeature> feature) {
        getGeneration().addFeature(decoration, feature);
        return this;
    }

    /**
     * Adds vanilla Mushrooms.
     *
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder defaultMushrooms() {
        return feature(BiomeDefaultFeatures::addDefaultMushrooms);
    }

    /**
     * Adds vanilla Nether Ores.
     *
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder netherDefaultOres() {
        return feature(BiomeDefaultFeatures::addNetherDefaultOres);
    }

    /**
     * Will add features into biome, used for vanilla feature adding functions.
     *
     * @param featureAdd {@link Consumer} with {@link BiomeGenerationSettings.Builder}.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder feature(Consumer<BiomeGenerationSettings.Builder> featureAdd) {
        featureAdd.accept(getGeneration());
        return this;
    }

    /**
     * Adds new feature to the biome.
     *
     * @param feature {@link BCLFeature}.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder feature(BCLFeature feature) {
        return feature(feature.getDecoration(), feature.getPlacedFeature());
    }

    /**
     * Adds new feature to the biome.
     *
     * @param feature {@link BCLFeature}.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder feature(org.betterx.bclib.api.v3.levelgen.features.BCLFeature feature) {
        return feature(feature.decoration, feature.placedFeature);
    }

    /**
     * Adds new structure feature into the biome.
     *
     * @param structureTag {@link TagKey} to add.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder structure(TagKey<Biome> structureTag) {
        tags.add(structureTag);
        return this;
    }

    /**
     * Adds new structure feature into thr biome. Will add building biome into the structure list.
     *
     * @param structure {@link BCLStructure} to add.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder structure(BCLStructure structure) {
        structure.addInternalBiome(biomeID);
        return structure(structure.biomeTag);
    }

    /**
     * Adds new world carver into the biome.
     *
     * @param carver {@link ConfiguredWorldCarver} to add.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder carver(GenerationStep.Carving step, Holder<? extends ConfiguredWorldCarver<?>> carver) {
        final ResourceLocation immutableID = biomeID;
        var oKey = carver.unwrapKey();
        if (oKey.isPresent()) {
            BiomeModifications.addCarver(
                    ctx -> ctx.getBiomeKey().location().equals(immutableID),
                    step,
                    (ResourceKey<ConfiguredWorldCarver<?>>) oKey.get()
            );
        }
        //carvers.add(new Pair<>(step, carver));
        return this;
    }

    /**
     * Adds new world surface rule for the given block
     *
     * @param surfaceBlock {@link Block} to use.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder surface(Block surfaceBlock) {
        return surface(surfaceBlock.defaultBlockState());
    }

    /**
     * Adds new world surface rule for the given block
     *
     * @param surfaceBlock {@link BlockState} to use.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder surface(BlockState surfaceBlock) {
        return surface(SurfaceRuleBuilder.start().surface(surfaceBlock).build());
    }

    /**
     * Adds blocks to the biome surface and below it (with specified depth).
     *
     * @param surfaceBlock    {@link Block} that will cover biome.
     * @param subterrainBlock {@link Block} below it with specified depth.
     * @param depth           thickness of bottom block layer.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder surface(Block surfaceBlock, Block subterrainBlock, int depth) {
        return surface(SurfaceRuleBuilder
                .start()
                .surface(surfaceBlock.defaultBlockState())
                .subsurface(subterrainBlock.defaultBlockState(), depth)
                .build());
    }

    /**
     * Adds surface rule to this biome.
     *
     * @param newSurfaceRule {link SurfaceRules.RuleSource} surface rule.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder surface(SurfaceRules.RuleSource newSurfaceRule) {
        this.surfaceRule = newSurfaceRule;
        return this;
    }

    public BCLBiomeBuilder tag(TagKey<Biome>... tag) {
        for (TagKey<Biome> t : tag) {
            tags.add(t);
        }
        return this;
    }

    /**
     * Set terrain height for the biome. Can be used in custom generators, doesn't change vanilla biome distribution or generation.
     *
     * @param height a relative float terrain height value.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder terrainHeight(float height) {
        this.height = height;
        return this;
    }


    /**
     * Make this a vertical Biome
     *
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder vertical() {
        this.vertical = vertical;
        return this;
    }

    /**
     * Finalize biome creation.
     *
     * @return created {@link BCLBiome} instance.
     */
    public BCLBiome build() {
        return build((BiomeSupplier<BCLBiome>) BCLBiome::new);
    }

    /**
     * Finalize biome creation.
     *
     * @param biomeConstructor {@link BiFunction} biome constructor.
     * @return created {@link BCLBiome} instance.
     */
    @Deprecated(forRemoval = true)
    public <T extends BCLBiome> T build(BiFunction<ResourceLocation, Biome, T> biomeConstructor) {
        return build((id, biome, settings) -> biomeConstructor.apply(id, biome));
    }

    private static BiomeGenerationSettings fixGenerationSettings(BiomeGenerationSettings settings) {
        //Fabric Biome Modification API can not handle an empty carver map, thus we will create one with
        //an empty HolderSet for every possible step:
        //https://github.com/FabricMC/fabric/issues/2079
        //TODO: Remove, once fabric gets fixed
        if (settings instanceof BiomeGenerationSettingsAccessor acc) {
            Map<GenerationStep.Carving, HolderSet<ConfiguredWorldCarver<?>>> carvers = CollectionsUtil.getMutable(acc.bclib_getCarvers());
            for (GenerationStep.Carving step : GenerationStep.Carving.values()) {
                carvers.computeIfAbsent(step, __ -> HolderSet.direct(Lists.newArrayList()));
            }
            acc.bclib_setCarvers(carvers);
        }
        return settings;
    }

    /**
     * Finalize biome creation.
     *
     * @param biomeConstructor {@link BiomeSupplier} biome constructor.
     * @return created {@link BCLBiome} instance.
     */
    public <T extends BCLBiome> T build(BiomeSupplier<T> biomeConstructor) {
        BiomeBuilder builder = new BiomeBuilder()
                .precipitation(precipitation)
                .temperature(temperature)
                .downfall(downfall);

        builder.mobSpawnSettings(getSpawns().build());
        builder.specialEffects(getEffects().build());

        builder.generationSettings(fixGenerationSettings(getGeneration().build()));

        BCLBiomeSettings settings = BCLBiomeSettings.createBCL()
                                                    .setTerrainHeight(height)
                                                    .setFogDensity(fogDensity)
                                                    .setGenChance(genChance)
                                                    .setEdgeSize(edgeSize)
                                                    .setEdge(edge)
                                                    .setVertical(vertical)
                                                    .build();

        final Biome biome = builder.build();
        final T res = biomeConstructor.apply(biomeID, biome, settings);
        res.addBiomeTags(tags);
        res.setSurface(surfaceRule);
        res.addClimateParameters(parameters);

        //carvers.forEach(cfg -> BiomeAPI.addBiomeCarver(biome, cfg.second, cfg.first));
        return res;
    }

    /**
     * Get or create {@link BiomeSpecialEffects.Builder} for biome visual effects.
     * For internal usage only.
     * For internal usage only.
     *
     * @return new or same {@link BiomeSpecialEffects.Builder} instance.
     */
    private BiomeSpecialEffects.Builder getEffects() {
        if (effectsBuilder == null) {
            effectsBuilder = new BiomeSpecialEffects.Builder();
        }
        return effectsBuilder;
    }

    /**
     * Get or create {@link MobSpawnSettings.Builder} for biome mob spawning.
     * For internal usage only.
     *
     * @return new or same {@link MobSpawnSettings.Builder} instance.
     */
    private MobSpawnSettings.Builder getSpawns() {
        if (spawnSettings == null) {
            spawnSettings = new MobSpawnSettings.Builder();
        }
        return spawnSettings;
    }

    /**
     * Get or create {@link BiomeGenerationSettings.Builder} for biome features and generation.
     * For internal usage only.
     *
     * @return new or same {@link BiomeGenerationSettings.Builder} instance.
     */
    private BiomeGenerationSettings.Builder getGeneration() {
        if (generationSettings == null) {
            generationSettings = new BiomeGenerationSettings.Builder();
        }
        return generationSettings;
    }
}
