package org.betterx.bclib.items.elytra;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
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
                damageElytra(entity, chestStack);
            }

            entity.gameEvent(GameEvent.ELYTRA_GLIDE);
        }
    }

    /**
     * Applies one point of flight damage like the vanilla elytra does.
     * <p>
     * {@link ItemStack#hurtAndBreak} uses the armor rule of Unbreaking for every {@link ArmorItem} (60% of the hits
     * ignore Unbreaking completely), which made armored elytras lose durability about three times faster than an
     * equally enchanted vanilla elytra (quiqueck/BetterEnd#321). Flight damage uses the plain Unbreaking chance
     * instead, armor damage from hits keeps the armor rule.
     */
    static void damageElytra(LivingEntity entity, ItemStack chestStack) {
        if (!(chestStack.getItem() instanceof ArmorItem)) {
            chestStack.hurtAndBreak(1, entity, (e) -> BCLElytraUtils.onBreak.accept(e, chestStack));
            return;
        }

        if (entity.level().isClientSide || !chestStack.isDamageableItem()) return;
        if (entity instanceof Player player && player.getAbilities().instabuild) return;

        final int unbreaking = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, chestStack);
        if (unbreaking > 0 && entity.getRandom().nextInt(unbreaking + 1) > 0) return;

        final int damage = chestStack.getDamageValue() + 1;
        if (entity instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(serverPlayer, chestStack, damage);
        }
        chestStack.setDamageValue(damage);

        if (damage >= chestStack.getMaxDamage()) {
            BCLElytraUtils.onBreak.accept(entity, chestStack);
            final Item item = chestStack.getItem();
            chestStack.shrink(1);
            if (entity instanceof Player player) {
                player.awardStat(Stats.ITEM_BROKEN.get(item));
            }
            chestStack.setDamageValue(0);
        }
    }
}
