package org.betterx.bclib.items;

import org.betterx.bclib.interfaces.ItemModelProvider;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.level.material.Fluids;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;

public class BaseBucketItem extends MobBucketItem implements ItemModelProvider {
    public BaseBucketItem(EntityType<?> type, FabricItemSettings settings) {
        super(type, Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH, settings.stacksTo(1));
    }
}
