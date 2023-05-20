package org.betterx.bclib.complexmaterials.entry;

import org.betterx.bclib.complexmaterials.ComplexMaterial;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
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
        var supplier = getBlockSupplier(parentMaterial);
        if (supplier != null) {
            final BlockEntry entry = new BlockEntry(suffix, supplier);
            modifyBlockEntry(parentMaterial, entry);
            return entry;
        }
        return null;
    }

    protected void modifyBlockEntry(M parentMaterial, @NotNull BlockEntry entry) {
    }

    @Override
    public void addRecipeEntry(M parentMaterial, Consumer<RecipeEntry> adder) {
        adder.accept(getRecipeEntry(parentMaterial));
    }

    @NotNull
    protected abstract BiFunction<ComplexMaterial, BlockBehaviour.Properties, Block> getBlockSupplier(M parentMaterial);

    protected @Nullable RecipeEntry getRecipeEntry(M parentMaterial) {
        return new RecipeEntry(suffix, this::getRecipeSupplier);
    }

    protected abstract @Nullable void getRecipeSupplier(
            ComplexMaterial parentMaterial,
            ResourceLocation id
    );

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
}
