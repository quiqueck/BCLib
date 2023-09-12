package org.betterx.bclib.api.v2.levelgen;

import org.betterx.bclib.api.v2.generator.BCLChunkGenerator;
import org.betterx.bclib.api.v2.generator.BCLibEndBiomeSource;
import org.betterx.bclib.api.v2.generator.BCLibNetherBiomeSource;
import org.betterx.bclib.api.v2.generator.config.BCLEndBiomeSourceConfig;
import org.betterx.bclib.api.v2.generator.config.BCLNetherBiomeSourceConfig;
import org.betterx.worlds.together.levelgen.WorldGenUtil;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class LevelGenUtil {
    private static final String TAG_VERSION = "version";
    private static final String TAG_BN_GEN_VERSION = "generator_version";

    @NotNull
    public static LevelStem getBCLNetherLevelStem(WorldGenUtil.Context context, BCLNetherBiomeSourceConfig config) {
        BCLibNetherBiomeSource netherSource = new BCLibNetherBiomeSource(config);

        return new LevelStem(
                context.dimension,
                new BCLChunkGenerator(
                        netherSource,
                        context.generatorSettings
                )
        );
    }

    public static LevelStem getBCLEndLevelStem(WorldGenUtil.Context context, BCLEndBiomeSourceConfig config) {
        BCLibEndBiomeSource endSource = new BCLibEndBiomeSource(config);
        return new LevelStem(
                context.dimension,
                new BCLChunkGenerator(
                        endSource,
                        context.generatorSettings
                )
        );
    }


    public static Registry<LevelStem> replaceGenerator(
            ResourceKey<LevelStem> dimensionKey,
            ResourceKey<DimensionType> dimensionTypeKey,
            RegistryAccess registryAccess,
            Registry<LevelStem> dimensionRegistry,
            ChunkGenerator generator
    ) {
        Registry<DimensionType> dimensionTypeRegistry = registryAccess.registryOrThrow(Registries.DIMENSION_TYPE);
        Registry<LevelStem> newDimensions = withDimension(
                dimensionKey,
                dimensionTypeKey,
                dimensionTypeRegistry,
                dimensionRegistry,
                generator
        );
        return newDimensions;
    }

    public static Registry<LevelStem> withDimension(
            ResourceKey<LevelStem> dimensionKey,
            ResourceKey<DimensionType> dimensionTypeKey,
            Registry<DimensionType> dimensionTypeRegistry,
            Registry<LevelStem> inputDimensions,
            ChunkGenerator generator
    ) {

        LevelStem levelStem = inputDimensions.get(dimensionKey);
        Holder<DimensionType> dimensionType = levelStem == null
                ? dimensionTypeRegistry.getHolderOrThrow(dimensionTypeKey)
                : levelStem.type();
        return withDimension(dimensionKey, inputDimensions, new LevelStem(dimensionType, generator));
    }

    public static Registry<LevelStem> withDimension(
            ResourceKey<LevelStem> dimensionKey,
            Registry<LevelStem> inputDimensions,
            LevelStem levelStem
    ) {
        MappedRegistry<LevelStem> writableRegistry = new MappedRegistry<>(
                Registries.LEVEL_STEM,
                Lifecycle.experimental()
        );
        writableRegistry.register(
                dimensionKey,
                levelStem,
                Lifecycle.stable()
        );
        for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : inputDimensions.entrySet()) {
            ResourceKey<LevelStem> resourceKey = entry.getKey();
            if (resourceKey == dimensionKey) continue;
            writableRegistry.register(
                    resourceKey,
                    entry.getValue(),
                    inputDimensions.lifecycle(entry.getValue())
            );
        }
        return writableRegistry;
    }
}
