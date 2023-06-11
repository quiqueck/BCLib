package org.betterx.bclib.complexmaterials.set.stone;

import org.betterx.bclib.complexmaterials.StoneComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.BlockEntry;
import org.betterx.bclib.complexmaterials.entry.MaterialSlot;
import org.betterx.bclib.complexmaterials.entry.RecipeEntry;

import java.util.function.Consumer;

public class Source extends MaterialSlot<StoneComplexMaterial> {
    public Source() {
        super("source");
    }


    @Override
    public void addBlockEntry(StoneComplexMaterial parentMaterial, Consumer<BlockEntry> adder) {
        adder.accept(new BlockEntry(suffix, true, true, (c, p) -> parentMaterial.sourceBlock));
    }

    @Override
    public void addRecipeEntry(StoneComplexMaterial parentMaterial, Consumer<RecipeEntry> adder) {

    }
}
