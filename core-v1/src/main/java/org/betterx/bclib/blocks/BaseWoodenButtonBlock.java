package org.betterx.bclib.blocks;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;

public class BaseWoodenButtonBlock extends BaseButtonBlock {
    public BaseWoodenButtonBlock(Block source) {
        super(source, FabricBlockSettings.copyOf(source).strength(0.5F, 0.5F).noOcclusion(), true);
    }

    @Override
    protected SoundEvent getSound(boolean clicked) {
        return clicked ? SoundEvents.WOODEN_BUTTON_CLICK_ON : SoundEvents.WOODEN_BUTTON_CLICK_OFF;
    }
}
