package org.betterx.bclib.mixin.client;

import org.betterx.bclib.blocks.BaseSignBlock;
import org.betterx.bclib.client.render.BaseSignBlockEntityRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SignEditScreen.class)
public abstract class SignEditScreenMixin extends AbstractSignEditScreen {

    @Shadow
    private SignRenderer.SignModel signModel;
    @Unique
    private boolean bclib_renderStick;
    @Unique
    private boolean bclib_isSign;

    public SignEditScreenMixin(SignBlockEntity signBlockEntity, boolean bl) {
        super(signBlockEntity, bl);
    }

    public SignEditScreenMixin(
            SignBlockEntity signBlockEntity,
            boolean bl,
            Component component
    ) {
        super(signBlockEntity, bl, component);
    }


    @Inject(method = "offsetSign", at = @At("TAIL"))
    private void bclib_offsetSign(PoseStack poseStack, BlockState blockState, CallbackInfo ci) {
        bclib_isSign = blockState.getBlock() instanceof BaseSignBlock;
        if (bclib_isSign) {
            bclib_renderStick = blockState.getValue(BaseSignBlock.FLOOR);
            if (bclib_renderStick) {
                poseStack.translate(0.0, 0.3125, 0.0);
            }
        }
    }

    @ModifyArg(method = "renderSignBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"), index = 1)
    private VertexConsumer bclib_renderSignBackground(VertexConsumer consumer) {
        if (bclib_isSign) {
            signModel.stick.visible = bclib_renderStick;
            Block block = sign.getBlockState().getBlock();
            MultiBufferSource.BufferSource bufferSource = this.minecraft.renderBuffers().bufferSource();
            return BaseSignBlockEntityRenderer.getConsumer(bufferSource, block);
        }
        return consumer;
    }
}
