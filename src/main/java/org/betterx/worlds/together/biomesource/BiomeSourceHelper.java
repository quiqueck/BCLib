package org.betterx.worlds.together.biomesource;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

import java.util.Collection;
import java.util.stream.Collectors;

public class BiomeSourceHelper {

    /**
     * Get a list of namespaces from a collection of biomes.
     *
     * @param biomes The collection of biomes.
     * @return A comma-separated list of namespaces including the number of biomes found in each namespace.
     */
    public static String getNamespaces(Collection<Holder<Biome>> biomes) {
        var namespaces = biomes
                .stream()
                .filter(h -> h.unwrapKey().isPresent())
                .map(h -> h.unwrapKey().get().location().getNamespace())
                .toList();

        return namespaces
                .stream()
                .distinct()
                .map(n -> n + "(" + namespaces.stream().filter(n::equals).count() + ")")
                .collect(Collectors.joining(", "));

    }
}
