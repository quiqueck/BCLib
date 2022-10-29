package org.betterx.bclib;

import org.betterx.bclib.api.v2.levelgen.biomes.*;
import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.surfaceRules.AssignedSurfaceRule;
import org.betterx.worlds.together.surfaceRules.SurfaceRuleRegistry;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBuiltinRegistriesProvider;

import com.google.gson.JsonElement;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class BCLibDatagen implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
        registerTestBiomes();

        final FabricDataGenerator.Pack pack = dataGenerator.create();
        pack.addProvider(CustomRegistriesDataProvider::new);
        pack.addProvider(FabricBuiltinRegistriesProvider.forCurrentMod());

    }

    private void registerTestBiomes() {

        if (true && BCLib.isDevEnvironment()) {
            BCLBiome theYellow = BCLBiomeBuilder
                    .start(BCLib.makeID("the_yellow"))
                    .precipitation(Biome.Precipitation.NONE)
                    .temperature(1.0f)
                    .wetness(1.0f)
                    .fogColor(0xFFFF00)
                    .waterColor(0x777700)
                    .waterFogColor(0xFFFF00)
                    .skyColor(0xAAAA00)
                    .addNetherClimateParamater(-1, 1)
                    .surface(Blocks.YELLOW_CONCRETE)
                    .build();
            BiomeAPI.registerEndLandBiome(theYellow);

            BCLBiome theBlue = BCLBiomeBuilder
                    .start(BCLib.makeID("the_blue"))
                    .precipitation(Biome.Precipitation.NONE)
                    .temperature(1.0f)
                    .wetness(1.0f)
                    .fogColor(0x0000FF)
                    .waterColor(0x000077)
                    .waterFogColor(0x0000FF)
                    .skyColor(0x0000AA)
                    .addNetherClimateParamater(-1, 1)
                    .surface(Blocks.LIGHT_BLUE_CONCRETE)
                    .build();
            BiomeAPI.registerEndLandBiome(theBlue);

            BCLBiome theGray = BCLBiomeBuilder
                    .start(BCLib.makeID("the_gray"))
                    .precipitation(Biome.Precipitation.NONE)
                    .temperature(1.0f)
                    .wetness(1.0f)
                    .fogColor(0xFFFFFF)
                    .waterColor(0x777777)
                    .waterFogColor(0xFFFFFF)
                    .skyColor(0xAAAAAA)
                    .addNetherClimateParamater(-1, 1)
                    .surface(Blocks.GRAY_CONCRETE)
                    .build();
            BiomeAPI.registerEndVoidBiome(theGray);

            BCLBiome theOrange = BCLBiomeBuilder
                    .start(BCLib.makeID("the_orange"))
                    .precipitation(Biome.Precipitation.NONE)
                    .temperature(1.0f)
                    .wetness(1.0f)
                    .fogColor(0xFF7700)
                    .waterColor(0x773300)
                    .waterFogColor(0xFF7700)
                    .skyColor(0xAA7700)
                    .addNetherClimateParamater(-1, 1.1f)
                    .surface(Blocks.ORANGE_CONCRETE)
                    .build();
            BiomeAPI.registerNetherBiome(theOrange);

            BCLBiome thePurple = BCLBiomeBuilder
                    .start(BCLib.makeID("the_purple"))
                    .precipitation(Biome.Precipitation.NONE)
                    .temperature(1.0f)
                    .wetness(1.0f)
                    .fogColor(0xFF00FF)
                    .waterColor(0x770077)
                    .waterFogColor(0xFF00FF)
                    .skyColor(0xAA00AA)
                    .addNetherClimateParamater(-1.1f, 1)
                    .surface(Blocks.PURPLE_CONCRETE)
                    .build();
            BiomeAPI.registerNetherBiome(thePurple);
        }
    }

    public static class CustomRegistriesDataProvider implements DataProvider {
        public static final List<RegistryDataLoader.RegistryData<?>> REGISTRIES = List.of(
                new RegistryDataLoader.RegistryData<>(BCLBiomeRegistry.BCL_BIOMES_REGISTRY, BiomeData.CODEC),
                new RegistryDataLoader.RegistryData<>(
                        SurfaceRuleRegistry.SURFACE_RULES_REGISTRY,
                        AssignedSurfaceRule.CODEC
                )
        );


        private final PackOutput output;

        public CustomRegistriesDataProvider(FabricDataOutput generator) {
            this.output = generator;
        }

        @Override
        public CompletableFuture<?> run(CachedOutput cachedOutput) {
            RegistryAccess.Frozen registryAccess = BuiltinRegistries.createAccess();
            RegistryOps<JsonElement> dynamicOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
            final List<CompletableFuture<?>> futures = new ArrayList<>();

            for (RegistryDataLoader.RegistryData<?> registryData : REGISTRIES) {
                futures.add(this.dumpRegistryCapFuture(
                        cachedOutput,
                        registryAccess,
                        dynamicOps,
                        (RegistryDataLoader.RegistryData) registryData
                ));
            }
            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        }

        private <T> CompletableFuture dumpRegistryCapFuture(
                CachedOutput cachedOutput,
                RegistryAccess registryAccess,
                DynamicOps<JsonElement> dynamicOps,
                RegistryDataLoader.RegistryData<T> registryData
        ) {
            return CompletableFuture.runAsync(() -> dumpRegistryCap(
                    cachedOutput,
                    registryAccess,
                    dynamicOps,
                    registryData
            ));
        }

        private <T> void dumpRegistryCap(
                CachedOutput cachedOutput,
                RegistryAccess registryAccess,
                DynamicOps<JsonElement> dynamicOps,
                RegistryDataLoader.RegistryData<T> registryData
        ) {
            ResourceKey<? extends Registry<T>> resourceKey = registryData.key();
            Registry<T> registry = registryAccess.registryOrThrow(resourceKey);
            PackOutput.PathProvider pathProvider = this.output.createPathProvider(
                    PackOutput.Target.DATA_PACK,
                    resourceKey.location().getPath()
            );
            for (Map.Entry<ResourceKey<T>, T> entry : registry.entrySet()) {
                dumpValue(
                        pathProvider.json(entry.getKey().location()),
                        cachedOutput,
                        dynamicOps,
                        registryData.elementCodec(),
                        entry.getValue()
                );
            }
        }

        private static <E> void dumpValue(
                Path path,
                CachedOutput cachedOutput,
                DynamicOps<JsonElement> dynamicOps,
                Encoder<E> encoder,
                E object
        ) {

            Optional<JsonElement> optional = encoder.encodeStart(dynamicOps, object)
                                                    .resultOrPartial(string -> WorldsTogether.LOGGER.error(
                                                            "Couldn't serialize element {}: {}",
                                                            path,
                                                            string
                                                    ));
            if (optional.isPresent()) {
                DataProvider.saveStable(cachedOutput, optional.get(), path);
            }

        }

        @Override
        public String getName() {
            return "BCL Registries";
        }
    }
}
