package org.betterx.bclib.mixin.common;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.state.BlockState;

import org.betterx.bclib.api.v2.poi.BCLPoiType;
import org.betterx.bclib.api.v2.poi.PoiRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;

@Mixin(PoiTypes.class)
public abstract class PoiTypesMixin {

    @Shadow
    protected static PoiType register(Registry<PoiType> registry,
                                      ResourceKey<PoiType> resourceKey,
                                      Set<BlockState> set,
                                      int i,
                                      int j) {
        throw new RuntimeException("Just a Shadow.");
    }

    @Shadow
    protected static void registerBlockStates(Holder<PoiType> holder) {
        throw new RuntimeException("Just a Shadow.");
    }

    @Inject(method = "bootstrap", at = @At(value = "HEAD"))
    private static void bcl_bootstrap(Registry<PoiType> registry, CallbackInfoReturnable<PoiType> cir) {
        List<BCLPoiType> list = PoiRegistry.getCustomPOIs();
        for (BCLPoiType type : list) {
            register(registry, type.key, type.matchingStatesProvider.get(), type.maxTickets, type.validRange);
        }
    }
}
