package org.betterx.bclib.mixin.common;

import org.betterx.bclib.interfaces.LootPoolAccessor;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;

import com.google.common.collect.Lists;
import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Optional;

/**
 * 26.3 reshaped {@link LootPool} substantially:
 * <ul>
 *     <li>Loot conditions and functions became the reloadable {@code minecraft:predicate} /
 *     {@code minecraft:item_modifier} registries, so the {@code List<LootItemCondition> conditions}
 *     and {@code List<LootItemFunction> functions} fields collapsed into single
 *     {@code Optional<Holder<...>>} fields named {@code condition} and {@code modifier}.</li>
 *     <li>Number providers became registries, so {@code rolls} is a {@code Holder<ContextIntProvider>}
 *     ({@code minecraft:context_int_provider}) and {@code bonusRolls} a {@code Holder<ContextFloatProvider>}
 *     ({@code minecraft:context_float_provider}); the 26.3 snapshots used a single
 *     {@code minecraft:number_provider} registry for both.</li>
 *     <li>All five fields are now {@code private}, so the shadows follow (they were {@code public}
 *     in 26.1). The constructor is private too; {@code bclib.accesswidener} widens it and its
 *     descriptor is updated to match.</li>
 * </ul>
 * Merging entries is unaffected: the copied pool keeps the original's condition, modifier and roll
 * counts by reference, exactly as before.
 */
@Mixin(LootPool.class)
public class LootPoolMixin implements LootPoolAccessor {
    @Shadow
    @Final
    private Holder<ContextIntProvider> rolls;
    @Shadow
    @Final
    private Holder<ContextFloatProvider> bonusRolls;

    @Shadow
    @Final
    private Optional<Holder<LootItemCondition>> condition;

    @Shadow
    @Final
    private Optional<Holder<LootItemFunction>> modifier;

    @Shadow
    @Final
    private List<LootPoolEntryContainer> entries;

    @Override
    public LootPool bcl_mergeEntries(List<LootPoolEntryContainer> newEntries) {
        final List<LootPoolEntryContainer> merged = Lists.newArrayList(entries);
        merged.addAll(newEntries);

        return new LootPool(
                merged,
                this.condition,
                this.modifier,
                this.rolls,
                this.bonusRolls
        );
    }
}
