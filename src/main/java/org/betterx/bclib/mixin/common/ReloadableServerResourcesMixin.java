package org.betterx.bclib.mixin.common;

import org.betterx.bclib.recipes.BCLRecipeManager;

import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.flag.FeatureFlagSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Hands the {@link ResourceManager} of the running datapack load to {@link BCLRecipeManager}.
 * <p>
 * Up to 26.1 the disabled-recipe filter ran from {@code RecipeManager#apply(RecipeMap, ResourceManager,
 * ProfilerFiller)}, which was handed the resource manager directly. 26.3 made recipes a datapack registry:
 * {@code RecipeManager} is built from a bare {@code HolderLookup.Provider} in its constructor and never
 * sees a {@link ResourceManager}, so {@code recipes.json} (which is a datapack file, not a registry entry)
 * has to be reached some other way.
 * <p>
 * {@code ReloadableServerResources#loadResources} is the only entry point that builds a
 * {@code ReloadableServerResources} - verified against the 26.3 jar - and the {@code RecipeManager} is
 * constructed downstream of it, inside the future this method returns, for the same reload. Capturing the
 * manager here and consuming it in {@code RecipeManagerMixin} is therefore the same view the 26.1 code had.
 * <p>
 * Note the server's own {@code getResourceManager()} is not usable: on the initial world load the
 * {@code MinecraftServer} does not exist yet when this runs (the resources are part of the
 * {@code WorldStem}), and on a reload it still points at the <em>old</em> manager.
 */
@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {
    @Inject(method = "loadResources", at = @At("HEAD"))
    private static void bcl_captureResourceManager(
            ResourceManager resourceManager,
            LayeredRegistryAccess<RegistryLayer> layers,
            List<Registry.PendingTags<?>> postponedTags,
            FeatureFlagSet enabledFeatures,
            Commands.CommandSelection commandSelection,
            PermissionSet functionCompilationPermissions,
            Executor backgroundExecutor,
            Executor gameExecutor,
            CallbackInfoReturnable<CompletableFuture<ReloadableServerResources>> cir
    ) {
        BCLRecipeManager.setLoadingResourceManager(resourceManager);
    }
}
