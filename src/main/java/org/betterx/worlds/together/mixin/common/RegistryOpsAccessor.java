package org.betterx.worlds.together.mixin.common;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RegistryOps.class)
public interface RegistryOpsAccessor {
    @Accessor("registryAccess")
    RegistryAccess bcl_getRegistryAccess();
}
