package org.betterx.worlds.together.worldPreset;

import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.mixin.common.WorldPresetAccessor;
import org.betterx.worlds.together.world.WorldConfig;
import org.betterx.worlds.together.world.WorldGenUtil;
import org.betterx.worlds.together.worldPreset.settings.VanillaWorldPresetSettings;
import org.betterx.worlds.together.worldPreset.settings.WorldPresetSettings;

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

    public static WorldPresetSettings writeWorldPresetSettings(Optional<Holder<WorldPreset>> worldPreset) {
        if (worldPreset.isPresent() && worldPreset.get().value() instanceof TogetherWorldPreset wp) {
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
            final CompoundTag settingsNbt = WorldConfig.getRootTag(WorldsTogether.MOD_ID);
            settingsNbt.put(TAG_GENERATOR, encodeResult.result().get());
        } else {
            WorldsTogether.LOGGER.error("Unable to encode world generator settings for level.dat.");
        }

        WorldConfig.saveFile(WorldsTogether.MOD_ID);
    }
}
