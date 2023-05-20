package org.betterx.bclib.complexmaterials.entry;

import org.betterx.bclib.complexmaterials.ComplexMaterial;

import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

public abstract class MaterialSlot<M extends ComplexMaterial> {
    @NotNull
    public final String suffix;

    public MaterialSlot(@NotNull String suffix) {
        this.suffix = suffix;
    }

    public abstract void addBlockEntry(M parentMaterial, Consumer<BlockEntry> adder);
    public abstract void addRecipeEntry(M parentMaterial, Consumer<RecipeEntry> adder);

    public void addItemEntry(M parentMaterial, Consumer<ItemEntry> adder) {
    }


    public void onInit(M parentMaterial) {
    }
}
