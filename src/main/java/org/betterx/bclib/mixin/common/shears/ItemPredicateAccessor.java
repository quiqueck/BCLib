package org.betterx.bclib.mixin.common.shears;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.Item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;
import org.jetbrains.annotations.Nullable;

@Mixin(ItemPredicate.class)
public interface ItemPredicateAccessor {
    @Accessor("items")
    @Nullable Set<Item> bcl_getItems();
}
