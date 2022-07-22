package org.betterx.bclib.networking;

import org.betterx.bclib.entrypoints.BCLibEntryPoint;

import net.minecraft.resources.ResourceLocation;

public interface VersionCheckEntryPoint extends BCLibEntryPoint {
    ResourceLocation updaterIcon(String modID);
}
