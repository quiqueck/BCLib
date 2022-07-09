package org.betterx.worlds.together.biomesource;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

import java.util.Collection;
import java.util.stream.Collectors;

public class BiomeSourceHelper {
    public static String getNamespaces(Collection<Holder<Biome>> biomes) {
        String namespaces = biomes
                .stream()
                .filter(h -> h.unwrapKey().isPresent())
                .map(h -> h.unwrapKey().get().location().getNamespace())
                .distinct()
                .collect(Collectors.joining(", "));
        return namespaces;
    }
}
