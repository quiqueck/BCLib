package org.betterx.bclib.complexmaterials.entry;

import org.betterx.bclib.complexmaterials.ComplexMaterial;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SimpleMaterialSlot<M extends ComplexMaterial> extends MaterialSlot<M> {
    public SimpleMaterialSlot(@NotNull String suffix) {
        super(suffix);
    }

    @Override
    public void addBlockEntry(M parentMaterial, Consumer<BlockEntry> adder) {
        adder.accept(getBlockEntry(parentMaterial));
    }

    @Nullable
    protected BlockEntry getBlockEntry(M parentMaterial) {
        final BlockEntry entry = new BlockEntry(suffix, (c, p) -> this.createBlock(parentMaterial, p));
        modifyBlockEntry(parentMaterial, entry);
        return entry;
    }

    @NotNull
    protected abstract Block createBlock(M parentMaterial, BlockBehaviour.Properties settings);

    protected void modifyBlockEntry(M parentMaterial, @NotNull BlockEntry entry) {
    }

    @Override
    public void addRecipeEntry(M parentMaterial, Consumer<RecipeEntry> adder) {
        adder.accept(getRecipeEntry(parentMaterial));
    }

    protected @Nullable RecipeEntry getRecipeEntry(M parentMaterial) {
        return new RecipeEntry(suffix, this::makeRecipe);
    }

    protected abstract @Nullable void makeRecipe(
            ComplexMaterial parentMaterial,
            ResourceLocation id
    );

    @Override
    public void addItemEntry(M parentMaterial, Consumer<ItemEntry> adder) {
        ItemEntry item = getItemEntry(parentMaterial);
        if (item != null) {
            adder.accept(item);
        }
    }

    @Nullable
    protected ItemEntry getItemEntry(M parentMaterial) {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleMaterialSlot)) return false;
        SimpleMaterialSlot that = (SimpleMaterialSlot) o;
        return suffix.equals(that.suffix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(suffix);
    }

    public static <M extends ComplexMaterial> SimpleMaterialSlot<M> createBlockItem(
            @NotNull String suffix,
            BiFunction<ComplexMaterial, BlockBehaviour.Properties, Block> maker
    ) {
        return new SimpleMaterialSlot(suffix) {
            @Override
            protected @NotNull Block createBlock(ComplexMaterial parentMaterial, BlockBehaviour.Properties settings) {
                return maker.apply(parentMaterial, settings);
            }

            @Override
            protected @Nullable void makeRecipe(ComplexMaterial parentMaterial, ResourceLocation id) {

            }
        };
    }

    public static <M extends ComplexMaterial> SimpleMaterialSlot<M> createBlockItem(
            @NotNull String suffix,
            Supplier<Block> maker
    ) {
        return new SimpleMaterialSlot(suffix) {
            @Override
            protected @NotNull Block createBlock(ComplexMaterial parentMaterial, BlockBehaviour.Properties settings) {
                return maker.get();
            }

            @Override
            protected @Nullable void makeRecipe(ComplexMaterial parentMaterial, ResourceLocation id) {

            }
        };
    }
}
