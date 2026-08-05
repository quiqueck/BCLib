package org.betterx.bclib.mixin.client;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.interfaces.AirSelectionItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
@Environment(EnvType.CLIENT)
public class DebugRendererMixin {
    @Inject(method = "emitGizmos", at = @At("TAIL"))
    void bcl_render(
            Frustum frustum,
            double camX,
            double camY,
            double camZ,
            float partialTick,
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
                final int color = airSelect.airSelectionColor();
                Gizmos.cuboid(pos, GizmoStyle.stroke(color));
            }
        }
    }
}
