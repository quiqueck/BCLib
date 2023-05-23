package org.betterx.bclib.api.v2.levelgen.biomes;

import org.betterx.bclib.api.v2.levelgen.structures.BCLStructure;
import org.betterx.bclib.api.v2.levelgen.surface.SurfaceRuleBuilder;
import org.betterx.bclib.api.v3.levelgen.features.BCLFeature;
import org.betterx.bclib.entity.BCLEntityWrapper;
import org.betterx.bclib.mixin.common.BiomeGenerationSettingsAccessor;
import org.betterx.bclib.util.CollectionsUtil;
import org.betterx.bclib.util.Pair;
import org.betterx.ui.ColorUtil;
import org.betterx.worlds.together.surfaceRules.SurfaceRuleRegistry;
import org.betterx.worlds.together.tag.v3.TagManager;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
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

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class BCLBiomeBuilder {
    static final ConcurrentLinkedQueue<UnboundBCLBiome<?>> UNBOUND_BIOMES = new ConcurrentLinkedQueue<>();

    public static int calculateSkyColor(float temperature) {
        return OverworldBiomes.calculateSkyColor(temperature);
    }

    public static int DEFAULT_NETHER_WATER_COLOR = 0x3F76E4;
    public static int DEFAULT_END_WATER_COLOR = DEFAULT_NETHER_WATER_COLOR;
    public static int DEFAULT_NETHER_WATER_FOG_COLOR = 0x050533;
    public static int DEFAULT_END_WATER_FOG_COLOR = DEFAULT_NETHER_WATER_FOG_COLOR;
    public static int DEFAULT_END_FOG_COLOR = 0xA080A0;
    public static int DEFAULT_END_SKY_COLOR = 0x000000;
    public static float DEFAULT_NETHER_TEMPERATURE = 2.0f;
    public static float DEFAULT_END_TEMPERATURE = 0.5f;
    public static float DEFAULT_NETHER_WETNESS = 0.0f;
    public static float DEFAULT_END_WETNESS = 0.5f;


    @FunctionalInterface
    public interface BiomeSupplier<T> extends BiFunction<ResourceKey<Biome>, BCLBiomeSettings, T> {
    }

    @FunctionalInterface
    private interface FeatureSupplier extends Consumer<BiomeGenerationSettings.Builder> {
    }

    @FunctionalInterface
    interface BuildCompletion extends Function<BootstapContext<Biome>, Biome> {
    }

    private static final SurfaceRules.ConditionSource SURFACE_NOISE = SurfaceRules.noiseCondition(
            Noises.SOUL_SAND_LAYER,
            -0.012
    );

    private final List<FeatureSupplier> featureSupliers = new LinkedList<>();
    private final List<Pair<GenerationStep.Carving, Holder<? extends ConfiguredWorldCarver<?>>>> carvers = new ArrayList<>(
            1);
    private BiomeGenerationSettings.Builder generationSettings;
    private BiomeSpecialEffects.Builder effectsBuilder;
    private MobSpawnSettings.Builder spawnSettings;
    private SurfaceRules.RuleSource surfaceRule;
    private boolean hasPrecipitation;
    final private ResourceLocation biomeID;


    private final Set<TagKey<Biome>> tags = Sets.newHashSet();

    private final List<Climate.ParameterPoint> parameters = Lists.newArrayList();

    private float temperature;
    private float fogDensity;
    private float genChance;
    private float downfall;
    private float height;
    private int edgeSize;
    private BCLBiome edge;
    private BCLBiome parent;
    private boolean vertical;

    private BiomeAPI.BiomeType biomeType;


    BCLBiomeBuilder(ResourceLocation biomeID) {
        this.biomeID = biomeID;
        this.hasPrecipitation = false;
        this.generationSettings = null;
        this.effectsBuilder = null;
        this.spawnSettings = null;
        this.temperature = 1.0F;
        this.fogDensity = 1.0F;
        this.edgeSize = 0;
        this.downfall = 1.0F;
        this.genChance = 1.0F;
        this.height = 0.1F;
        this.vertical = false;
        this.edge = null;
        this.parent = null;
        this.biomeType = null;
    }

    /**
     * Starts new biome building process.
     *
     * @param biomeID {@link ResourceLocation} biome identifier.
     * @return prepared {@link BCLBiomeBuilder} instance.
     */
    public static BCLBiomeBuilder start(
            ResourceLocation biomeID
    ) {
        return new BCLBiomeBuilder(biomeID);
    }

    public BCLBiomeBuilder addNetherClimateParamater(float temperature, float humidity, float offset) {
        parameters.add(Climate.parameters(temperature, humidity, 0, 0, 0, 0, offset));
        return this;
    }

    public BCLBiomeBuilder addNetherClimateParamater(float temperature, float humidity) {
        return addNetherClimateParamater(temperature, humidity, 0);
    }

    public BCLBiomeBuilder parentBiome(BCLBiome parent) {
        this.parent = parent;
        return this;
    }

    /**
     * Set the type for this Biome. If the type was set, the Biome can be registered.
     *
     * @param type selected Type
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder type(BiomeAPI.BiomeType type) {
        this.biomeType = type;
        return this;
    }

    /**
     * Set biome {@link Precipitation}. Affect biome visual effects (rain, snow, none).
     *
     * @param precipitation {@link Precipitation}
     * @return same {@link BCLBiomeBuilder} instance.
     * @deprecated Use hasPrecipitation() instead
     */
    @Deprecated(forRemoval = true)
    public BCLBiomeBuilder precipitation(Precipitation precipitation) {
        return hasPrecipitation(precipitation != Precipitation.NONE);
    }

    /**
     * Set biome {@link Precipitation}. Affect biome visual effects (rain, snow, none).
     *
     * @param precipitation true, if this biome can have presipitation. Rain/Snow is determined by temperature
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder hasPrecipitation(boolean precipitation) {
        this.hasPrecipitation = precipitation;
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
    public BCLBiomeBuilder music(Holder<SoundEvent> music) {
        return music(new Music(music, 600, 2400, true));
    }

    /**
     * Sets biome ambient loop sound. Can be used for biome environment.
     *
     * @param loopSound {@link SoundEvent} to use as a loop.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder loop(Holder<SoundEvent> loopSound) {
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
    public BCLBiomeBuilder mood(
            Holder<SoundEvent> mood,
            int tickDelay,
            int blockSearchExtent,
            float soundPositionOffset
    ) {
        getEffects().ambientMoodSound(new AmbientMoodSettings(mood, tickDelay, blockSearchExtent, soundPositionOffset));
        return this;
    }

    /**
     * Sets biome mood sound. Can be used for biome environment.
     *
     * @param mood {@link SoundEvent} to use as a mood.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder mood(Holder<SoundEvent> mood) {
        return mood(mood, 6000, 8, 2.0F);
    }

    /**
     * Sets biome additionsl ambient sounds.
     *
     * @param additions {@link SoundEvent} to use.
     * @param intensity sound intensity. Default is 0.0111F.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder additions(Holder<SoundEvent> additions, float intensity) {
        getEffects().ambientAdditionsSound(new AmbientAdditionsSettings(additions, intensity));
        return this;
    }

    /**
     * Sets biome additionsl ambient sounds.
     *
     * @param additions {@link SoundEvent} to use.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder additions(Holder<SoundEvent> additions) {
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
        featureSupliers.add(gen -> gen.addFeature(decoration, feature));
        return this;
    }

    /**
     * Adds new feature to the biome.
     *
     * @param decoration {@link Decoration} feature step.
     * @param feature    {@link PlacedFeature}.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder feature(Decoration decoration, ResourceKey<PlacedFeature> feature) {
        featureSupliers.add(gen -> gen.addFeature(decoration, feature));
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
        featureSupliers.add(gen -> featureAdd.accept(gen));
        return this;
    }


    /**
     * Adds new feature to the biome.
     *
     * @param feature {@link BCLFeature}.
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder feature(BCLFeature feature) {
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
    public BCLBiomeBuilder carver(GenerationStep.Carving step, Holder<ConfiguredWorldCarver<?>> carver) {
        final Optional<ResourceKey<ConfiguredWorldCarver<?>>> oKey = carver.unwrapKey();
        if (oKey.isPresent()) {
            return carver(step, oKey.get());
        }

        return this;
    }

    public BCLBiomeBuilder carver(
            GenerationStep.Carving step,
            ResourceKey<ConfiguredWorldCarver<?>> carverKey
    ) {
        BiomeModifications.addCarver(
                ctx -> ctx.getBiomeKey().location().equals(biomeID),
                step,
                carverKey
        );
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

    /**
     * Changes the type for the Biome. The intended Type defines in which Dimension a
     * Biome is allowed to spawn. Currently each Biome can only spawn in one dimension
     *
     * @param type The intended type
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder intendedType(BiomeAPI.BiomeType type) {
        this.biomeType = type;
        return this;
    }


    /**
     * Changes the intended type for the Biome to an EndLand Biome
     *
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder endLandBiome() {
        return intendedType(BiomeAPI.BiomeType.BCL_END_LAND);
    }

    /**
     * Changes the intended type for the Biome to an EndVoid (aka small islands) Biome
     *
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder endVoidBiome() {
        return intendedType(BiomeAPI.BiomeType.BCL_END_VOID);
    }

    /**
     * Changes the intended type for the Biome to an Endbarrens Biome
     *
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder endBarrensBiome() {
        return intendedType(BiomeAPI.BiomeType.BCL_END_BARRENS);
    }

    /**
     * Changes the intended type for the Biome to an End Center Island Biome
     *
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder endCenterBiome() {
        return intendedType(BiomeAPI.BiomeType.BCL_END_CENTER);
    }

    /**
     * Changes the intended type for the Biome to a Nether Biome
     *
     * @return same {@link BCLBiomeBuilder} instance.
     */
    public BCLBiomeBuilder netherBiome() {
        return intendedType(BiomeAPI.BiomeType.BCL_NETHER);
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
        this.vertical = true;
        return this;
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
    private BiomeGenerationSettings.Builder getGeneration(BootstapContext<Biome> bootstrapContext) {

        if (generationSettings == null) {
            generationSettings = new BiomeGenerationSettings.Builder(
                    bootstrapContext.lookup(Registries.PLACED_FEATURE),
                    bootstrapContext.lookup(Registries.CONFIGURED_CARVER)
            );
        }
        return generationSettings;
    }

    /**
     * Finalize biome creation.
     *
     * @return created {@link BCLBiome} instance.
     */
    public BCLBiomeContainer<BCLBiome> build() {
        return build(BCLBiome::new);
    }


    /**
     * Finalize biome creation.
     *
     * @param biomeConstructor {@link BiomeSupplier} biome constructor.
     * @return created {@link BCLBiome} instance.
     */
    public <T extends BCLBiome> BCLBiomeContainer<T> build(BiomeSupplier<T> biomeConstructor) {
        BCLBiomeSettings settings = BCLBiomeSettings.createBCL()
                                                    .setTerrainHeight(height)
                                                    .setFogDensity(fogDensity)
                                                    .setGenChance(genChance)
                                                    .setEdgeSize(edgeSize)
                                                    .setVertical(vertical)
                                                    .build();
        final T bclBiome = biomeConstructor.apply(ResourceKey.create(Registries.BIOME, biomeID), settings);
        tags.forEach(tagKey -> TagManager.BIOMES.add(tagKey, bclBiome.getBiomeKey()));

        SurfaceRuleRegistry.registerRule(biomeID, surfaceRule, biomeID);
        bclBiome.addClimateParameters(parameters);
        if (biomeType != null)
            bclBiome._setIntendedType(biomeType);


        BiomeBuilder builder = new BiomeBuilder()
                .hasPrecipitation(hasPrecipitation)
                .temperature(temperature)
                .downfall(downfall);

        builder.mobSpawnSettings(getSpawns().build());
        builder.specialEffects(getEffects().build());

        if (edge != null) {
            bclBiome._setEdge(edge);
        }

        //res.addBiomeTags(tags);
        //res.setSurface(surfaceRule);

        //carvers.forEach(cfg -> BiomeAPI.addBiomeCarver(biome, cfg.second, cfg.first));
        final UnboundBCLBiome<T> unbound = new UnboundBCLBiome<>(
                bclBiome,
                parent,
                ctx -> {
                    BiomeGenerationSettings.Builder genBuilder = getGeneration(ctx);
                    featureSupliers.forEach(s -> s.accept(genBuilder));
                    return builder.generationSettings(fixGenerationSettings(genBuilder.build())).build();
                }
        );

        UNBOUND_BIOMES.add(unbound);
        return unbound;
    }

    public static void registerUnbound(BootstapContext<Biome> context) {
        UNBOUND_BIOMES.forEach(u -> u.register(context));
        UNBOUND_BIOMES.clear();
    }
}
