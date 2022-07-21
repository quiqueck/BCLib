package org.betterx.bclib.items.elytra;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class BCLElytraUtils {
    @FunctionalInterface
    public interface SlotProvider {
        ItemStack getElytra(LivingEntity entity, Function<EquipmentSlot, ItemStack> slotGetter);
    }

    public static SlotProvider slotProvider = null;
    public static BiConsumer<LivingEntity, ItemStack> onBreak =
            (entity, chestStack) -> entity.broadcastBreakEvent(EquipmentSlot.CHEST);
}
