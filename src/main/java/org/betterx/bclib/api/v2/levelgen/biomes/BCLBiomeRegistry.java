package org.betterx.bclib.api.v2.levelgen.biomes;

import org.betterx.bclib.BCLib;
import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.world.event.WorldBootstrap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class BCLBiomeRegistry {
    public static final ResourceKey<Registry<BCLBiome>> BCL_BIOMES_REGISTRY =
            createRegistryKey(WorldsTogether.makeID("worldgen/betterx/biome"));

    public static final ResourceKey<Registry<Codec<? extends BCLBiome>>> BCL_BIOME_CODEC_REGISTRY =
            createRegistryKey(WorldsTogether.makeID("worldgen/betterx/biome_codec"));

    public static Registry<Codec<? extends BCLBiome>> BIOME_CODECS = BuiltInRegistries.registerSimple(
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

    public static final BCLBiome THE_END = InternalBiomeAPI.wrapBiome(
            Biomes.THE_END,
            InternalBiomeAPI.OTHER_END_CENTER
    );

    public static final BCLBiome NETHER_WASTES_BIOME = InternalBiomeAPI.wrapBiome(
            Biomes.NETHER_WASTES,
            InternalBiomeAPI.OTHER_NETHER
    );

    public static final BCLBiome CRIMSON_FOREST_BIOME = InternalBiomeAPI.wrapBiome(
            Biomes.CRIMSON_FOREST,
            InternalBiomeAPI.OTHER_NETHER
    );

    public static final BCLBiome WARPED_FOREST_BIOME = InternalBiomeAPI.wrapBiome(
            Biomes.WARPED_FOREST,
            InternalBiomeAPI.OTHER_NETHER
    );

    public static final BCLBiome SOUL_SAND_VALLEY_BIOME = InternalBiomeAPI.wrapBiome(
            Biomes.SOUL_SAND_VALLEY,
            InternalBiomeAPI.OTHER_NETHER
    );
    public static final BCLBiome BASALT_DELTAS_BIOME = InternalBiomeAPI.wrapBiome(
            Biomes.BASALT_DELTAS,
            InternalBiomeAPI.OTHER_NETHER
    );

    public static final BCLBiome END_MIDLANDS = InternalBiomeAPI.wrapBiome(
            Biomes.END_MIDLANDS,
            0.5F,
            InternalBiomeAPI.OTHER_END_LAND
    );

    public static final BCLBiome END_HIGHLANDS = InternalBiomeAPI.wrapBiome(
            Biomes.END_HIGHLANDS,
            END_MIDLANDS,
            8,
            0.5F,
            InternalBiomeAPI.OTHER_END_LAND
    );

    public static final BCLBiome END_BARRENS = InternalBiomeAPI.wrapBiome(
            Biomes.END_BARRENS,
            InternalBiomeAPI.OTHER_END_BARRENS
    );

    public static final BCLBiome SMALL_END_ISLANDS = InternalBiomeAPI.wrapBiome(
            Biomes.SMALL_END_ISLANDS,
            InternalBiomeAPI.OTHER_END_VOID
    );

    public static boolean isEmptyBiome(ResourceLocation l) {
        return l == null || Biomes.THE_VOID.location().equals(l);
    }

    public static boolean isEmptyBiome(BCLBiome b) {
        return b == null || b == EMPTY_BIOME;
    }

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
     * @param biome The Biome Data to register
     * @return The resource-key for the registry
     */
    @ApiStatus.Internal
    public static ResourceKey<BCLBiome> registerForDatagen(BCLBiome biome) {
        if (BUILTIN_BCL_BIOMES == null) return biome.getBCLBiomeKey();

        Registry.register(
                BUILTIN_BCL_BIOMES,
                biome.getBCLBiomeKey(),
                biome
        );

        return biome.getBCLBiomeKey();
    }

    public static void register(BCLBiome biome) {
        registerForDatagen(biome);
    }

    public static boolean hasBiome(ResourceKey<Biome> key, Registry<BCLBiome> bclBiomes) {
        return hasBiome(key.location(), bclBiomes);
    }

    public static boolean hasBiome(ResourceLocation loc, Registry<BCLBiome> bclBiomes) {
        if (loc == null) return false;
        if (bclBiomes != null && bclBiomes.containsKey(loc)) {
            return true;
        }

        return BUILTIN_BCL_BIOMES.containsKey(loc);
    }

    public static BCLBiome getBiome(ResourceKey<Biome> key, Registry<BCLBiome> bclBiomes) {
        return getBiome(key.location(), bclBiomes);
    }

    public static BCLBiome getBiome(ResourceLocation loc, Registry<BCLBiome> bclBiomes) {
        if (bclBiomes != null && bclBiomes.containsKey(loc)) {
            return bclBiomes.get(loc);
        }

        return BUILTIN_BCL_BIOMES.get(loc);
    }

    public static BCLBiome getBiomeOrNull(ResourceLocation loc, Registry<BCLBiome> bclBiomes) {
        if (!hasBiome(loc, bclBiomes)) return null;
        return getBiome(loc, bclBiomes);
    }

    public static BCLBiome getBiomeOrNull(ResourceKey<Biome> key, Registry<BCLBiome> bclBiomes) {
        return getBiomeOrNull(key.location(), bclBiomes);
    }

    public static BCLBiome getBiomeOrEmpty(ResourceLocation loc, Registry<BCLBiome> bclBiomes) {
        if (!hasBiome(loc, bclBiomes)) return EMPTY_BIOME;
        return getBiome(loc, bclBiomes);
    }

    public static BCLBiome getBiomeOrEmpty(ResourceKey<Biome> key, Registry<BCLBiome> bclBiomes) {
        return getBiomeOrEmpty(key.location(), bclBiomes);
    }

    private static <T> ResourceKey<Registry<T>> createRegistryKey(ResourceLocation location) {
        return ResourceKey.createRegistryKey(location);
    }


    private static Codec<? extends BCLBiome> bootstrapCodecs(Registry<Codec<? extends BCLBiome>> registry) {
        return Registry.register(registry, BCLib.makeID("biome"), BCLBiome.KEY_CODEC.codec());
    }

    public static Registry<BCLBiome> registryOrNull() {
        if (WorldBootstrap.getLastRegistryAccess() == null) return null;
        return WorldBootstrap.getLastRegistryAccess().registry(BCL_BIOMES_REGISTRY).orElse(null);
    }
    

    public static Stream<ResourceKey<BCLBiome>> getAll(BiomeAPI.BiomeType dim) {
        Set<ResourceKey<BCLBiome>> result = new HashSet<>();
        final Registry<BCLBiome> reg = registryOrNull();
        if (reg != null) {
            reg.entrySet()
               .stream()
               .filter(e -> e.getValue().getIntendedType().is(dim))
               .map(e -> e.getKey())
               .forEach(k -> result.add(k));
        }

        if (BUILTIN_BCL_BIOMES != null) {
            BUILTIN_BCL_BIOMES
                    .entrySet()
                    .stream()
                    .filter(e -> e.getValue().getIntendedType().is(dim))
                    .map(e -> e.getKey())
                    .filter(k -> !result.contains(k))
                    .forEach(k -> result.add(k));
        }
        return result.stream();
    }

    private static Registry<BCLBiome> getBclBiomesRegistry(@Nullable RegistryAccess access) {
        if (access != null) {
            return access
                    .registry(BCLBiomeRegistry.BCL_BIOMES_REGISTRY)
                    .orElse(BUILTIN_BCL_BIOMES);
        } else {
            return BUILTIN_BCL_BIOMES;
        }
    }

    public static void ensureStaticallyLoaded() {

    }
}
