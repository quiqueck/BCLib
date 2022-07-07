package org.betterx.bclib.api.v2.generator;

import org.betterx.bclib.api.v2.levelgen.biomes.BiomeAPI;

import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;

public abstract class TypeBiomeDecider extends BiomeDecider {
    protected final BiomeAPI.BiomeType assignedType;

    public TypeBiomeDecider(BiomeAPI.BiomeType assignedType) {
        this(null, assignedType);
    }

    protected TypeBiomeDecider(Registry<Biome> biomeRegistry, BiomeAPI.BiomeType assignedType) {
        super(biomeRegistry, (biome) -> biome.getIntendedType().is(assignedType));
        this.assignedType = assignedType;
    }

    @Override
    public boolean canProvideBiome(BiomeAPI.BiomeType suggestedType) {
        return suggestedType.equals(assignedType);
    }
}
