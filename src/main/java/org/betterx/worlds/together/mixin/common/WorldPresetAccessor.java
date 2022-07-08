package org.betterx.worlds.together.mixin.common;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(WorldPreset.class)
public interface WorldPresetAccessor {
    @Accessor("dimensions")
    Map<ResourceKey<LevelStem>, LevelStem> bcl_getDimensions();

    @Accessor("dimensions")
    @Mutable
    void bcl_setDimensions(Map<ResourceKey<LevelStem>, LevelStem> map);
}
