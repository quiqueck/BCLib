package org.betterx.worlds.together.mixin.common;

import org.betterx.worlds.together.world.event.WorldBootstrap;

import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.WorldLoader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(WorldLoader.class)
public class WorldLoaderMixin {
    //this is the place a new Registry access gets first istantiated
    //either when a new Datapack was added to a world on the create-screen
    //or because we did start world loading
    @ModifyArg(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/ReloadableServerResources;loadResources(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/core/LayeredRegistryAccess;Lnet/minecraft/world/flag/FeatureFlagSet;Lnet/minecraft/commands/Commands$CommandSelection;ILjava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private static LayeredRegistryAccess<RegistryLayer> wover_captureRegistry(LayeredRegistryAccess<RegistryLayer> layered) {
        WorldBootstrap.InGUI.registryReady(layered.getAccessForLoading(RegistryLayer.RELOADABLE));
        return layered;
    }
}
