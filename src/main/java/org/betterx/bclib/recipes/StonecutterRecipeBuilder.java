package org.betterx.bclib.recipes;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

public class StonecutterRecipeBuilder extends AbstractUnlockableRecipeBuilder<StonecutterRecipeBuilder> {

    protected StonecutterRecipeBuilder(
            ResourceLocation id,
            ItemLike output
    ) {
        super(id, output);
    }

    static StonecutterRecipeBuilder make(ResourceLocation id, ItemLike output) {
        return new StonecutterRecipeBuilder(id, output);
    }

    @Override
    public StonecutterRecipeBuilder setGroup(String group) {
        return super.setGroup(group);
    }

    /**
     * @param in
     * @return
     * @deprecated Use {@link #setPrimaryInputAndUnlock(ItemLike...)} instead
     */
    @Deprecated(forRemoval = true)
    public StonecutterRecipeBuilder setInput(ItemLike in) {
        return super.setPrimaryInputAndUnlock(in);
    }

    /**
     * @param in
     * @return
     * @deprecated use {@link #setPrimaryInputAndUnlock(TagKey)} instead
     */
    @Deprecated(forRemoval = true)
    public StonecutterRecipeBuilder setInput(TagKey<Item> in) {
        return super.setPrimaryInputAndUnlock(in);
    }

    @Override
    public StonecutterRecipeBuilder setOutputCount(int count) {
        return super.setOutputCount(count);
    }

    @Override
    protected void buildRecipe(Consumer<FinishedRecipe> cc) {
        final SingleItemRecipeBuilder builder = SingleItemRecipeBuilder.stonecutting(
                primaryInput, category, output.getItem(), output.getCount()
        );
        for (var item : unlocks.entrySet()) {
            builder.unlockedBy(item.getKey(), item.getValue());
        }
        builder.group(group);
        builder.save(cc, id);
    }

}
