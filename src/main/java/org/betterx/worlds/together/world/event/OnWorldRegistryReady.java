package org.betterx.worlds.together.world.event;

import net.minecraft.core.RegistryAccess;

public interface OnWorldRegistryReady {
    void initRegistry(RegistryAccess access);
}
