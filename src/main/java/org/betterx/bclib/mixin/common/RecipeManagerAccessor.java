package org.betterx.bclib.mixin.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(RecipeManager.class)
public interface RecipeManagerAccessor {
    @Accessor("recipes")
    <C extends Container, T extends Recipe<C>> Map<RecipeType<T>, Map<ResourceLocation, T>> bclib_getRecipes();

    @Accessor("recipes")
    <C extends Container, T extends Recipe<C>> void bclib_setRecipes(Map<RecipeType<T>, Map<ResourceLocation, T>> recipes);

    @Accessor("byName")
    <C extends Container, T extends Recipe<C>> Map<ResourceLocation, T> bclib_getRecipesByName();

    @Accessor("byName")
    <C extends Container, T extends Recipe<C>> void bclib_setRecipesByName(Map<ResourceLocation, T> recipes);
}