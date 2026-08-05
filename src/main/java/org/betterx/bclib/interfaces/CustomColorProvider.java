package org.betterx.bclib.interfaces;

import net.minecraft.client.color.block.BlockTintSource;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface CustomColorProvider {
    // Client-only: BlockTintSource doesn't exist on a dedicated server. Every override must carry
    // this same annotation, or Fabric's environment stripper leaves an orphaned method behind that
    // still references BlockTintSource, crashing server startup the moment that class is verified
    // (e.g. via a constructor method reference).
    @Environment(EnvType.CLIENT)
    BlockTintSource getProvider();

}
