package org.betterx.bclib.blocks;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;


public class BaseStoneButtonBlock extends BaseButtonBlock {
    public BaseStoneButtonBlock(Block source) {
        super(source, Properties.copy(source).noOcclusion(), false);
    }

    @Override
    protected SoundEvent getSound(boolean clicked) {
        return clicked ? SoundEvents.STONE_BUTTON_CLICK_ON : SoundEvents.STONE_BUTTON_CLICK_OFF;
    }
}
