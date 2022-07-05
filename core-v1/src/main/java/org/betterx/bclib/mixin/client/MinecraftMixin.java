package org.betterx.bclib.mixin.client;

import org.betterx.bclib.interfaces.CustomColorProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.main.GameConfig;
import net.minecraft.core.Registry;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Final
    @Shadow
    private BlockColors blockColors;

    @Final
    @Shadow
    private ItemColors itemColors;

    @Inject(method = "<init>*", at = @At("TAIL"))
    private void bclib_onMCInit(GameConfig args, CallbackInfo info) {
        Registry.BLOCK.forEach(block -> {
            if (block instanceof CustomColorProvider provider) {
                blockColors.register(provider.getProvider(), block);
                itemColors.register(provider.getItemProvider(), block.asItem());
            }
        });
    }
}
