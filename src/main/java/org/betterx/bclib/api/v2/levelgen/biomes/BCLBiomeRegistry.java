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
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import net.fabricmc.fabric.api.biome.v1.NetherBiomes;
import net.fabricmc.fabric.api.biome.v1.TheEndBiomes;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BCLBiomeRegistry {
    public static final ResourceKey<Registry<BCLBiome>> BCL_BIOMES_REGISTRY =
            createRegistryKey(WorldsTogether.makeID("worldgen/betterx/biome"));

    public static final ResourceKey<Registry<Codec<? extends BCLBiome>>> BCL_BIOME_CODEC_REGISTRY =
            createRegistryKey(WorldsTogether.makeID("worldgen/betterx/biome_codec"));

    public static Registry<Codec<? extends BCLBiome>> BIOME_CODECS = FabricRegistryBuilder
            .from(new MappedRegistry<>(BCL_BIOME_CODEC_REGISTRY, Lifecycle.stable()))
            .attribute(RegistryAttribute.MODDED)
            .buildAndRegister();
    public static MappedRegistry<BCLBiome> BUILTIN_BCL_BIOMES = new MappedRegistry<>(
            BCL_BIOMES_REGISTRY,
            Lifecycle.stable()
    );

    /**
     * Empty biome used as default value if requested biome doesn't exist or linked. Shouldn't be registered anywhere to prevent bugs.
     * Have {@code Biomes.THE_VOID} as the reference biome.
     **/
    public static final BCLBiome EMPTY_BIOME = new BCLBiome(Biomes.THE_VOID.location());
    private static boolean didCreate = false;

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

    public static BCLBiome registerIfUnknown(Holder<Biome> biomeHolder, @NotNull BiomeAPI.BiomeType intendedType) {
        if (biomeHolder == null) return null;
        return registerIfUnknown(biomeHolder.unwrapKey().orElse(null), intendedType);
    }

    public static BCLBiome registerIfUnknown(ResourceKey<Biome> biomeKey, @NotNull BiomeAPI.BiomeType intendedType) {
        if (biomeKey != null) {
            if (!hasBiome(biomeKey, BCLBiomeRegistry.registryOrNull())) {
                final var bclBiome = new BCLBiome(
                        biomeKey.location(),
                        intendedType
                );
                BCLBiomeRegistry.register(bclBiome);
                return bclBiome;
            }
        }
        return null;
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

    @ApiStatus.Internal
    public static void register() {
        bootstrapCodecs(BIOME_CODECS);
    }

    @ApiStatus.Internal
    public static void bootstrap(BootstapContext<BCLBiome> ctx) {
        //copy from builtin, disabled as we do not bootstrap any biomes, all are loaded from the default datapack
        //vanilla biomes are provided by bclib
//        for (Map.Entry<ResourceKey<BCLBiome>, BCLBiome> e : BUILTIN_BCL_BIOMES.entrySet()) {
//            ctx.register(e.getKey(), e.getValue());
//        }
    }

    private static void onBiomeLoad(Registry<BCLBiome> registry, int rawID, ResourceLocation id, BCLBiome biome) {
        //this ensures that all BCL Manage Biomes get added to the fabric Biome-API on load
        if (!"minecraft".equals(id.getNamespace())) {
            if (biome.getIntendedType().is(BiomeAPI.BiomeType.BCL_NETHER)) {
                for (var params : biome.parameterPoints) {
                    NetherBiomes.addNetherBiome(biome.getBiomeKey(), params);
                }
            } else if (biome.getIntendedType().is(BiomeAPI.BiomeType.BCL_END_CENTER)) {
                TheEndBiomes.addMainIslandBiome(biome.getBiomeKey(), 1.0);
            } else if (biome.getIntendedType().is(BiomeAPI.BiomeType.BCL_END_BARRENS)) {
                TheEndBiomes.addBarrensBiome(biome.getParentBiome().getBiomeKey(), biome.getBiomeKey(), 1.0);
            } else if (biome.getIntendedType().is(BiomeAPI.BiomeType.BCL_END_LAND)) {
                TheEndBiomes.addHighlandsBiome(biome.getBiomeKey(), 1.0);
                TheEndBiomes.addMidlandsBiome(biome.getBiomeKey(), biome.getBiomeKey(), 1.0);
            } else if (biome.getIntendedType().is(BiomeAPI.BiomeType.BCL_END_VOID)) {
                TheEndBiomes.addSmallIslandsBiome(biome.getBiomeKey(), 1.0);
            } else {
                BCLib.LOGGER.info("Did not manage biome " + biome);
            }

            //System.out.println("Loaded " + biome);
        }
    }

    static {
        DynamicRegistrySetupCallback.EVENT.register(registryManager -> {
            Optional<? extends Registry<BCLBiome>> oBCLBiomeRegistry = registryManager.asDynamicRegistryManager()
                                                                                      .registry(BCLBiomeRegistry.BCL_BIOMES_REGISTRY);
            if (oBCLBiomeRegistry.isPresent()) {
                final Registry<BCLBiome> registry = oBCLBiomeRegistry.orElseThrow();
                RegistryEntryAddedCallback
                        .event(oBCLBiomeRegistry.get())
                        .register((rawId, loc, biome) -> BCLBiomeRegistry.onBiomeLoad(registry, rawId, loc, biome));
            } else {
                BCLib.LOGGER.warning("No valid BCLBiome Registry available!");
            }
        });
    }
}
