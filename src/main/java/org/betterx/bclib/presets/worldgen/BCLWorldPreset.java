package org.betterx.bclib.presets.worldgen;

import org.betterx.bclib.mixin.common.WorldPresetAccessor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import java.util.Map;

public class BCLWorldPreset extends WorldPreset {
    public final WorldPresetSettings settings;
    public final int sortOrder;
    public static final Codec<BCLWorldPreset> DIRECT_CODEC = RecordCodecBuilder.create(builderInstance -> {
        RecordCodecBuilder<BCLWorldPreset, Map<ResourceKey<LevelStem>, LevelStem>> dimensionsBuidler = Codec
                .unboundedMap(ResourceKey.codec(Registry.LEVEL_STEM_REGISTRY), LevelStem.CODEC)
                .fieldOf("dimensions")
                .forGetter(worldPreset -> worldPreset.getDimensions());

        RecordCodecBuilder<BCLWorldPreset, Integer> sortBuilder = Codec.INT
                .fieldOf("sort_order")
                .forGetter(wp -> wp.sortOrder);

        RecordCodecBuilder<BCLWorldPreset, WorldPresetSettings> settingsBuilder = WorldPresetSettings.CODEC
                .fieldOf("settings")
                .forGetter(wp -> wp.settings);

        return builderInstance
                .group(dimensionsBuidler, sortBuilder, settingsBuilder)
                .apply(builderInstance, BCLWorldPreset::new);
    });

    public static final Codec<Holder<WorldPreset>> CODEC = RegistryFileCodec.create(
            Registry.WORLD_PRESET_REGISTRY,
            (Codec<WorldPreset>) ((Object) DIRECT_CODEC)
    );

    public BCLWorldPreset(Map<ResourceKey<LevelStem>, LevelStem> map, int sortOrder, WorldPresetSettings settings) {
        super(map);
        this.sortOrder = sortOrder;
        this.settings = settings;
    }

    private Map<ResourceKey<LevelStem>, LevelStem> getDimensions() {
        return ((WorldPresetAccessor) this).bcl_getDimensions();
    }

}
