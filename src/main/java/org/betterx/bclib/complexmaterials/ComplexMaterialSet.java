package org.betterx.bclib.complexmaterials;

import org.betterx.bclib.complexmaterials.entry.MaterialSlot;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.List;

public abstract class ComplexMaterialSet<M extends ComplexMaterialSet<?>> extends ComplexMaterial {
    List<MaterialSlot<M>> slots;

    protected ComplexMaterialSet(String modID, String baseName, String receipGroupPrefix) {
        super(modID, baseName, receipGroupPrefix);
    }

    protected abstract List<MaterialSlot<M>> createMaterialSlots();


    @Override
    protected final void initDefault(BlockBehaviour.Properties blockSettings, Item.Properties itemSettings) {
        this.slots = createMaterialSlots();

        for (MaterialSlot<M> slot : slots) {
            slot.addBlockEntry((M) this, this::addBlockEntry);
        }
        for (MaterialSlot<M> slot : slots) {
            slot.addItemEntry((M) this, this::addItemEntry);
        }

        this.initAdditional(blockSettings, itemSettings);
    }

    protected void initAdditional(BlockBehaviour.Properties blockSettings, Item.Properties itemSettings) {

    }

    @Override
    protected final void initDefaultRecipes() {
        super.initDefaultRecipes();
        this.initAdditionalRecipes();
    }

    protected void initAdditionalRecipes() {
        for (MaterialSlot<M> slot : slots) {
            slot.addRecipeEntry((M) this, this::addRecipeEntry);
        }
    }
}
