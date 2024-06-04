package org.betterx.bclib.mixin.common;

import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Recipe.class)
public interface RecipeMixin<C extends RecipeInput> {
    //Water Bottles are potions and they do not return an empty bottle in crafting Recipes
    //This mixin will fix that behaviour

    @Inject(method = "getRemainingItems", at = @At("RETURN"))
    default void bcl_getRemainingItems(C container, CallbackInfoReturnable<NonNullList<ItemStack>> cir) {
        NonNullList<ItemStack> remaining = cir.getReturnValue();

        for (int i = 0; i < remaining.size(); ++i) {
            ItemStack stack = container.getItem(i);
            if (stack.getItem() instanceof PotionItem pi) {
                Optional<Holder<Potion>> potion = stack
                        .getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
                        .potion();
                if (potion.map(p -> p == Potions.WATER).orElse(false))
                    remaining.set(i, new ItemStack(Items.GLASS_BOTTLE));
            }
        }
    }
}
