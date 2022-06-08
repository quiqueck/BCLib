package org.betterx.bclib.mixin.client;

import org.betterx.bclib.api.v2.dataexchange.DataExchangeAPI;

import net.minecraft.client.Game;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Game.class)
public class GameMixin {

    @Inject(method = "onStartGameSession", at = @At("TAIL"))
    public void bclib_onStart(CallbackInfo ci) {
        DataExchangeAPI.sendOnEnter();
    }
}
