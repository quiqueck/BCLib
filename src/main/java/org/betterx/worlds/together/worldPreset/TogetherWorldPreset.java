package org.betterx.worlds.together.worldPreset;

import org.betterx.bclib.BCLib;
import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.levelgen.WorldGenUtil;
import org.betterx.worlds.together.mixin.common.WorldPresetAccessor;
import org.betterx.worlds.together.world.WorldConfig;
import org.betterx.worlds.together.world.event.WorldBootstrap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TogetherWorldPreset extends WorldPreset {
    public final int sortOrder;

    private static int NEXT_IN_SORT_ORDER = 1000;
    private final WorldDimensions worldDimensions;
    @Nullable
    public final ResourceKey<WorldPreset> parentKey;

    public TogetherWorldPreset(
            Map<ResourceKey<LevelStem>, LevelStem> map,
            Optional<Integer> sortOrder
    ) {
        this(map, sortOrder.orElse(NEXT_IN_SORT_ORDER++), null);
    }

    public TogetherWorldPreset(
            Map<ResourceKey<LevelStem>, LevelStem> map,
            Optional<Integer> sortOrder,
            @Nullable ResourceKey<WorldPreset> parentKey
    ) {
        this(map, sortOrder.orElse(NEXT_IN_SORT_ORDER++), parentKey);
    }

    public TogetherWorldPreset(
            Map<ResourceKey<LevelStem>, LevelStem> map,
            int sortOrder
    ) {
        this(map, sortOrder, null);
    }

    public TogetherWorldPreset(
            Map<ResourceKey<LevelStem>, LevelStem> map,
            int sortOrder,
            @Nullable ResourceKey<WorldPreset> parentKey
    ) {
        super(map);
        this.sortOrder = sortOrder;
        this.worldDimensions = buildWorldDimensions(map);
        this.parentKey = parentKey;
    }

    public static WorldDimensions buildWorldDimensions(Map<ResourceKey<LevelStem>, LevelStem> map) {
        Registry<LevelStem> registry = new MappedRegistry<>(Registries.LEVEL_STEM, Lifecycle.experimental());
        for (var entry : map.entrySet()) {
            Registry.register(registry, entry.getKey(), entry.getValue());
        }

        return new WorldDimensions(registry);
    }

    public WorldDimensions getWorldDimensions() {
        return this.worldDimensions;
    }

    public TogetherWorldPreset withDimensions(
            Registry<LevelStem> dimensions,
            @Nullable ResourceKey<WorldPreset> parentKey
    ) {
        Map<ResourceKey<LevelStem>, LevelStem> map = new HashMap<>();
        for (var entry : dimensions.entrySet()) {
            ResourceKey<LevelStem> key = entry.getKey();
            LevelStem stem = entry.getValue();
            map.put(key, stem);
        }
        return new TogetherWorldPreset(map, sortOrder, parentKey);
    }

    private Map<ResourceKey<LevelStem>, LevelStem> getDimensions() {
        return ((WorldPresetAccessor) this).bcl_getDimensions();
    }

    public Map<ResourceKey<LevelStem>, ChunkGenerator> getDimensionsMap() {
        return DimensionsWrapper.build(getDimensions());
    }

    public LevelStem getDimension(ResourceKey<LevelStem> key) {
        return getDimensions().get(key);
    }

    public static void writeWorldPresetSettings(WorldDimensions dimensions) {
        writeWorldPresetSettings(dimensions.dimensions());
    }

    public static void writeWorldPresetSettings(Registry<LevelStem> dimensions) {
        DimensionsWrapper wrapper = new DimensionsWrapper(dimensions);
        writeWorldPresetSettings(wrapper);
    }

    public static void writeWorldPresetSettings(Map<ResourceKey<LevelStem>, LevelStem> settings) {
        DimensionsWrapper wrapper = new DimensionsWrapper(DimensionsWrapper.build(settings));
        writeWorldPresetSettings(wrapper);
    }

    public static void writeWorldPresetSettingsDirect(Map<ResourceKey<LevelStem>, ChunkGenerator> settings) {
        DimensionsWrapper wrapper = new DimensionsWrapper(settings);
        writeWorldPresetSettings(wrapper);
    }

    private static void writeWorldPresetSettings(DimensionsWrapper wrapper) {
        final RegistryOps<Tag> registryOps = RegistryOps.create(
                NbtOps.INSTANCE,
                WorldBootstrap.getLastRegistryAccessOrElseBuiltin()
        );
        final var encodeResult = DimensionsWrapper.CODEC.encodeStart(registryOps, wrapper);

        if (encodeResult.result().isPresent()) {
            final CompoundTag settingsNbt = WorldConfig.getRootTag(WorldsTogether.MOD_ID);
            settingsNbt.put(WorldGenUtil.TAG_PRESET, encodeResult.result().get());
        } else {
            WorldsTogether.LOGGER.error("Unable to encode world generator settings for level.dat.");
        }

        WorldConfig.saveFile(WorldsTogether.MOD_ID);
    }

    private static DimensionsWrapper DEFAULT_DIMENSIONS_WRAPPER = null;

    public static @NotNull Map<ResourceKey<LevelStem>, ChunkGenerator> loadWorldDimensions() {
        try {
            final RegistryAccess registryAccess = WorldBootstrap.getLastRegistryAccessOrElseBuiltin();
            final RegistryOps<Tag> registryOps = RegistryOps.create(NbtOps.INSTANCE, registryAccess);
            if (DEFAULT_DIMENSIONS_WRAPPER == null) {
                DEFAULT_DIMENSIONS_WRAPPER = new DimensionsWrapper(TogetherWorldPreset.getDimensionsMap(WorldPresets.getDEFAULT()));
            }

            CompoundTag presetNBT = WorldGenUtil.getPresetsNbt();
            if (!presetNBT.contains("dimensions")) {
                return DEFAULT_DIMENSIONS_WRAPPER.dimensions;
            }

            Optional<DimensionsWrapper> oLevelStem = DimensionsWrapper.CODEC
                    .parse(new Dynamic<>(registryOps, presetNBT))
                    .resultOrPartial(WorldsTogether.LOGGER::error);


            return oLevelStem.orElse(DEFAULT_DIMENSIONS_WRAPPER).dimensions;
        } catch (Exception e) {
            BCLib.LOGGER.error("Failed to load Dimensions", e);
            return DEFAULT_DIMENSIONS_WRAPPER.dimensions;
        }
    }

    public static @Nullable Registry<LevelStem> getDimensions(ResourceKey<WorldPreset> key) {
        RegistryAccess access = WorldBootstrap.getLastRegistryAccessOrElseBuiltin();
        if (access == null) {
            WorldsTogether.LOGGER.error("No valid registry found!");
            return null;
        }
        var preset = access.registryOrThrow(Registries.WORLD_PRESET).getHolder(key);
        if (preset.isEmpty()) return null;
        return preset
                .get()
                .value()
                .createWorldDimensions()
                .dimensions();
    }

    public static @NotNull Map<ResourceKey<LevelStem>, ChunkGenerator> getDimensionsMap(ResourceKey<WorldPreset> key) {
        Registry<LevelStem> reg = getDimensions(key);
        if (reg == null) return new HashMap<>();
        return DimensionsWrapper.build(reg);
    }

    public static @NotNull Map<ResourceKey<LevelStem>, ChunkGenerator> getDimensionMap(WorldDimensions worldDims) {
        return DimensionsWrapper.build(worldDims.dimensions());
    }

    public static @NotNull WorldDimensions getWorldDimensions(ResourceKey<WorldPreset> key) {
        Registry<LevelStem> reg = getDimensions(key);
        return new WorldDimensions(reg);
    }

    private static class DimensionsWrapper {
        public static final Codec<DimensionsWrapper> CODEC = RecordCodecBuilder.create(instance -> instance
                .group(Codec.unboundedMap(
                                    ResourceKey.codec(Registries.LEVEL_STEM),
                                    ChunkGenerator.CODEC
                            )
                            .fieldOf("dimensions")
                            .orElse(new HashMap<>())
                            .forGetter(o -> o.dimensions))
                .apply(instance, DimensionsWrapper::new));
        final Map<ResourceKey<LevelStem>, ChunkGenerator> dimensions;

        static Map<ResourceKey<LevelStem>, ChunkGenerator> build(Registry<LevelStem> dimensions) {
            Map<ResourceKey<LevelStem>, ChunkGenerator> map = new HashMap<>();
            for (var entry : dimensions.entrySet()) {
                ResourceKey<LevelStem> key = entry.getKey();
                LevelStem stem = entry.getValue();
                map.put(key, stem.generator());
            }
            return map;
        }

        static Map<ResourceKey<LevelStem>, ChunkGenerator> build(Map<ResourceKey<LevelStem>, LevelStem> input) {
            Map<ResourceKey<LevelStem>, ChunkGenerator> map = new HashMap<>();
            for (var entry : input.entrySet()) {
                ResourceKey<LevelStem> key = entry.getKey();
                LevelStem stem = entry.getValue();
                map.put(key, stem.generator());
            }
            return map;
        }


        DimensionsWrapper(Registry<LevelStem> dimensions) {
            this(build(dimensions));
        }

        private DimensionsWrapper(Map<ResourceKey<LevelStem>, ChunkGenerator> dimensions) {
            this.dimensions = dimensions;
        }
    }
}
