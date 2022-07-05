package org.betterx.bclib.mixin.common;

import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.placement.PlacementContext;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlacementContext.class)
public class PlacementContextMixin implements org.betterx.bclib.interfaces.BCLPlacementContext {
    private Rotation bcl_rotation = Rotation.NONE;
    private Mirror bcl_mirror = Mirror.NONE;


    @Override
    public Rotation bcl_getRotation() {
        return bcl_rotation;
    }

    @Override
    public void bcl_setRotation(Rotation bcl_rotation) {
        this.bcl_rotation = bcl_rotation;
    }

    @Override
    public Mirror bcl_getMirror() {
        return bcl_mirror;
    }

    @Override
    public void bcl_setMirror(Mirror bcl_mirror) {
        this.bcl_mirror = bcl_mirror;
    }
}
