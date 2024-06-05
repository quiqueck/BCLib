package org.betterx.bclib.mixin.client;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.interfaces.AirSelectionItem;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
@Environment(EnvType.CLIENT)
public class DebugRendererMixin {
    @Inject(method = "render", at = @At("TAIL"))
    void bcl_render(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            double camX,
            double camY,
            double camZ,
            CallbackInfo ci
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (BCLib.isDevEnvironment() && minecraft.hitResult instanceof BlockHitResult blockHitResult && minecraft.player != null) {
            //will render a block outline when empty blocks are targeted
            ItemStack item = minecraft.player.getMainHandItem();
            if (item != null
                    && (item.getItem() instanceof AirSelectionItem airSelect)
                    && airSelect.renderAirSelection()
                    && blockHitResult.getType() == HitResult.Type.MISS
            ) {
                final BlockPos pos = blockHitResult.getBlockPos();
                final BlockState state = Blocks.DIRT.defaultBlockState();
                final int color = airSelect.airSelectionColor();
                final VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
                final Camera camera = minecraft.gameRenderer.getMainCamera();
                final Vec3 camPos = camera.getPosition();

                LevelRendererAccessor.bclib_renderShape(
                        poseStack, consumer,
                        state.getShape(minecraft.level, pos, CollisionContext.of(camera.getEntity())),
                        pos.getX() - camPos.x(), pos.getY() - camPos.y(), pos.getZ() - camPos.z(),
                        FastColor.ARGB32.red(color) / (float) 0xff,
                        FastColor.ARGB32.green(color) / (float) 0xff,
                        FastColor.ARGB32.blue(color) / (float) 0xff,
                        FastColor.ARGB32.alpha(color) / (float) 0xff
                );
            }
        }
    }
}
