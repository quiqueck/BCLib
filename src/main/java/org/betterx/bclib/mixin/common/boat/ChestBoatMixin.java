package org.betterx.bclib.mixin.common.boat;

import org.betterx.bclib.items.boat.BoatTypeOverride;
import org.betterx.bclib.items.boat.CustomBoatTypeOverride;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.Item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChestBoat.class)
public abstract class ChestBoatMixin {
    @Shadow
    public abstract InteractionResult interact(Player player, InteractionHand interactionHand);

    @Inject(method = "getDropItem", at = @At("HEAD"), cancellable = true)
    void bcl_getDropItem(CallbackInfoReturnable<Item> cir) {
        if (this instanceof CustomBoatTypeOverride cbto) {
            BoatTypeOverride type = cbto.bcl_getCustomType();
            if (type != null) {
                BoatItem boat = type.getChestBoatItem();
                if (boat != null) {
                    cir.setReturnValue(boat);
                }
            }
        }
    }
}
