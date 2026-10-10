package org.betterx.bclib.mixin.common;

import org.betterx.bclib.recipes.BCLRecipeManager;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * 26.3 turned recipes into a datapack registry: {@code RecipeManager} is no longer a reload listener with
 * a {@code prepare}/{@code apply} pair, it is built once per world load from a
 * {@link HolderLookup.Provider} and fills its {@link RecipeMap} in its constructor. The old
 * {@code apply(RecipeMap, ResourceManager, ProfilerFiller)} target is gone, so the disabled-recipe filter
 * moves onto the lookup on its way into {@code RecipeMap.create} - the same place WorldWeaver's
 * {@code RecipeManagerMixin} hooks. Filtering the lookup rather than rebuilding the finished map is what
 * keeps Fabric's recipe-sync decoration intact; see {@code de.ambertation.wover.recipe.impl.RecipeLookups}.
 * <p>
 * The {@link ResourceManager} the filter reads {@code recipes.json} from is no longer a parameter; it is
 * published by {@code ReloadableServerResourcesMixin} from the load that is currently running (see there
 * for why the server's own manager is not usable). If none is available the filter is skipped and the
 * datapack recipes are used unchanged.
 * <p>
 * Priority 900 puts this modifier ahead of WorldWeaver's default-priority one on the same argument, so the
 * chain runs datapack recipes -> filtered here -> runtime recipes appended there. That preserves the 26.1
 * ordering, where BCLib filtered at {@code apply} HEAD and WoVer appended at {@code apply} TAIL: recipes
 * contributed at runtime are not subject to {@code recipes.json}.
 */
@Mixin(value = RecipeManager.class, priority = 900)
public abstract class RecipeManagerMixin {
    @Shadow
    @Final
    private RecipeMap recipes;

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/RecipeMap;create(Lnet/minecraft/core/HolderLookup;)Lnet/minecraft/world/item/crafting/RecipeMap;"
            )
    )
    private HolderLookup<Recipe<?>> bcl_removeDisabledRecipes(HolderLookup<Recipe<?>> datapackRecipes) {
        final ResourceManager resourceManager = BCLRecipeManager.loadingResourceManager();
        if (resourceManager == null) return datapackRecipes;
        return BCLRecipeManager.removeDisabledRecipes(resourceManager, datapackRecipes);
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
        final Identifier idA = a.id().identifier();
        final Identifier idB = b.id().identifier();
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