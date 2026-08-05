package org.betterx.bclib.mixin.common;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.class)
public interface ItemAccessor {
    @Accessor("craftingRemainingItem")
    @Mutable
    public ItemStackTemplate bcl_craftingRemainingItem();

    @Accessor("craftingRemainingItem")
    @Mutable
    public void bcl_setCraftingRemainingItem(ItemStackTemplate item);
}
