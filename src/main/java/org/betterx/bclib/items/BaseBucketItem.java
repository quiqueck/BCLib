package org.betterx.bclib.items;

import org.betterx.bclib.interfaces.ItemModelProvider;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.level.material.Fluids;

public class BaseBucketItem extends MobBucketItem implements ItemModelProvider {
    public BaseBucketItem(EntityType<?> type, Item.Properties settings) {
        super(type, Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH, settings.stacksTo(1));
    }
}
