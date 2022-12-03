package org.betterx.bclib.api.v2.levelgen.biomes;

import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.world.level.biome.Biome;

import java.util.Objects;

class UnboundBCLBiome<T extends BCLBiome> extends BCLBiomeContainer<T> {
    private final BCLBiome parentBiome;
    private final BCLBiomeBuilder.BuildCompletion supplier;
    private BCLBiomeContainer<T> registered;

    UnboundBCLBiome(T biome, BCLBiome parentBiome, BCLBiomeBuilder.BuildCompletion supplier) {
        super(biome);
        this.parentBiome = parentBiome;
        this.supplier = supplier;
    }


    @Override
    public BCLBiomeContainer<T> register(BootstapContext<Biome> bootstrapContext, BiomeAPI.BiomeType dim) {
        if (registered != null) return registered;
        if (dim == null) dim = BiomeAPI.BiomeType.NONE;

        biome._setBiomeToRegister(this.supplier.apply(bootstrapContext));

        if (hasParent()) {
            BiomeAPI.registerSubBiome(bootstrapContext, parentBiome, biome, dim);
        } else if (dim.is(BiomeAPI.BiomeType.END_LAND)) {
            BiomeAPI.registerEndLandBiome(bootstrapContext, biome);
        } else if (dim.is(BiomeAPI.BiomeType.END_VOID)) {
            BiomeAPI.registerEndVoidBiome(bootstrapContext, biome);
        } else if (dim.is(BiomeAPI.BiomeType.END_BARRENS)) {
            BiomeAPI.registerEndBarrensBiome(bootstrapContext, parentBiome, biome);
        } else if (dim.is(BiomeAPI.BiomeType.END_CENTER)) {
            BiomeAPI.registerEndCenterBiome(bootstrapContext, biome);
        } else if (dim.is(BiomeAPI.BiomeType.NETHER)) {
            BiomeAPI.registerNetherBiome(bootstrapContext, biome);
        } else {
            BiomeAPI.registerBuiltinBiomeAndOverrideIntendedDimension(bootstrapContext, biome, dim);
        }

        BCLBiomeBuilder.UNBOUND_BIOMES.remove(this);
        registered = new BCLBiomeContainer<>(this.biome);
        return registered;
    }

    @Override
    public T biome() {
        return biome;
    }

    public boolean hasParent() {
        return parentBiome != null;
    }

    public BCLBiome parentBiome() {
        return parentBiome;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        UnboundBCLBiome<?> that = (UnboundBCLBiome<?>) obj;
        return Objects.equals(this.biome, that.biome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(biome);
    }

    @Override
    public String toString() {
        return "UnregisteredBiome[" + "biome=" + biome + ']';
    }
}
