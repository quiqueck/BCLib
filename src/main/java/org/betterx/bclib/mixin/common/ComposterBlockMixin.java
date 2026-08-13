package org.betterx.bclib.mixin.common;

import org.betterx.bclib.trait.Compostables;
import org.betterx.bclib.trait.block.CompostableBlockTrait;
import org.betterx.bclib.trait.item.CompostableItemTrait;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.ComposterBlock;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Resolves compostability from a block's runtime {@link CompostableBlockTrait} - and, failing that, from the
 * item's own runtime {@link CompostableItemTrait} - at composter-use time instead of from vanilla's static
 * {@link ComposterBlock#COMPOSTABLES} map. Modded blocks/items are intentionally never added to that map (it
 * is only observable to datagen for vanilla items), so this wraps the three map lookups in the fill path and
 * falls back to the traits when vanilla reports the item as non-compostable.
 * <p>
 * Precedence is: vanilla map first, then the block trait, then the item trait. Vanilla items are untouched:
 * the wrapped {@link Operation} runs first and, when it already resolves the item, its result is returned
 * verbatim.
 * <p>
 * This covers the paths on {@link ComposterBlock} itself. Hoppers insert through the container returned by
 * {@code getContainer} instead, which runs a fourth map lookup of its own - see
 * {@link ComposterInputContainerMixin}.
 */
@Mixin(ComposterBlock.class)
public class ComposterBlockMixin {
    // useItemOn (player right-click, runs on both sides for the SUCCESS result) and insertItem (hopper /
    // dispenser) each gate on COMPOSTABLES.containsKey. Extend that gate so a BlockItem whose block carries
    // the compostable trait is accepted even though it was never put into the vanilla map. Split into one
    // handler per method (require = 1 each) so both the manual and the automated fill path are guaranteed to
    // be patched rather than only one of them satisfying a shared require.
    @WrapOperation(
            method = "useItemOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lit/unimi/dsi/fastutil/objects/Object2FloatMap;containsKey(Ljava/lang/Object;)Z"
            )
    )
    private boolean bclib_composterContainsKeyUse(
            Object2FloatMap<ItemLike> map,
            Object key,
            Operation<Boolean> original
    ) {
        return bclib_containsCompostable(map, key, original);
    }

    @WrapOperation(
            method = "insertItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lit/unimi/dsi/fastutil/objects/Object2FloatMap;containsKey(Ljava/lang/Object;)Z"
            )
    )
    private static boolean bclib_composterContainsKeyInsert(
            Object2FloatMap<ItemLike> map,
            Object key,
            Operation<Boolean> original
    ) {
        return bclib_containsCompostable(map, key, original);
    }

    private static boolean bclib_containsCompostable(
            Object2FloatMap<ItemLike> map,
            Object key,
            Operation<Boolean> original
    ) {
        if (original.call(map, key)) return true;
        if (!(key instanceof Item item)) return false;
        return Compostables.isCompostable(item);
    }

    // addItem reads the actual chance via COMPOSTABLES.getFloat (default return value -1 for absent items).
    // When vanilla has no chance for the item, substitute the trait's chance so composting progresses.
    @WrapOperation(
            method = "addItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lit/unimi/dsi/fastutil/objects/Object2FloatMap;getFloat(Ljava/lang/Object;)F"
            )
    )
    private static float bclib_composterGetFloat(
            Object2FloatMap<ItemLike> map,
            Object key,
            Operation<Float> original
    ) {
        final float vanilla = original.call(map, key);
        if (vanilla >= 0.0f) return vanilla;
        if (!(key instanceof Item item)) return vanilla;
        return Compostables.chanceFor(item);
    }
}
