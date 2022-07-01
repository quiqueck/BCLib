package org.betterx.worlds.together.worldPreset;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class WorldPreset {
    private final net.minecraft.client.gui.screens.worldselection.WorldPreset parent;

    public WorldPreset(net.minecraft.client.gui.screens.worldselection.WorldPreset parent) {
        this.parent = parent;
    }

    protected ChunkGenerator generator(RegistryAccess registryAccess, long l) {
        return null;
    }
}
