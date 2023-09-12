package org.betterx.worlds.together.levelgen;

import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.world.event.WorldBootstrap;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public class WorldGenUtil {
    public static class Context extends StemContext {
        public Context(
                Holder<DimensionType> dimension,
                HolderGetter<StructureSet> structureSets,
                Holder<NoiseGeneratorSettings> generatorSettings
        ) {
            super(dimension, structureSets, generatorSettings);
        }
    }

    public static class StemContext {
        public final Holder<DimensionType> dimension;
        public final HolderGetter<StructureSet> structureSets;
        public final Holder<NoiseGeneratorSettings> generatorSettings;

        public StemContext(
                Holder<DimensionType> dimension,
                HolderGetter<StructureSet> structureSets,
                Holder<NoiseGeneratorSettings> generatorSettings
        ) {
            this.dimension = dimension;
            this.structureSets = structureSets;
            this.generatorSettings = generatorSettings;
        }
    }

    public static ResourceLocation getBiomeID(Biome biome) {
        ResourceLocation id = null;
        RegistryAccess access = WorldBootstrap.getLastRegistryAccessOrElseBuiltin();

        id = access.registryOrThrow(Registries.BIOME).getKey(biome);

        if (id == null) {
            WorldsTogether.LOGGER.error("Unable to get ID for " + biome + ".");
        }

        return id;
    }
}
