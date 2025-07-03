package org.betterx.bclib.mixin.common;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EnchantingTableBlock;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(EnchantingTableBlock.class)
public abstract class EnchantingTableBlockMixin extends Block {
    public EnchantingTableBlockMixin(Properties settings) {
        super(settings);
    }
}
