package org.betterx.bclib.mixin.client;

import org.betterx.bclib.blocks.BaseSignBlock;
import org.betterx.bclib.client.render.BaseSignBlockEntityRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(SignEditScreen.class)
public abstract class SignEditScreenMixin extends Screen {
    @Shadow
    @Final
    private SignBlockEntity sign;
    @Shadow
    private SignRenderer.SignModel signModel;
    @Unique
    private boolean bclib_renderStick;
    @Unique
    private boolean bclib_isSign;

    protected SignEditScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V", locals = LocalCapture.CAPTURE_FAILSOFT, at = @At(ordinal = 1,
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
            shift = Shift.BEFORE
    ))
    private void bclib_checkOffset(
            PoseStack poseStack,
            int i,
            int j,
            float f,
            CallbackInfo ci,
            float g,
            BlockState blockState,
            boolean bl,
            boolean bl2,
            float h
    ) {
        bclib_isSign = blockState.getBlock() instanceof BaseSignBlock;
        if (bclib_isSign) {
            bclib_renderStick = blockState.getValue(BaseSignBlock.FLOOR);
            if (bclib_renderStick) {
                poseStack.translate(0.0, 0.3125, 0.0);
            }
        }
    }

    @ModifyArg(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"), index = 1)
    private VertexConsumer bclib_signRender(VertexConsumer consumer) {
        if (bclib_isSign) {
            signModel.stick.visible = bclib_renderStick;
            Block block = sign.getBlockState().getBlock();
            MultiBufferSource.BufferSource bufferSource = this.minecraft.renderBuffers().bufferSource();
            return BaseSignBlockEntityRenderer.getConsumer(bufferSource, block);
        }
        return consumer;
    }
}
