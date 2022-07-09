package org.betterx.worlds.together.mixin.common;

import org.betterx.worlds.together.biomesource.BiomeSourceHelper;

import net.minecraft.world.level.biome.BiomeSource;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(BiomeSource.class)
public class BiomeSourceMixin {
    @Override
    public String toString() {
        BiomeSource self = (BiomeSource) (Object) this;
        return "\n" + getClass().getSimpleName() + " (" + Integer.toHexString(hashCode()) + ")" +
                "\n    biomes     = " + self.possibleBiomes().size() +
                "\n    namespaces = " + BiomeSourceHelper.getNamespaces(self.possibleBiomes());
    }
}
