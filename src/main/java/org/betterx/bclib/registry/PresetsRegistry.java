package org.betterx.bclib.registry;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.config.Configs;
import org.betterx.worlds.together.worldPreset.WorldPresets;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

public class PresetsRegistry {
    public static ResourceKey<WorldPreset> BCL_WORLD = WorldPresets.createKey(BCLib.makeID("normal"));
    public static ResourceKey<WorldPreset> BCL_WORLD_LARGE = WorldPresets.createKey(BCLib.makeID("large"));
    public static ResourceKey<WorldPreset> BCL_WORLD_AMPLIFIED = WorldPresets.createKey(BCLib.makeID("amplified"));
    public static ResourceKey<WorldPreset> BCL_WORLD_17 = WorldPresets.createKey(BCLib.makeID("legacy_17"));

    public static void register() {
        if (Configs.CLIENT_CONFIG.forceBetterXPreset())
            WorldPresets.setDEFAULT(BCL_WORLD);
        else
            WorldPresets.setDEFAULT(net.minecraft.world.level.levelgen.presets.WorldPresets.NORMAL);
    }
}
