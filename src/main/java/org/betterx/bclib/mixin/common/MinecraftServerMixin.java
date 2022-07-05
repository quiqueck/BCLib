package org.betterx.bclib.mixin.common;

import org.betterx.bclib.recipes.BCLRecipeManager;

import net.minecraft.server.MinecraftServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Mixin(value = MinecraftServer.class)
public class MinecraftServerMixin {
    @Shadow
    private MinecraftServer.ReloadableResources resources;


    @Inject(method = "reloadResources", at = @At(value = "RETURN"), cancellable = true)
    private void bclib_reloadResources(
            Collection<String> collection,
            CallbackInfoReturnable<CompletableFuture<Void>> info
    ) {
        bclib_injectRecipes();
    }

    @Inject(method = "loadLevel", at = @At(value = "RETURN"), cancellable = true)
    private void bclib_loadLevel(CallbackInfo info) {
        bclib_injectRecipes();
    }

    private void bclib_injectRecipes() {
        RecipeManagerAccessor accessor = (RecipeManagerAccessor) resources.managers().getRecipeManager();
        accessor.bclib_setRecipesByName(BCLRecipeManager.getMapByName(accessor.bclib_getRecipesByName()));
        accessor.bclib_setRecipes(BCLRecipeManager.getMap(accessor.bclib_getRecipes()));
    }
}
