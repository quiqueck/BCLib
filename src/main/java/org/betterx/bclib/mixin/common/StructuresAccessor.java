package org.betterx.bclib.mixin.common;

import net.minecraft.data.worldgen.Structures;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(Structures.class)
public interface StructuresAccessor {
    //TODO: 1.19.3 Refactor
//    @Invoker
//    static Holder<Structure> callRegister(ResourceKey<Structure> resourceKey, Structure structure) {
//        throw new RuntimeException("Unexpected call");
//    }
}
