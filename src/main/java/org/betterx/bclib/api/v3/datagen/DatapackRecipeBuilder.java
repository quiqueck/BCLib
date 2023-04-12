package org.betterx.bclib.api.v3.datagen;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public interface DatapackRecipeBuilder {
    ResourceLocation getId();

    default String getNamespace() {
        return this.getId().getNamespace();
    }
    void build(Consumer<FinishedRecipe> cc);
}
