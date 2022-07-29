package org.betterx.bclib.client.render;

import org.betterx.bclib.items.boat.BoatTypeOverride;
import org.betterx.bclib.items.boat.CustomBoatTypeOverride;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value = EnvType.CLIENT)
public class BoatRenderer {

    public static boolean render(
            Boat boat,
            float f,
            float g,
            PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            int i
    ) {
        if (boat instanceof CustomBoatTypeOverride cbto) {
            BoatTypeOverride type = cbto.bcl_getCustomType();
            if (type != null) {
                boolean hasChest = boat instanceof ChestBoat;
                float k;
                poseStack.pushPose();
                poseStack.translate(0.0, 0.375, 0.0);
                poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f - f));
                float h = (float) boat.getHurtTime() - g;
                float j = boat.getDamage() - g;
                if (j < 0.0f) {
                    j = 0.0f;
                }
                if (h > 0.0f) {
                    poseStack.mulPose(Vector3f.XP.rotationDegrees(Mth.sin(h) * h * j / 10.0f * (float) boat.getHurtDir()));
                }
                if (!Mth.equal(k = boat.getBubbleAngle(g), 0.0f)) {
                    poseStack.mulPose(new Quaternion(new Vector3f(1.0f, 0.0f, 1.0f), boat.getBubbleAngle(g), true));
                }
                ResourceLocation resourceLocation = hasChest ? type.chestBoatTexture : type.boatTexture;
                BoatModel boatModel = type.getBoatModel(hasChest);
                poseStack.scale(-1.0f, -1.0f, 1.0f);
                poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0f));
                boatModel.setupAnim(boat, g, 0.0f, -0.1f, 0.0f, 0.0f);
                VertexConsumer vertexConsumer = multiBufferSource.getBuffer(boatModel.renderType(resourceLocation));
                boatModel.renderToBuffer(
                        poseStack, vertexConsumer, i,
                        OverlayTexture.NO_OVERLAY,
                        1.0f, 1.0f, 1.0f, 1.0f
                );
                if (!boat.isUnderWater()) {
                    VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RenderType.waterMask());
                    boatModel.waterPatch().render(poseStack, vertexConsumer2, i, OverlayTexture.NO_OVERLAY);
                }
                poseStack.popPose();

                return true;
            }

        }
        return false;
    }
}
