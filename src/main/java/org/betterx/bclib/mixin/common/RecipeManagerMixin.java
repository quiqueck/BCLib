package org.betterx.bclib.mixin.common;

import org.betterx.bclib.recipes.BCLRecipeManager;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import com.google.gson.JsonElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"))
    public void bcl_interceptApply(
            Map<ResourceLocation, JsonElement> map,
            ResourceManager resourceManager,
            ProfilerFiller profiler,
            CallbackInfo info
    ) {
        BCLRecipeManager.removeDisabledRecipes(resourceManager, map);
    }

    @Shadow
    protected abstract <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> byType(
            RecipeType<T> recipeType
    );

    @Inject(method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    <I extends RecipeInput, T extends Recipe<I>> void bcl_sort(
            RecipeType<T> recipeType, I recipeInput, Level level, CallbackInfoReturnable<Optional<RecipeHolder<T>>> cir
    ) {
        var inter = this.byType(recipeType);
        var all = inter
                .stream()
                .filter((recipe) -> recipe.value().matches(recipeInput, level)).sorted((a, b) -> {
                    if (a.id().getNamespace().equals(b.id().getNamespace())) {
                        return a.id().getPath().compareTo(b.id().getPath());
                    }
                    if (a.id().getNamespace().equals("minecraft") && !b.id().getNamespace().equals("minecraft")) {
                        return 1;
                    } else if (!a.id().getNamespace().equals("minecraft") && b.id()
                                                                              .getNamespace()
                                                                              .equals("minecraft")) {
                        return -1;
                    } else {
                        return a.id().getNamespace().compareTo(b.id().getNamespace());
                    }
                }).toList();

        if (all.size() > 1) {
            cir.setReturnValue(Optional.of(all.get(0)));
        }

    }

}