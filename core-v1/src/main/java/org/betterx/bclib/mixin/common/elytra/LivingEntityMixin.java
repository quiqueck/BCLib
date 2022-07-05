package org.betterx.bclib.mixin.common.elytra;

import org.betterx.bclib.items.elytra.BCLElytraItem;
import org.betterx.bclib.items.elytra.BCLElytraUtils;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(value = LivingEntity.class, priority = 199)
public abstract class LivingEntityMixin {
    @Shadow
    public abstract ItemStack getItemBySlot(EquipmentSlot equipmentSlot);

    @ModifyArg(
            method = "travel",
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isFallFlying()Z"),
                    to = @At(value = "INVOKE:LAST", target = "Lnet/minecraft/world/entity/LivingEntity;setSharedFlag(IZ)V")
            ),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V")
    )
    public Vec3 be_travel(Vec3 moveDelta) {
        ItemStack itemStack;
        if (BCLElytraUtils.slotProvider == null) itemStack = getItemBySlot(EquipmentSlot.CHEST);
        else itemStack = BCLElytraUtils.slotProvider.getElytra((LivingEntity) (Object) this, this::getItemBySlot);
        if (itemStack != null && itemStack.getItem() instanceof BCLElytraItem elytra) {
            double movementFactor = elytra.getMovementFactor();
            moveDelta = moveDelta.multiply(movementFactor, 1.0D, movementFactor);
        }
        return moveDelta;
    }
}
