package org.betterx.worlds.together.worldPreset;

import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.levelgen.WorldGenUtil;
import org.betterx.worlds.together.mixin.common.WorldPresetAccessor;
import org.betterx.worlds.together.world.WorldConfig;
import org.betterx.worlds.together.world.event.WorldBootstrap;
import org.betterx.worlds.together.worldPreset.settings.VanillaWorldPresetSettings;
import org.betterx.worlds.together.worldPreset.settings.WorldPresetSettings;

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
    public final WorldPresetSettings settings;
    public final int sortOrder;

    private static final String TAG_GENERATOR = WorldGenUtil.TAG_GENERATOR;

    private static int NEXT_IN_SORT_ORDER = 1000;

    public TogetherWorldPreset(
            Map<ResourceKey<LevelStem>, LevelStem> map,
            Optional<Integer> sortOrder,
            Optional<WorldPresetSettings> settings
    ) {
        this(map, sortOrder.orElse(NEXT_IN_SORT_ORDER++), settings.orElse(VanillaWorldPresetSettings.DEFAULT));
    }

    public TogetherWorldPreset(
            Map<ResourceKey<LevelStem>, LevelStem> map,
            int sortOrder,
            WorldPresetSettings settings
    ) {
        super(map);
        this.sortOrder = sortOrder;
        this.settings = settings;
    }

    public TogetherWorldPreset withSettings(WorldPresetSettings settings) {
        return new TogetherWorldPreset(getDimensions(), sortOrder, settings);
    }

    private Map<ResourceKey<LevelStem>, LevelStem> getDimensions() {
        return ((WorldPresetAccessor) this).bcl_getDimensions();
    }

    public LevelStem getDimension(ResourceKey<LevelStem> key) {
        return getDimensions().get(key);
    }

    public static void writeWorldPresetSettings(Map<ResourceKey<LevelStem>, LevelStem> settings) {
        final RegistryOps<Tag> registryOps = RegistryOps.create(
                NbtOps.INSTANCE,
                WorldBootstrap.getLastRegistryAccess()
        );
        DimensionsWrapper wrapper = new DimensionsWrapper(DimensionsWrapper.build(settings));
        final var encodeResult = wrapper.CODEC.encodeStart(registryOps, wrapper);

        if (encodeResult.result().isPresent()) {
            final CompoundTag settingsNbt = WorldConfig.getRootTag(WorldsTogether.MOD_ID);
            settingsNbt.put(TAG_GENERATOR, encodeResult.result().get());
        } else {
            WorldsTogether.LOGGER.error("Unable to encode world generator settings for level.dat.");
        }

        WorldConfig.saveFile(WorldsTogether.MOD_ID);
    }

    private static DimensionsWrapper DEFAULT_DIMENSIONS_WRAPPER = null;

    public static @NotNull Map<ResourceKey<LevelStem>, ChunkGenerator> getWorldDimensions() {
        if (BuiltinRegistries.ACCESS == null) return null;
        final RegistryAccess registryAccess;
        if (WorldBootstrap.getLastRegistryAccess() != null) {
            registryAccess = WorldBootstrap.getLastRegistryAccess();
        } else {
            registryAccess = BuiltinRegistries.ACCESS;
        }
        final RegistryOps<Tag> registryOps = RegistryOps.create(NbtOps.INSTANCE, registryAccess);

        Optional<DimensionsWrapper> oLevelStem = DimensionsWrapper.CODEC
                .parse(new Dynamic<>(registryOps, WorldGenUtil.getSettingsNbt()))
                .resultOrPartial(WorldsTogether.LOGGER::error);

        if (DEFAULT_DIMENSIONS_WRAPPER == null) {
            DEFAULT_DIMENSIONS_WRAPPER = new DimensionsWrapper(WorldPresets
                    .get(
                            registryAccess,
                            WorldPresets.DEFAULT.orElseThrow()
                    )
                    .value()
                    .createWorldGenSettings(0, true, true)
                    .dimensions());
        }

        return oLevelStem.orElse(DEFAULT_DIMENSIONS_WRAPPER).dimensions;
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
