package org.betterx.bclib.mixin.client;

import org.betterx.bclib.client.BCLibClient;

import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ModelManager.class)
public class ModelManagerMixin {
    @Inject(method = "reload", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/util/profiling/ProfilerFiller;startTick()V"))
    private void bclib_loadCustomModels(
            PreparableReloadListener.PreparationBarrier preparationBarrier,
            ResourceManager resourceManager,
            ProfilerFiller profilerFiller,
            ProfilerFiller profilerFiller2,
            Executor executor,
            Executor executor2,
            CallbackInfoReturnable<CompletableFuture<Void>> cir
    ) {
        BCLibClient.lazyModelbakery().loadCustomModels(resourceManager);
    }
}
