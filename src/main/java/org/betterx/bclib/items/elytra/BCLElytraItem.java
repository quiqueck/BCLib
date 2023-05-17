package org.betterx.bclib.items.elytra;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;

import net.fabricmc.fabric.api.entity.event.v1.FabricElytraItem;

public interface BCLElytraItem extends FabricElytraItem {
    ResourceLocation getModelTexture();

    double getMovementFactor();


    default void doVanillaElytraTick(LivingEntity entity, ItemStack chestStack) {
        vanillaElytraTick(entity, chestStack);
    }

    static void vanillaElytraTick(LivingEntity entity, ItemStack chestStack) {
        int nextRoll = entity.getFallFlyingTicks() + 1;

        if (!entity.level().isClientSide && nextRoll % 10 == 0) {
            if ((nextRoll / 10) % 2 == 0) {
                chestStack.hurtAndBreak(1, entity, (e) -> BCLElytraUtils.onBreak.accept(e, chestStack));
            }

            entity.gameEvent(GameEvent.ELYTRA_GLIDE);
        }
    }
}
