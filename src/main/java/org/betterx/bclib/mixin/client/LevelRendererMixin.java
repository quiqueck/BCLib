package org.betterx.bclib.mixin.client;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.items.DebugDataItem;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Final
    @Shadow
    private Minecraft minecraft;
    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Shadow
    protected static void renderShape(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            VoxelShape voxelShape,
            double d,
            double e,
            double f,
            float g,
            float h,
            float i,
            float j
    ) {
    }

    @Inject(method = "renderLevel", at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;getModelViewStack()Lcom/mojang/blaze3d/vertex/PoseStack;",
            shift = At.Shift.BEFORE
    ))
    public void bcl_renderLevel(
            PoseStack poseStack,
            float f,
            long l,
            boolean bl,
            Camera camera,
            GameRenderer gameRenderer,
            LightTexture lightTexture,
            Matrix4f matrix4f,
            CallbackInfo info
    ) {
        if (BCLib.isDevEnvironment() && minecraft.hitResult instanceof BlockHitResult blockHitResult) {
            //will render a block outline when empty blocks are targeted
            ItemStack item = minecraft.player.getMainHandItem();
            if (item != null && (item.getItem() instanceof DebugDataItem ddi) && ddi.placeInAir) {
                final var pos = blockHitResult.getBlockPos();
                final var state = Blocks.DIRT.defaultBlockState();
                MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();
                VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
                Vec3 camPos = camera.getPosition();

                this.renderShape(
                        poseStack, consumer,
                        state.getShape(minecraft.level, pos, CollisionContext.of(camera.getEntity())),
                        pos.getX() - camPos.x(), pos.getY() - camPos.y(), pos.getZ() - camPos.z(),
                        246.0f / 0xff, 250.0f / 0xff, 112.0f / 0xff, 0.75F
                );
            }
        }
    }
}
