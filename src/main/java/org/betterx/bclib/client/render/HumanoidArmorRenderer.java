package org.betterx.bclib.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;

import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public abstract class HumanoidArmorRenderer implements ArmorRenderer {
    public interface CopyExtraState {
        void copyPropertiesFrom(HumanoidModel<LivingEntity> parentModel);
    }

    @Override
    public void render(
            PoseStack pose, MultiBufferSource buffer,
            ItemStack stack, LivingEntity entity, EquipmentSlot slot,
            int light, HumanoidModel<LivingEntity> parentModel
    ) {
        HumanoidModel<LivingEntity> model = getModelForSlot(entity, slot);
        if (model != null) {
            Item item = stack.getItem();
            if (!(item instanceof ArmorItem)) {
                return;
            }
            ArmorItem armorItem = (ArmorItem) item;
            if (armorItem.getEquipmentSlot() != slot) {
                return;
            }
            parentModel.copyPropertiesTo(model);
            if (model instanceof CopyExtraState mdl) {
                mdl.copyPropertiesFrom(parentModel);
            }
            setPartVisibility(model, slot);
            renderModel(
                    pose, buffer, light, model,
                    getTextureForSlot(slot, usesInnerModel(slot)),
                    1.0f, 1.0f, 1.0f
            );

            if (stack.hasFoil()) {
                this.renderGlint(pose, buffer, light, model);
            }
        }
    }

    @NotNull
    protected abstract ResourceLocation getTextureForSlot(EquipmentSlot slot, boolean innerLayer);
    protected abstract HumanoidModel<LivingEntity> getModelForSlot(LivingEntity entity, EquipmentSlot slot);

    protected boolean usesInnerModel(EquipmentSlot equipmentSlot) {
        return equipmentSlot == EquipmentSlot.LEGS;
    }

    protected void renderModel(
            PoseStack pose, MultiBufferSource buffer,
            int light,
            HumanoidModel<LivingEntity> humanoidModel, ResourceLocation texture,
            float r, float g, float b
    ) {
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.armorCutoutNoCull(texture));
        humanoidModel.renderToBuffer(pose, vertexConsumer, light, OverlayTexture.NO_OVERLAY, r, g, b, 1.0f);
    }

    protected void renderGlint(
            PoseStack pose,
            MultiBufferSource buffer,
            int light,
            HumanoidModel<LivingEntity> humanoidModel
    ) {
        humanoidModel.renderToBuffer(
                pose, buffer.getBuffer(RenderType.armorEntityGlint()), light, OverlayTexture.NO_OVERLAY,
                1.0f, 1.0f, 1.0f, 1.0f
        );
    }

    protected void setPartVisibility(HumanoidModel<LivingEntity> humanoidModel, EquipmentSlot equipmentSlot) {
        humanoidModel.setAllVisible(false);
        switch (equipmentSlot) {
            case HEAD: {
                humanoidModel.head.visible = true;
                humanoidModel.hat.visible = true;
                break;
            }
            case CHEST: {
                humanoidModel.body.visible = true;
                humanoidModel.rightArm.visible = true;
                humanoidModel.leftArm.visible = true;
                break;
            }
            case LEGS: {
                humanoidModel.body.visible = true;
                humanoidModel.rightLeg.visible = true;
                humanoidModel.leftLeg.visible = true;
                break;
            }
            case FEET: {
                humanoidModel.rightLeg.visible = true;
                humanoidModel.leftLeg.visible = true;
            }
        }
    }
}
