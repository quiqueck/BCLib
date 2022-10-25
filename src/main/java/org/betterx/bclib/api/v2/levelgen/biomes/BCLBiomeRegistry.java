package org.betterx.bclib.api.v2.levelgen.biomes;

import org.betterx.bclib.BCLib;
import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.world.event.WorldBootstrap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.biome.Biomes;

import java.util.Optional;
import java.util.stream.Stream;
import org.jetbrains.annotations.ApiStatus;

public class BCLBiomeRegistry {
    public static final ResourceKey<Registry<BCLBiome>> BCL_BIOMES_REGISTRY =
            createRegistryKey(WorldsTogether.makeID("worldgen/betterx/biome"));

    public static final ResourceKey<Registry<Codec<? extends BCLBiome>>> BCL_BIOME_CODEC_REGISTRY =
            createRegistryKey(WorldsTogether.makeID("worldgen/betterx/biome_codec"));

    public static Registry<Codec<? extends BCLBiome>> BIOME_CODECS = Registry.registerSimple(
            BCL_BIOME_CODEC_REGISTRY,
            BCLBiomeRegistry::bootstrapCodecs
    );
    public static Registry<BCLBiome> BUILTIN_BCL_BIOMES = new MappedRegistry<>(
            BCL_BIOMES_REGISTRY,
            Lifecycle.stable()
    );

    /**
     * Empty biome used as default value if requested biome doesn't exist or linked. Shouldn't be registered anywhere to prevent bugs.
     * Have {@code Biomes.THE_VOID} as the reference biome.
     **/
    public static final BCLBiome EMPTY_BIOME = new BCLBiome(Biomes.THE_VOID.location());

    /**
     * Register a codec for a custom subclass of {@link BCLBiome}. Each subclass needs to provide
     * a codec, otherwise the instance will get rebuild as a regular BCLib biome loosing the Type
     * of the class as well as all member values
     *
     * @param location A {@link ResourceLocation} identifying this class
     * @param codec    The matching Codec
     * @return The codec that will get used
     */
    public static <E extends BCLBiome> Codec<E> registerBiomeCodec(
            ResourceLocation location,
            KeyDispatchDataCodec<E> codec
    ) {
        Registry.register(BIOME_CODECS, location, codec.codec());
        return codec.codec();
    }

    /**
     * Register new Biome Data
     *
     * @param access The {@link RegistryAccess} to use. If null, we will use the
     *               built inregistry ({@link BCLBiomeRegistry#BUILTIN_BCL_BIOMES})
     * @param biome  The Biome Data to register
     * @return The resource-key for the registry
     */
    @ApiStatus.Internal
    public static ResourceKey<BCLBiome> register(RegistryAccess access, BCLBiome biome) {
        Registry.register(
                access == null ? BUILTIN_BCL_BIOMES : access.registryOrThrow(BCL_BIOMES_REGISTRY),
                biome.getBCLBiomeKey(),
                biome
        );
        return biome.getBCLBiomeKey();
    }

    private static <T> ResourceKey<Registry<T>> createRegistryKey(ResourceLocation location) {
        return ResourceKey.createRegistryKey(location);
    }

    private static Codec<? extends BCLBiome> bootstrapCodecs(Registry<Codec<? extends BCLBiome>> registry) {
        return Registry.register(registry, BCLib.makeID("biome"), BCLBiome.KEY_CODEC.codec());
    }


    @ApiStatus.Internal
    public static Holder<BCLBiome> bootstrap(Registry<BCLBiome> registry) {
        BuiltinRegistries.register(registry, BiomeAPI.SMALL_END_ISLANDS.getBCLBiomeKey(), BiomeAPI.SMALL_END_ISLANDS);
        BuiltinRegistries.register(registry, BiomeAPI.END_BARRENS.getBCLBiomeKey(), BiomeAPI.END_BARRENS);
        BuiltinRegistries.register(registry, BiomeAPI.END_HIGHLANDS.getBCLBiomeKey(), BiomeAPI.END_HIGHLANDS);
        BuiltinRegistries.register(registry, BiomeAPI.END_MIDLANDS.getBCLBiomeKey(), BiomeAPI.END_MIDLANDS);
        BuiltinRegistries.register(registry, BiomeAPI.THE_END.getBCLBiomeKey(), BiomeAPI.THE_END);
        BuiltinRegistries.register(
                registry,
                BiomeAPI.BASALT_DELTAS_BIOME.getBCLBiomeKey(),
                BiomeAPI.BASALT_DELTAS_BIOME
        );
        BuiltinRegistries.register(
                registry,
                BiomeAPI.SOUL_SAND_VALLEY_BIOME.getBCLBiomeKey(),
                BiomeAPI.SOUL_SAND_VALLEY_BIOME
        );
        BuiltinRegistries.register(
                registry,
                BiomeAPI.WARPED_FOREST_BIOME.getBCLBiomeKey(),
                BiomeAPI.WARPED_FOREST_BIOME
        );
        BuiltinRegistries.register(
                registry,
                BiomeAPI.CRIMSON_FOREST_BIOME.getBCLBiomeKey(),
                BiomeAPI.CRIMSON_FOREST_BIOME
        );
        BuiltinRegistries.register(
                registry,
                BiomeAPI.NETHER_WASTES_BIOME.getBCLBiomeKey(),
                BiomeAPI.NETHER_WASTES_BIOME
        );
        return BuiltinRegistries.register(registry, EMPTY_BIOME.getBCLBiomeKey(), EMPTY_BIOME);
    }

    public static BCLBiome get(ResourceLocation loc) {
        return get(WorldBootstrap.getLastRegistryAccessOrElseBuiltin(), loc);
    }

    public static BCLBiome get(RegistryAccess access, ResourceLocation loc) {
        return getBclBiomesRegistry(access).get(loc);
    }

    public static BCLBiome getOrElseEmpty(ResourceLocation loc) {
        return getOrElseEmpty(WorldBootstrap.getLastRegistryAccessOrElseBuiltin(), loc);
    }

    public static BCLBiome getOrElseEmpty(RegistryAccess access, ResourceLocation loc) {
        BCLBiome res = get(access, loc);
        if (res == null) return EMPTY_BIOME;
        return res;
    }

    public static Stream<ResourceKey<BCLBiome>> getAll(BiomeAPI.BiomeType dim) {
        return getAll(WorldBootstrap.getLastRegistryAccessOrElseBuiltin(), dim);
    }

    public static Stream<ResourceKey<BCLBiome>> getAll(RegistryAccess access, BiomeAPI.BiomeType dim) {
        return getBclBiomesRegistry(access)
                .entrySet()
                .stream()
                .filter(e -> e.getValue().getIntendedType().is(dim))
                .map(e -> e.getKey());
    }

    private static Registry<BCLBiome> getBclBiomesRegistry(RegistryAccess access) {
        if (access != null) {
            return ((Optional<Registry<BCLBiome>>) access
                    .registry(BCLBiomeRegistry.BCL_BIOMES_REGISTRY))
                    .orElse(BUILTIN_BCL_BIOMES);
        } else {
            return BUILTIN_BCL_BIOMES;
        }
    }

    public static void ensureStaticallyLoaded() {

    }
}
