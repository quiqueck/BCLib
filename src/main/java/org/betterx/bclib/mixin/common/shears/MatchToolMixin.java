package org.betterx.bclib.mixin.common.shears;

import de.ambertation.wover.tag.api.predefined.CommonItemTags;

import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(MatchTool.class)
public class MatchToolMixin {
    @Shadow
    @Final
    private Optional<ItemPredicate> predicate;

    @Unique
    private static final byte BCL_CHECKED = 0x01;

    @Unique
    private static final byte BCL_SHEARS = 0x02;
    
    @Unique
    private byte bcl_isShears;

    @Inject(method = "test(Lnet/minecraft/world/level/storage/loot/LootContext;)Z", at = @At("HEAD"), cancellable = true)
    private void bcl_isShears(LootContext lootContext, CallbackInfoReturnable<Boolean> cir) {
        if ((bcl_isShears & BCL_CHECKED) == 0) {
            bcl_isShears = BCL_CHECKED;
            if (this.predicate.isPresent()) {
                if (this.predicate.get().items().isPresent()) {
                    final var items = this.predicate.get().items().get();
                    if (items.size() == 1 && items.get(0).value() == Items.SHEARS) {
                        bcl_isShears = (byte) (bcl_isShears | BCL_SHEARS);
                    }
                }
            }
        }
        if ((bcl_isShears & BCL_SHEARS) != 0) {
            ItemStack itemStack = (ItemStack) lootContext.getOptionalParameter(LootContextParams.TOOL);
            cir.setReturnValue(itemStack != null && itemStack.is(CommonItemTags.SHEARS));
        }
    }
}
