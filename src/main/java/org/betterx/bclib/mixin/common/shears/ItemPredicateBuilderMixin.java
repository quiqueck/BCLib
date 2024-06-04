package org.betterx.bclib.mixin.common.shears;

import org.betterx.worlds.together.tag.v3.CommonItemTags;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(MatchTool.class)
public abstract class ItemPredicateBuilderMixin {
    @Unique
    private static final ResourceLocation BCL_MINECRAFT_SHEARS = ResourceLocation.withDefaultNamespace("shears");

    @Inject(method = "toolMatches", at = @At("HEAD"), cancellable = true)
    private static void bclib_isShears(
            ItemPredicate.Builder builder,
            CallbackInfoReturnable<LootItemCondition.Builder> cir
    ) {
        //TODO: 1.21 test if shears still  (used in loot test hasShearsOrSilkTouch)
        var built = builder.build();
        if (built.items().map(set -> set.stream().anyMatch(item -> item.is(BCL_MINECRAFT_SHEARS))).orElse(false)) {
            var shearTags = ItemPredicate.Builder.item().of(CommonItemTags.SHEARS);
            LootItemCondition.Builder orgMatcher = () -> new MatchTool(Optional.of(builder.build()));
            LootItemCondition.Builder matcher = () -> new MatchTool(Optional.of(shearTags.build()));
            cir.setReturnValue(new AnyOfCondition.Builder(orgMatcher, matcher));
        }
    }
}
