package org.betterx.worlds.together;

import org.betterx.bclib.util.Logger;
import org.betterx.worlds.together.world.WorldConfig;
import org.betterx.worlds.together.worldPreset.WorldPresets;

import net.minecraft.resources.ResourceLocation;

import net.fabricmc.loader.api.FabricLoader;

public class WorldsTogether {
    public static boolean SURPRESS_EXPERIMENTAL_DIALOG = false;
    public static final String MOD_ID = "worlds_together";
    public static final Logger LOGGER = new Logger(MOD_ID);
    public static final boolean RUNS_TERRABLENDER = FabricLoader.getInstance()
                                                                .getModContainer("terrablender")
                                                                .isPresent();

    public static boolean isDevEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static void onInitialize() {
        WorldConfig.registerModCache(WorldsTogether.MOD_ID);
        WorldPresets.ensureStaticallyLoaded();
    }

    public static ResourceLocation makeID(String s) {
        return new ResourceLocation(MOD_ID, s);
    }
}
