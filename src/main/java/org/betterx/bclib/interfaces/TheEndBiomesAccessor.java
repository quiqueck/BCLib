package org.betterx.bclib.interfaces;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public interface TheEndBiomesAccessor {
    boolean bcl_canGenerateAsEndBiome(ResourceKey<Biome> key);

    boolean bcl_canGenerateAsEndMidlandBiome(ResourceKey<Biome> key);

    boolean bcl_canGenerateAsEndBarrensBiome(ResourceKey<Biome> key);

    default boolean bcl_isNonVanillaAndCanGenerateInEnd(ResourceKey<Biome> key) {
        return !"minecraft".equals(key.location().getNamespace()) &&
                bcl_canGenerateInEnd(key);
    }
    default boolean bcl_canGenerateInEnd(ResourceKey<Biome> key) {
        return bcl_canGenerateAsEndBarrensBiome(key) ||
                bcl_canGenerateAsEndMidlandBiome(key) ||
                bcl_canGenerateAsEndBiome(key)
                ;
    }
}
