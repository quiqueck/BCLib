package org.betterx.bclib.mixin.common;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.interfaces.BiomeSourceAccessor;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(BiomeSource.class)
public abstract class BiomeSourceMixin implements BiomeSourceAccessor {

    @Shadow
    public abstract Set<Biome> possibleBiomes();

    public void bclRebuildFeatures() {
        //Feature sorting is now a task in ChunkGenerator
        BCLib.LOGGER.info("Rebuilding features in BiomeSource " + this);
        //featuresPerStep = Suppliers.memoize(() -> FeatureSorter.buildFeaturesPerStep(this.possibleBiomes().stream().toList(), true));
    }
}
