package org.betterx.bclib.recipes;

import net.minecraft.advancements.Criterion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractUnlockableRecipeBuilder<T extends AbstractUnlockableRecipeBuilder> extends AbstractSimpleRecipeBuilder<T> {

    protected AbstractUnlockableRecipeBuilder(ResourceLocation id, ItemLike output) {
        super(id, output);
    }

    protected final Map<String, Criterion<?>> unlocks = new HashMap<>();

    @Override
    public T unlockedBy(ItemLike item) {
        return super.unlockedBy(item);
    }

    @Override
    public T unlockedBy(TagKey<Item> tag) {
        return super.unlockedBy(tag);
    }

    @Override
    protected T unlocks(String name, Criterion<?> criterion) {
        this.unlocks.put(name, criterion);
        return (T) this;
    }
}
