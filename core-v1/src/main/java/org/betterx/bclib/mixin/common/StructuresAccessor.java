package org.betterx.bclib.mixin.common;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.Structures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Structures.class)
public interface StructuresAccessor {
    @Invoker
    static Holder<Structure> callRegister(ResourceKey<Structure> resourceKey, Structure structure) {
        throw new RuntimeException("Unexpected call");
    }
}
