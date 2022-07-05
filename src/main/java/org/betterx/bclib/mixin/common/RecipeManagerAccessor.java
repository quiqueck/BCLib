package org.betterx.bclib.mixin.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(RecipeManager.class)
public interface RecipeManagerAccessor {
    @Accessor("recipes")
    Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> bclib_getRecipes();

    @Accessor("recipes")
    void bclib_setRecipes(Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes);

    @Accessor("byName")
    Map<ResourceLocation, Recipe<?>> bclib_getRecipesByName();

    @Accessor("byName")
    void bclib_setRecipesByName(Map<ResourceLocation, Recipe<?>> recipes);
}