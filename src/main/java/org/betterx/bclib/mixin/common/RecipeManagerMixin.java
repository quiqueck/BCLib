package org.betterx.bclib.mixin.common;

import org.betterx.bclib.recipes.BCLRecipeManager;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import com.google.gson.JsonElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Optional;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    @Shadow
    protected abstract <C extends Container, T extends Recipe<C>> Map<ResourceLocation, T> byType(RecipeType<T> recipeType);

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
            CallbackInfoReturnable<Optional<T>> cir
    ) {
        // Called every tick by furnaces and modded machines, so pick the best match in a single pass
        // instead of collecting and sorting all matches (#225).
        T best = null;
        for (T recipe : this.<C, T>byType(recipeType).values()) {
            if ((best == null || bcl_compare(recipe, best) < 0) && recipe.matches(container, level)) {
                best = recipe;
            }
        }
        cir.setReturnValue(Optional.ofNullable(best));
    }

    // Non-minecraft recipes win over minecraft ones, otherwise ordered by namespace and path
    @Unique
    private static int bcl_compare(Recipe<?> a, Recipe<?> b) {
        final ResourceLocation idA = a.getId();
        final ResourceLocation idB = b.getId();
        final String nsA = idA.getNamespace();
        final String nsB = idB.getNamespace();
        if (nsA.equals(nsB)) {
            return idA.getPath().compareTo(idB.getPath());
        }
        final boolean vanillaA = nsA.equals("minecraft");
        final boolean vanillaB = nsB.equals("minecraft");
        if (vanillaA != vanillaB) {
            return vanillaA ? 1 : -1;
        }
        return nsA.compareTo(nsB);
    }

}