package org.betterx.bclib.interfaces;

import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

public interface BCLPlacementContext {
    Rotation bcl_getRotation();
    void bcl_setRotation(Rotation bcl_rotation);
    Mirror bcl_getMirror();
    void bcl_setMirror(Mirror bcl_mirror);
}
