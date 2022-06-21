package org.betterx.bclib.api.v2;

import org.betterx.worlds.together.world.WorldConfig;

import net.minecraft.nbt.CompoundTag;

import java.io.File;

/**
 * @deprecated Implementation moved to {@link WorldConfig}
 */
@Deprecated(forRemoval = true)
public class WorldDataAPI {
    /**
     * @deprecated use {@link WorldConfig#load(File)} instead
     */
    @Deprecated(forRemoval = true)
    public static void load(File dataDir) {
        WorldConfig.load(dataDir);
    }

    /**
     * @deprecated use {@link WorldConfig#registerModCache(String)} instead
     */
    @Deprecated(forRemoval = true)
    public static void registerModCache(String modID) {
        WorldConfig.registerModCache(modID);
    }

    /**
     * @deprecated use {@link WorldConfig#getRootTag(String)} instead
     */
    @Deprecated(forRemoval = true)
    public static CompoundTag getRootTag(String modID) {
        return WorldConfig.getRootTag(modID);
    }

    /**
     * @deprecated use {@link WorldConfig#hasMod(String)} instead
     */
    @Deprecated(forRemoval = true)
    public static boolean hasMod(String modID) {
        return WorldConfig.hasMod(modID);
    }

    /**
     * @deprecated use {@link WorldConfig#getCompoundTag(String, String)} instead
     */
    @Deprecated(forRemoval = true)
    public static CompoundTag getCompoundTag(String modID, String path) {
        return WorldConfig.getCompoundTag(modID, path);
    }

    /**
     * @deprecated use {@link WorldConfig#saveFile(String)} instead
     */
    @Deprecated(forRemoval = true)
    public static void saveFile(String modID) {
        WorldConfig.saveFile(modID);
    }

    /**
     * @deprecated use {@link WorldConfig#getModVersion(String)} instead
     */
    @Deprecated(forRemoval = true)
    public static String getModVersion(String modID) {
        return WorldConfig.getModVersion(modID);
    }

    /**
     * @deprecated use {@link WorldConfig#getIntModVersion(String)} instead
     */
    @Deprecated(forRemoval = true)
    public static int getIntModVersion(String modID) {
        return WorldConfig.getIntModVersion(modID);
    }
}
