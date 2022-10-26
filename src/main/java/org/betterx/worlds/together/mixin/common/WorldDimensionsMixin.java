package org.betterx.worlds.together.mixin.common;

import net.minecraft.world.level.levelgen.WorldDimensions;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = WorldDimensions.class, priority = 100)
public class WorldDimensionsMixin {
    //TODO:1.19.3 no general registry access available yet as the layerd access is generated after this
//    @ModifyVariable(method = "bake", argsOnly = true, at = @At("HEAD"))
//    Registry<LevelStem> wt_bake(Registry<LevelStem> dimensionRegistry) {
//        final Registry<LevelStem> changedRegistry = WorldBootstrap.enforceInNewWorld(dimensionRegistry);
//        return changedRegistry;
//    }
}
