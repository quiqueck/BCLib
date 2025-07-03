package org.betterx.bclib.registry;

import org.betterx.bclib.client.render.BaseChestBlockEntityRenderer;
import org.betterx.bclib.furniture.renderer.RenderChair;
import org.betterx.bclib.items.boat.BoatTypeOverride;

import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.RaftModel;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;


@Environment(EnvType.CLIENT)
public class BaseBlockEntityRenders {
    public static void register() {
        BlockEntityRenderers.register(BaseBlockEntities.CHEST, BaseChestBlockEntityRenderer::new);

        LayerDefinition boatModel = BoatModel.createBoatModel();
        LayerDefinition chestBoatModel = BoatModel.createChestBoatModel();
        LayerDefinition raftModel = RaftModel.createRaftModel();
        LayerDefinition chestRaftModel = RaftModel.createChestRaftModel();

        BoatTypeOverride.values().forEach(type -> {
            EntityModelLayerRegistry.registerModelLayer(type.boatModelName, () -> type.isRaft ? raftModel : boatModel);
            EntityModelLayerRegistry.registerModelLayer(
                    type.chestBoatModelName,
                    () -> type.isRaft ? chestRaftModel : chestBoatModel
            );
        });

        EntityRendererRegistry.register(BaseBlockEntities.CHAIR, RenderChair::new);
    }
}
