package org.betterx.bclib.integration.emi;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import dev.emi.emi.api.stack.ItemEmiStack;

public class AnvilEmiStack extends ItemEmiStack {
    public AnvilEmiStack(ItemLike itemLike) {
        super(new ItemStack(itemLike));
    }

}
