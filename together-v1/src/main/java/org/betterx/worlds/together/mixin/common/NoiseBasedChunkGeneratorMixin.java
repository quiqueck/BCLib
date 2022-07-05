package org.betterx.worlds.together.mixin.common;

import org.betterx.worlds.together.chunkgenerator.InjectableSurfaceRules;

import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseBasedChunkGeneratorMixin implements InjectableSurfaceRules {
}
