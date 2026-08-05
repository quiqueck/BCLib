package org.betterx.bclib.mixin.common;

import org.betterx.bclib.recipes.BCLRecipeManager;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    @Shadow
    private RecipeMap recipes;

    @Inject(method = "apply(Lnet/minecraft/world/item/crafting/RecipeMap;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD"))
    public void bcl_interceptApply(
            RecipeMap recipeMap, ResourceManager resourceManager, ProfilerFiller profilerFiller, CallbackInfo ci
    ) {
        this.recipes = BCLRecipeManager.removeDisabledRecipes(resourceManager, recipeMap);
    }


    @Inject(method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    <I extends RecipeInput, T extends Recipe<I>> void bcl_sort(
            RecipeType<T> recipeType, I recipeInput, Level level, CallbackInfoReturnable<Optional<RecipeHolder<T>>> cir
    ) {

        var inter = this.recipes.byType(recipeType);
        var all = inter
                .stream()
                .filter((recipe) -> recipe.value().matches(recipeInput, level)).sorted((a, b) -> {
                    if (a.id().identifier().getNamespace().equals(b.id().identifier().getNamespace())) {
                        return a.id().identifier().getPath().compareTo(b.id().identifier().getPath());
                    }
                    if (a.id().identifier().getNamespace().equals("minecraft") && !b.id()
                                                                                  .identifier()
                                                                                  .getNamespace()
                                                                                  .equals("minecraft")) {
                        return 1;
                    } else if (!a.id().identifier().getNamespace().equals("minecraft") && b.id().identifier()
                                                                                         .getNamespace()
                                                                                         .equals("minecraft")) {
                        return -1;
                    } else {
                        return a.id().identifier().getNamespace().compareTo(b.id().identifier().getNamespace());
                    }
                }).toList();

        if (all.size() > 1) {
            cir.setReturnValue(Optional.of(all.getFirst()));
        }

    }

}