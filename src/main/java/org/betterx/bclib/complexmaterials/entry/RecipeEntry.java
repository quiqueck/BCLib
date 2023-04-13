package org.betterx.bclib.complexmaterials.entry;

import org.betterx.bclib.complexmaterials.ComplexMaterial;

import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;

public class RecipeEntry extends ComplexMaterialEntry {
    final BiConsumer<ComplexMaterial, ResourceLocation> initFunction;

    public RecipeEntry(String suffix, BiConsumer<ComplexMaterial, ResourceLocation> initFunction) {
        super(suffix);
        this.initFunction = initFunction;
    }

    public void init(ComplexMaterial material) {
        initFunction.accept(material, getLocation(material.getModID(), material.getBaseName()));
    }
}
