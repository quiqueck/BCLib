package org.betterx.bclib.complexmaterials.entry;

import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.config.PathConfig;
import org.betterx.bclib.interfaces.TriConsumer;

import net.minecraft.resources.ResourceLocation;

public class RecipeEntry extends ComplexMaterialEntry {
    final TriConsumer<ComplexMaterial, PathConfig, ResourceLocation> initFunction;

    public RecipeEntry(String suffix, TriConsumer<ComplexMaterial, PathConfig, ResourceLocation> initFunction) {
        super(suffix);
        this.initFunction = initFunction;
    }

    public void init(ComplexMaterial material, PathConfig recipeConfig) {
        initFunction.accept(material, recipeConfig, getLocation(material.getModID(), material.getBaseName()));
    }
}
