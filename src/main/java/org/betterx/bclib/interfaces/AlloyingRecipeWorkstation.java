package org.betterx.bclib.interfaces;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public interface AlloyingRecipeWorkstation {
    static List<Block> getWorkstations() {
        return BuiltInRegistries.BLOCK
                .stream()
                .filter(b -> b instanceof AlloyingRecipeWorkstation)
                .toList();
    }

    static ItemStack getWorkstationIcon() {
        var workstations = AlloyingRecipeWorkstation.getWorkstations();
        if (workstations.isEmpty()) return new ItemStack(Blocks.BARRIER);
        return new ItemStack(workstations.get(0));
    }
}
