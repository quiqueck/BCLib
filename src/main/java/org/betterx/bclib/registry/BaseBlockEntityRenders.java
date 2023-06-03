package org.betterx.bclib.registry;

import org.betterx.bclib.client.render.BaseChestBlockEntityRenderer;
import org.betterx.bclib.items.boat.BoatTypeOverride;

import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.geom.builders.LayerDefinition;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;

@Environment(EnvType.CLIENT)
public class BaseBlockEntityRenders {
    public static void register() {
        BlockEntityRendererRegistry.register(BaseBlockEntities.CHEST, BaseChestBlockEntityRenderer::new);

        LayerDefinition boatModel = BoatModel.createBodyModel();
        LayerDefinition chestBoatModel = ChestBoatModel.createBodyModel();

        BoatTypeOverride.values().forEach(type -> {
            EntityModelLayerRegistry.registerModelLayer(type.boatModelName, () -> boatModel);
            EntityModelLayerRegistry.registerModelLayer(type.chestBoatModelName, () -> chestBoatModel);
        });
    }
}
