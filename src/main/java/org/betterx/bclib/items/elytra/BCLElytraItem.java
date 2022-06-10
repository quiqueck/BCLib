package org.betterx.bclib.items.elytra;

import net.minecraft.resources.ResourceLocation;

import net.fabricmc.fabric.api.entity.event.v1.FabricElytraItem;

public interface BCLElytraItem extends FabricElytraItem {
    ResourceLocation getModelTexture();

    double getMovementFactor();
}
