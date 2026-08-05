package org.betterx.bclib.mixin.client;

import org.betterx.bclib.interfaces.CustomColorProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.core.registries.BuiltInRegistries;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import org.jetbrains.annotations.Nullable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Final
    @Shadow
    private BlockColors blockColors;

    @Shadow
    @Nullable
    public Screen screen;

    @Inject(method = "<init>*", at = @At("TAIL"))
    private void bclib_onMCInit(GameConfig args, CallbackInfo info) {
        BuiltInRegistries.BLOCK.forEach(block -> {
            if (block instanceof CustomColorProvider provider) {
                blockColors.register(List.of(provider.getProvider()), block);
            }
        });
    }
}
