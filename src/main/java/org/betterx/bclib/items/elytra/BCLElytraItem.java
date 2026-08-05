package org.betterx.bclib.items.elytra;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.gameevent.GameEvent;


public interface BCLElytraItem {
    static Item.Properties addElytraProperties(Item.Properties elytraItem) {
        return elytraItem.durability(432)
                         .rarity(Rarity.EPIC)
                         .component(DataComponents.GLIDER, Unit.INSTANCE)
                         .component(
                                 DataComponents.EQUIPPABLE,
                                 Equippable.builder(EquipmentSlot.CHEST)
                                           .setEquipSound(SoundEvents.ARMOR_EQUIP_ELYTRA)
                                           .setAsset(EquipmentAssets.ELYTRA)
                                           .setDamageOnHurt(false)
                                           .build()
                         )
                         .repairable(Items.PHANTOM_MEMBRANE);
    }
    Identifier getModelTexture();

    double getMovementFactor();


    default void doVanillaElytraTick(LivingEntity entity, ItemStack chestStack) {
        vanillaElytraTick(entity, chestStack);
    }

    static void vanillaElytraTick(LivingEntity entity, ItemStack chestStack) {
        int nextRoll = entity.getFallFlyingTicks() + 1;

        if (!entity.level().isClientSide() && nextRoll % 10 == 0) {
            if ((nextRoll / 10) % 2 == 0) {
                BCLElytraUtils.onBreak.accept(entity, chestStack);
                return;
            }

            entity.gameEvent(GameEvent.ELYTRA_GLIDE);
        }
    }
}
