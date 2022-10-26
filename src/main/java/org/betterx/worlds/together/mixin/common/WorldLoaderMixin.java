package org.betterx.worlds.together.mixin.common;

import org.betterx.worlds.together.world.event.WorldBootstrap;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.WorldLoader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(WorldLoader.class)
public class WorldLoaderMixin {
    //this is the place a new Registry access gets first istantiated
    //either when a new Datapack was added to a world on the create-screen
    //or because we did start world loading
    @ModifyArg(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/ReloadableServerResources;loadResources(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/core/RegistryAccess$Frozen;Lnet/minecraft/world/flag/FeatureFlagSet;Lnet/minecraft/commands/Commands$CommandSelection;ILjava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private static RegistryAccess.Frozen wt_newRegistry(RegistryAccess.Frozen frozen) {
        WorldBootstrap.InGUI.registryReady(frozen);
        return frozen;
    }
}
