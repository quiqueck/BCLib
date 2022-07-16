package org.betterx.worlds.together.worldPreset;

import org.betterx.bclib.BCLib;
import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.levelgen.WorldGenUtil;
import org.betterx.worlds.together.mixin.common.WorldPresetAccessor;
import org.betterx.worlds.together.world.WorldConfig;
import org.betterx.worlds.together.world.event.WorldBootstrap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class TogetherWorldPreset extends WorldPreset {
    public final int sortOrder;

    private static int NEXT_IN_SORT_ORDER = 1000;

    public TogetherWorldPreset(
            Map<ResourceKey<LevelStem>, LevelStem> map,
            Optional<Integer> sortOrder
    ) {
        this(map, sortOrder.orElse(NEXT_IN_SORT_ORDER++));
    }

    public TogetherWorldPreset(
            Map<ResourceKey<LevelStem>, LevelStem> map,
            int sortOrder
    ) {
        super(map);
        this.sortOrder = sortOrder;
    }

    public TogetherWorldPreset withDimensions(Registry<LevelStem> dimensions) {
        Map<ResourceKey<LevelStem>, LevelStem> map = new HashMap<>();
        for (var entry : dimensions.entrySet()) {
            ResourceKey<LevelStem> key = entry.getKey();
            LevelStem stem = entry.getValue();
            map.put(key, stem);
        }
        return new TogetherWorldPreset(map, sortOrder);
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
        final RegistryAccess registryAccess = WorldBootstrap.getLastRegistryAccessOrElseBuiltin();
        if (registryAccess == BuiltinRegistries.ACCESS) {
            BCLib.LOGGER.info("Loading from builtin Registry");
        }
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
    }

    public static Registry<LevelStem> getDimensions(ResourceKey<WorldPreset> key) {
        RegistryAccess access = WorldBootstrap.getLastRegistryAccessOrElseBuiltin();
        var preset = access.registryOrThrow(Registry.WORLD_PRESET_REGISTRY).getHolder(key);
        if (preset.isEmpty()) return null;
        return preset
                .get()
                .value()
                .createWorldGenSettings(
                        0,
                        true,
                        true
                )
                .dimensions();
    }

    public static @NotNull Map<ResourceKey<LevelStem>, ChunkGenerator> getDimensionsMap(ResourceKey<WorldPreset> key) {
        Registry<LevelStem> reg = getDimensions(key);
        if (reg == null) return new HashMap<>();
        return DimensionsWrapper.build(reg);
    }

    private static class DimensionsWrapper {
        public static final Codec<DimensionsWrapper> CODEC = RecordCodecBuilder.create(instance -> instance
                .group(Codec.unboundedMap(
                                    ResourceKey.codec(Registry.LEVEL_STEM_REGISTRY),
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
