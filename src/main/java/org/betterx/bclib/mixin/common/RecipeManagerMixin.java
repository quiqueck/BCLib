package org.betterx.bclib.mixin.common;

import org.betterx.bclib.recipes.BCLRecipeManager;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import com.google.gson.JsonElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Optional;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    @Shadow
    protected abstract <C extends Container, T extends Recipe<C>> Map<ResourceLocation, RecipeHolder<T>> byType(
            RecipeType<T> recipeType
    );

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"))
    public void bcl_interceptApply(
            Map<ResourceLocation, JsonElement> map,
            ResourceManager resourceManager,
            ProfilerFiller profiler,
            CallbackInfo info
    ) {
        BCLRecipeManager.removeDisabledRecipes(resourceManager, map);
    }

    @Inject(method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    <C extends Container, T extends Recipe<C>> void bcl_sort(
            RecipeType<T> recipeType,
            C container,
            Level level,
            CallbackInfoReturnable<Optional<RecipeHolder<T>>> cir
    ) {
        var inter = this.byType(recipeType);
        var all = inter
                .values()
                .stream()
                .filter((recipe) -> recipe.value().matches(container, level)).sorted((a, b) -> {
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