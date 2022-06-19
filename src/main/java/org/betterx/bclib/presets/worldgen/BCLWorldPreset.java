package org.betterx.bclib.presets.worldgen;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.WorldDataAPI;
import org.betterx.bclib.api.v2.levelgen.LevelGenUtil;
import org.betterx.bclib.mixin.common.WorldPresetAccessor;

import net.minecraft.core.Holder;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import java.util.Map;
import java.util.Optional;

public class BCLWorldPreset extends WorldPreset {
    public final WorldPresetSettings settings;
    public final int sortOrder;

    private static final String TAG_GENERATOR = LevelGenUtil.TAG_GENERATOR;

    private static int NEXT_IN_SORT_ORDER = 1000;

    public BCLWorldPreset(
            Map<ResourceKey<LevelStem>, LevelStem> map,
            Optional<Integer> sortOrder,
            Optional<WorldPresetSettings> settings
    ) {
        this(map, sortOrder.orElse(NEXT_IN_SORT_ORDER++), settings.orElse(VanillaWorldPresetSettings.DEFAULT));
    }

    public BCLWorldPreset(Map<ResourceKey<LevelStem>, LevelStem> map, int sortOrder, WorldPresetSettings settings) {
        super(map);
        this.sortOrder = sortOrder;
        this.settings = settings;
    }

    public BCLWorldPreset withSettings(WorldPresetSettings settings) {
        return new BCLWorldPreset(getDimensions(), sortOrder, settings);
    }

    private Map<ResourceKey<LevelStem>, LevelStem> getDimensions() {
        return ((WorldPresetAccessor) this).bcl_getDimensions();
    }

    public static WorldPresetSettings writeWorldPresetSettings(Optional<Holder<WorldPreset>> worldPreset) {
        if (worldPreset.isPresent() && worldPreset.get().value() instanceof BCLWorldPreset wp) {
            writeWorldPresetSettings(wp.settings);
            return wp.settings;
        } else {
            writeWorldPresetSettings(VanillaWorldPresetSettings.DEFAULT);
            return VanillaWorldPresetSettings.DEFAULT;
        }
    }

    public static void writeWorldPresetSettings(WorldPresetSettings presetSettings) {
        final RegistryOps<Tag> registryOps = RegistryOps.create(NbtOps.INSTANCE, BuiltinRegistries.ACCESS);
        final var codec = WorldPresetSettings.CODEC.orElse(presetSettings);
        final var encodeResult = codec.encodeStart(registryOps, presetSettings);

        if (encodeResult.result().isPresent()) {
            final CompoundTag settingsNbt = WorldDataAPI.getRootTag(BCLib.TOGETHER_WORLDS);
            settingsNbt.put(TAG_GENERATOR, encodeResult.result().get());
        } else {
            BCLib.LOGGER.error("Unable to encode world generator settings generator for level.dat.");
        }

        WorldDataAPI.saveFile(BCLib.TOGETHER_WORLDS);
    }
}
