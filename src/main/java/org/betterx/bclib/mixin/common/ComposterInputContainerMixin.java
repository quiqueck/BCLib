package org.betterx.bclib.mixin.common;

import org.betterx.bclib.trait.Compostables;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.ComposterBlock;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The hopper/automation counterpart to {@link ComposterBlockMixin}.
 * <p>
 * A hopper does not go through {@code ComposterBlock.insertItem}; it inserts through the
 * {@link net.minecraft.world.WorldlyContainer} that {@code ComposterBlock.getContainer} hands out, and that
 * container runs its <em>own</em> {@link ComposterBlock#COMPOSTABLES} lookup in
 * {@code canPlaceItemThroughFace}. Without this mixin a modded compostable (saplings, leaves, ...) could be
 * composted by hand but was silently rejected by a hopper, because only the manual and dispenser paths were
 * patched.
 * <p>
 * Once the item is accepted, the container's {@code setChanged} calls {@code ComposterBlock.addItem}, whose
 * chance lookup {@link ComposterBlockMixin} already patches - so accepting the item here is all that is needed
 * for the fill level to actually rise.
 */
@Mixin(targets = "net.minecraft.world.level.block.ComposterBlock$InputContainer")
public class ComposterInputContainerMixin {
    @WrapOperation(
            method = "canPlaceItemThroughFace",
            at = @At(
                    value = "INVOKE",
                    target = "Lit/unimi/dsi/fastutil/objects/Object2FloatMap;containsKey(Ljava/lang/Object;)Z"
            )
    )
    private boolean bclib_composterContainsKeyThroughFace(
            Object2FloatMap<ItemLike> map,
            Object key,
            Operation<Boolean> original
    ) {
        if (original.call(map, key)) return true;
        if (!(key instanceof Item item)) return false;
        return Compostables.isCompostable(item);
    }
}
