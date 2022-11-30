package org.betterx.bclib.api.v2;

import org.betterx.bclib.api.v2.datafixer.DataFixerAPI;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * provides some lifetime hooks for a Minecraft instance
 */
public class LifeCycleAPI {
    private final static List<LevelLoadBiomesCall> onLoadLevelBiomes = new ArrayList<>(2);
    private final static List<LevelLoadCall> onLoadLevel = new ArrayList<>(2);
    private final static List<BeforeLevelLoadCall> beforeLoadLevel = new ArrayList<>(2);


    /**
     * Register a callback that is called before a level is loaded or created,
     * but after the {@link org.betterx.worlds.together.world.WorldConfig} was initialized and patches from
     * the {@link DataFixerAPI} were applied.
     *
     * @param call The callback Method
     */
    public static void beforeLevelLoad(BeforeLevelLoadCall call) {
        beforeLoadLevel.add(call);
    }

    /**
     * Register a callback that is called when a new {@code ServerLevel is instantiated}.
     * This callback will receive the world seed as well as it's biome registry.
     *
     * @param call The calbback Method
     */
    public static void onLevelLoad(LevelLoadBiomesCall call) {
        onLoadLevelBiomes.add(call);
    }

    /**
     * Register a callback that is called when a new {@code ServerLevel is instantiated}.
     * This callbacl will receiv all parameters that were passed to the ServerLevel's constructor
     *
     * @param call The calbback Method
     */
    public static void onLevelLoad(LevelLoadCall call) {
        onLoadLevel.add(call);
    }

    /**
     * For internal use, You should not call this method!
     */
    public static void _runBeforeLevelLoad() {
        beforeLoadLevel.forEach(c -> c.beforeLoad());
    }

    /**
     * For internal use, You should not call this method!
     *
     * @param minecraftServer
     * @param executor
     * @param levelStorageAccess
     * @param serverLevelData
     * @param resourceKey
     * @param chunkProgressListener
     * @param bl
     * @param l
     * @param list
     * @param bl2
     */
    public static void _runLevelLoad(
            ServerLevel world,
            MinecraftServer minecraftServer,
            Executor executor,
            LevelStorageSource.LevelStorageAccess levelStorageAccess,
            ServerLevelData serverLevelData,
            ResourceKey<Level> resourceKey,
            ChunkProgressListener chunkProgressListener,
            boolean bl,
            long l,
            List<CustomSpawner> list,
            boolean bl2
    ) {
        onLoadLevel.forEach(c -> c.onLoad(
                world,
                minecraftServer,
                executor,
                levelStorageAccess,
                serverLevelData,
                resourceKey,
                chunkProgressListener,
                bl,
                l,
                list,
                bl2
        ));

        final long seed = world.getSeed();
        final Registry<Biome> biomeRegistry = world.registryAccess().registryOrThrow(Registries.BIOME);
        onLoadLevelBiomes.forEach(c -> c.onLoad(world, seed, biomeRegistry));
    }

    /**
     * A callback function that is used for each new ServerLevel instance
     */
    public interface BeforeLevelLoadCall {
        void beforeLoad();
    }

    /**
     * A callback function that is used for each new ServerLevel instance
     */
    public interface LevelLoadBiomesCall {
        void onLoad(ServerLevel world, long seed, Registry<Biome> registry);
    }

    /**
     * A callback function that is used for each new ServerLevel instance
     */
    public interface LevelLoadCall {
        void onLoad(
                ServerLevel world,
                MinecraftServer minecraftServer,
                Executor executor,
                LevelStorageSource.LevelStorageAccess levelStorageAccess,
                ServerLevelData serverLevelData,
                ResourceKey<Level> resourceKey,
                ChunkProgressListener chunkProgressListener,
                boolean bl,
                long l,
                List<CustomSpawner> list,
                boolean bl2
        );
    }
}
