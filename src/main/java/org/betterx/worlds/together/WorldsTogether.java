package org.betterx.worlds.together;

import org.betterx.worlds.together.tag.v3.TagManager;
import org.betterx.worlds.together.util.Logger;
import org.betterx.worlds.together.world.WorldConfig;

import net.minecraft.resources.ResourceLocation;

import net.fabricmc.loader.api.FabricLoader;

public class WorldsTogether {
    public static boolean SURPRESS_EXPERIMENTAL_DIALOG = false;
    public static boolean FORCE_SERVER_TO_BETTERX_PRESET = false;
    public static final String MOD_ID = "worlds_together";
    public static final Logger LOGGER = new Logger(MOD_ID);
    public static final boolean RUNS_TERRABLENDER = FabricLoader.getInstance()
                                                                .getModContainer("terrablender")
                                                                .isPresent();

    public static boolean isDevEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static void onInitialize() {
        TagManager.ensureStaticallyLoaded();
        WorldConfig.registerModCache(WorldsTogether.MOD_ID);
    }

    public static ResourceLocation makeID(String s) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, s);
    }
}
