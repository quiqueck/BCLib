package org.betterx.bclib.mixin.common;

import org.betterx.bclib.api.v2.poi.PoiTypeExtension;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PoiType.class)
public class PoiTypeMixin implements PoiTypeExtension {
    private TagKey<Block> bcl_tag = null;


    @Inject(method = "is", cancellable = true, at = @At("HEAD"))
    void bcl_is(BlockState blockState, CallbackInfoReturnable<Boolean> cir) {
        if (bcl_tag != null && blockState.is(bcl_tag)) {
            cir.setReturnValue(true);
        }
    }

    public void bcl_setTag(TagKey<Block> tag) {
        bcl_tag = tag;
    }

    public TagKey<Block> bcl_getTag() {
        return bcl_tag;
    }

}
