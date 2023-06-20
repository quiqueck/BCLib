package org.betterx.bclib.mixin.client;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.interfaces.AirSelectionItem;
import org.betterx.bclib.interfaces.LevelRendererAccess;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.jetbrains.annotations.Nullable;

@Mixin(LevelRenderer.class)
@Environment(EnvType.CLIENT)
public abstract class LevelRendererMixin implements LevelRendererAccess {
    @Final
    @Shadow
    private Minecraft minecraft;
    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    public Particle bcl_addParticle(
            ParticleOptions particleOptions,
            double x, double y, double z,
            double vx, double vy, double vz
    ) {
        return this.addParticleInternal(particleOptions, false, x, y, z, vx, vy, vz);
    }

    @Shadow
    protected static void renderShape(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            VoxelShape voxelShape,
            double x, double y, double z,
            float r, float g, float b, float a
    ) {
    }

    @Shadow
    @Nullable
    protected abstract Particle addParticleInternal(
            ParticleOptions particleOptions,
            boolean bl,
            double d,
            double e,
            double f,
            double g,
            double h,
            double i
    );

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
            if (item != null
                    && (item.getItem() instanceof AirSelectionItem airSelect)
                    && airSelect.renderAirSelection()
                    && blockHitResult.getType() == HitResult.Type.MISS
            ) {
                final BlockPos pos = blockHitResult.getBlockPos();
                final BlockState state = Blocks.DIRT.defaultBlockState();
                final int color = airSelect.airSelectionColor();
                final MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();
                final VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
                final Vec3 camPos = camera.getPosition();

                this.renderShape(
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
