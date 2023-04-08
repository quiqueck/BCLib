package org.betterx.bclib.blocks;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockSetType;


public class BaseWoodenButtonBlock extends BaseButtonBlock {
    public BaseWoodenButtonBlock(Block source, BlockSetType type) {
        super(source, Properties.copy(source).strength(0.5F, 0.5F).noOcclusion(), true, type);
    }

    @Override
    protected SoundEvent getSound(boolean clicked) {
        return clicked ? SoundEvents.WOODEN_BUTTON_CLICK_ON : SoundEvents.WOODEN_BUTTON_CLICK_OFF;
    }
}
