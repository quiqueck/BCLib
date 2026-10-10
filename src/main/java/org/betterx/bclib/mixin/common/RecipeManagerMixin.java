package org.betterx.bclib.mixin.common;

import org.betterx.bclib.recipes.BCLRecipeManager;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    @Shadow
    private RecipeMap recipes;

    /**
     * Filters the recipes listed in {@code recipes.json} out of the map {@code apply} receives, before its
     * body runs. Vanilla's body is {@code this.recipes = recipeMap}, so assigning the field here (as BCLib
     * used to) was overwritten right away, and WorldWeaver's {@code RecipeManagerMixin} rebuilds the field
     * from this parameter at {@code TAIL} as well. Replacing the parameter itself is what both of them see -
     * the equivalent of 26.3, where the filter modifies the recipes on their way into {@code RecipeMap.create}.
     * Runtime recipes WorldWeaver appends at {@code TAIL} are not subject to {@code recipes.json}, as on 26.3.
     */
    @ModifyVariable(
            method = "apply(Lnet/minecraft/world/item/crafting/RecipeMap;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private RecipeMap bcl_removeDisabledRecipes(
            RecipeMap recipeMap, RecipeMap unfiltered, ResourceManager resourceManager, ProfilerFiller profilerFiller
    ) {
        return BCLRecipeManager.removeDisabledRecipes(resourceManager, recipeMap);
    }


    @Inject(method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    <I extends RecipeInput, T extends Recipe<I>> void bcl_sort(
            RecipeType<T> recipeType, I recipeInput, Level level, CallbackInfoReturnable<Optional<RecipeHolder<T>>> cir
    ) {
        if (recipeInput.isEmpty()) {
            cir.setReturnValue(Optional.empty());
            return;
        }

        // Called every tick by furnaces and modded machines, so pick the best match in a single pass
        // instead of collecting and sorting all matches (#225).
        RecipeHolder<T> best = null;
        for (RecipeHolder<T> recipe : this.recipes.byType(recipeType)) {
            if ((best == null || bcl_compare(recipe, best) < 0) && recipe.value().matches(recipeInput, level)) {
                best = recipe;
            }
        }
        cir.setReturnValue(Optional.ofNullable(best));
    }

    // Non-minecraft recipes win over minecraft ones, otherwise ordered by namespace and path
    @Unique
    private static int bcl_compare(RecipeHolder<?> a, RecipeHolder<?> b) {
        final ResourceLocation idA = a.id().location();
        final ResourceLocation idB = b.id().location();
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