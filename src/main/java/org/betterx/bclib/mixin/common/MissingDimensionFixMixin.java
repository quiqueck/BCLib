package org.betterx.bclib.mixin.common;

import com.mojang.datafixers.DSL;
import net.minecraft.util.datafix.fixes.MissingDimensionFix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.HashMap;
import java.util.Map;

@Mixin(MissingDimensionFix.class)
public class MissingDimensionFixMixin {
    @ModifyArg(method = "makeRule", at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/DSL;taggedChoiceType(Ljava/lang/String;Lcom/mojang/datafixers/types/Type;Ljava/util/Map;)Lcom/mojang/datafixers/types/Type;"))
    Map<String, Object> bcl_addGenerator(Map<String, Object> map) {

        if (map.containsKey("minecraft:flat")) {
            Map<String, Object> nMap = new HashMap<>(map);
            nMap.put("bclib:betterx", DSL.remainderType());
            return nMap;
        }
        return map;
    }
}
