package org.betterx.bclib.mixin.common;

import org.betterx.bclib.api.v2.ComposterAPI;
import org.betterx.bclib.trait.block.CompostableBlockTrait;
import org.betterx.bclib.trait.item.CompostableItemTrait;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.ResolvableNumber;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Resolves compostability from a block's runtime {@link CompostableBlockTrait} - and, failing that, from the
 * item's own runtime {@link CompostableItemTrait} or an explicit {@link ComposterAPI#allowCompost}
 * registration - at composter-use time.
 * <p>
 * 26.1 gated the two fill paths on {@code ComposterBlock.COMPOSTABLES.containsKey} and read the chance from
 * {@code COMPOSTABLES.getFloat}. 26.3 deleted that map: {@code useItemOn} and {@code insertItem} now read the
 * {@code minecraft:compostable} item component and hand it to {@code addLayer}, which turns it into a layer
 * count by calling {@link ResolvableNumber#getInt(LootContext, int)} on the component's {@code layers()} -
 * for our synthetic marker (a {@link ResolvableNumber.Reference} whose key is deliberately never registered)
 * that call falls through to its {@code 0} default. This mixin therefore hooks the two places that are the
 * exact analogues of the old ones:
 * <ol>
 *     <li>the {@code ItemStack.get(DataComponents.COMPOSTABLE)} in {@code useItemOn} (player right-click) and
 *     in {@code insertItem} (hopper / dispenser) - one call each, verified against the 26.3 jar - which is
 *     where 26.1 asked {@code containsKey}. When vanilla has no component, BCLib substitutes a marker one for
 *     items it knows a chance for, so all of vanilla's surrounding logic (level &lt; 7 check, sound, stat,
 *     item consumption, {@code SUCCESS} on the client) runs exactly as it does for a vanilla compostable;</li>
 *     <li>the {@code ResolvableNumber.getInt(LootContext, int)} call in {@code addLayer} - one call, verified
 *     - which is where 26.1 read the chance. When that call falls back to its {@code 0} default and the value
 *     is one of BCLib's markers, BCLib substitutes {@link ComposterAPI#runtimeLayerCount}'s roll (reproducing
 *     {@link ComposterAPI.RuntimeCompostChance}) instead.</li>
 * </ol>
 * Vanilla items are untouched in both cases: the wrapped {@link Operation} runs first and, whenever it
 * already produces a result, that result is returned verbatim.
 */
@Mixin(ComposterBlock.class)
public class ComposterBlockMixin {
    @WrapOperation(
            method = "useItemOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"
            )
    )
    private Object bclib_compostableOnUse(
            ItemStack itemStack,
            DataComponentType<?> componentType,
            Operation<Object> original
    ) {
        return bclib_compostableOr(itemStack, componentType, original);
    }

    @WrapOperation(
            method = "insertItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"
            )
    )
    private static Object bclib_compostableOnInsert(
            ItemStack itemStack,
            DataComponentType<?> componentType,
            Operation<Object> original
    ) {
        return bclib_compostableOr(itemStack, componentType, original);
    }

    @Unique
    private static Object bclib_compostableOr(
            ItemStack itemStack,
            DataComponentType<?> componentType,
            Operation<Object> original
    ) {
        final Object vanilla = original.call(itemStack, componentType);
        if (vanilla != null) return vanilla;
        return ComposterAPI.runtimeCompostable(itemStack.getItem());
    }

    @WrapOperation(
            method = "addLayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/loot/providers/number/ResolvableNumber;getInt(Lnet/minecraft/world/level/storage/loot/LootContext;I)I"
            )
    )
    private static int bclib_runtimeLayerCount(
            ResolvableNumber layers,
            LootContext lootContext,
            int fallback,
            Operation<Integer> original
    ) {
        final int vanilla = original.call(layers, lootContext, fallback);
        if (vanilla != 0) return vanilla;

        final int runtime = ComposterAPI.runtimeLayerCount(layers, lootContext);
        return runtime >= 0 ? runtime : vanilla;
    }
}
