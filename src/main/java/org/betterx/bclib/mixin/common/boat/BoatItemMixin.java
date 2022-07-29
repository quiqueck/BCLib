package org.betterx.bclib.mixin.common.boat;

import org.betterx.bclib.items.boat.CustomBoatTypeOverride;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BoatItem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BoatItem.class)
public class BoatItemMixin {
    @ModifyArg(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;noCollision(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Z"))
    Entity bcl_suse(Entity boat) {
        if (this instanceof CustomBoatTypeOverride self) {
            if (boat instanceof CustomBoatTypeOverride newBoat) {
                newBoat.setCustomType(self.bcl_getCustomType());
            }
        }
        return boat;
    }
}
