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

    public static final BCLBiome THE_END = new BCLBiome(Biomes.THE_END.location(), InternalBiomeAPI.OTHER_END_CENTER);
//            InternalBiomeAPI.wrapNativeBiome(
//            Biomes.THE_END,
//            0.5F,
//            InternalBiomeAPI.OTHER_END_CENTER
//    );

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
        if (bclBiomes != null && bclBiomes.containsKey(key.location())) {
            return true;
        }

        return BUILTIN_BCL_BIOMES.containsKey(key.location());
    }

    public static BCLBiome getBiome(ResourceKey<Biome> key, Registry<BCLBiome> bclBiomes) {
        if (bclBiomes != null && bclBiomes.containsKey(key.location())) {
            return bclBiomes.get(key.location());
        }

        return BUILTIN_BCL_BIOMES.get(key.location());
    }

    private static <T> ResourceKey<Registry<T>> createRegistryKey(ResourceLocation location) {
        return ResourceKey.createRegistryKey(location);
    }


    private static Codec<? extends BCLBiome> bootstrapCodecs(Registry<Codec<? extends BCLBiome>> registry) {
        return Registry.register(registry, BCLib.makeID("biome"), BCLBiome.KEY_CODEC.codec());
    }

    public static BCLBiome get(ResourceLocation loc) {
        return get(WorldBootstrap.getLastRegistryAccess(), loc);
    }

    public static BCLBiome get(@Nullable RegistryAccess access, ResourceLocation loc) {
        var reg = getBclBiomesRegistry(access);
        if (reg == null) return null;
        return reg.get(loc);
    }

    public static BCLBiome getOrElseEmpty(ResourceLocation loc) {
        return getOrElseEmpty(WorldBootstrap.getLastRegistryAccess(), loc);
    }

    public static @Nullable BCLBiome getOrElseEmpty(@Nullable RegistryAccess access, ResourceLocation loc) {
        BCLBiome res = access == null ? null : get(access, loc);
        //if (res == null) return EMPTY_BIOME;
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
