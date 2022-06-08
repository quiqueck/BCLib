package org.betterx.bclib.interfaces;

import org.betterx.bclib.api.v2.levelgen.surface.SurfaceRuleBuilder;

import net.minecraft.world.level.block.state.BlockState;

public interface SurfaceMaterialProvider {
    BlockState getTopMaterial();
    BlockState getUnderMaterial();
    BlockState getAltTopMaterial();

    boolean generateFloorRule();
    SurfaceRuleBuilder surface();
}
