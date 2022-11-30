package org.betterx.bclib.api.v2.levelgen.biomes;

import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.world.level.biome.Biome;

public class BCLBiomeContainer<T extends BCLBiome> {
    protected final T biome;

    BCLBiomeContainer(T biome) {
        this.biome = biome;
    }

    public T biome() {
        return biome;
    }

    public BCLBiomeContainer<T> register(BootstapContext<Biome> bootstrapContext) {
        return register(bootstrapContext, biome.getIntendedType());
    }

    public BCLBiomeContainer<T> register(BootstapContext<Biome> bootstrapContext, BiomeAPI.BiomeType dim) {
        return this;
    }
}
