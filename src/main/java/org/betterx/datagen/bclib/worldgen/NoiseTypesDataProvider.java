package org.betterx.datagen.bclib.worldgen;

import org.betterx.bclib.api.v2.generator.BCLChunkGenerator;

import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public class NoiseTypesDataProvider {
    public static void bootstrap(BootstapContext<NoiseGeneratorSettings> bootstrapContext) {
        bootstrapContext.register(
                BCLChunkGenerator.AMPLIFIED_NETHER,
                BCLChunkGenerator.amplifiedNether(bootstrapContext)
        );
    }

}
