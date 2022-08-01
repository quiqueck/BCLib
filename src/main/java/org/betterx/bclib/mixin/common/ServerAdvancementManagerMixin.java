package org.betterx.bclib.mixin.common;

import org.betterx.bclib.api.v2.advancement.AdvancementManager;

import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;

@Mixin(ServerAdvancementManager.class)
public class ServerAdvancementManagerMixin {
    @ModifyArg(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/AdvancementList;add(Ljava/util/Map;)V"))
    public Map<ResourceLocation, Advancement.Builder> bcl_interceptApply(Map<ResourceLocation, Advancement.Builder> map) {
        AdvancementManager.addAdvancements(map);
        return map;
    }
}
