package org.betterx.bclib.complexmaterials.entry;

import org.betterx.bclib.complexmaterials.ComplexMaterial;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SimpleBlockOnlyMaterialSlot<M extends ComplexMaterial> extends SimpleMaterialSlot<M> {
    public SimpleBlockOnlyMaterialSlot(@NotNull String suffix) {
        super(suffix);
    }

    @Override
    @Nullable
    protected BlockEntry getBlockEntry(M parentMaterial) {
        final BlockEntry entry = new BlockEntry(suffix, false, (c, p) -> this.createBlock(parentMaterial, p));
        modifyBlockEntry(parentMaterial, entry);
        return entry;
    }

    public static <M extends ComplexMaterial> SimpleBlockOnlyMaterialSlot<M> createBlockOnly(
            @NotNull String suffix,
            BiFunction<ComplexMaterial, BlockBehaviour.Properties, Block> maker
    ) {
        return new SimpleBlockOnlyMaterialSlot(suffix) {
            @Override
            protected @NotNull Block createBlock(ComplexMaterial parentMaterial, BlockBehaviour.Properties settings) {
                return maker.apply(parentMaterial, settings);
            }

            @Override
            protected @Nullable void makeRecipe(ComplexMaterial parentMaterial, ResourceLocation id) {

            }
        };
    }

    public static <M extends ComplexMaterial> SimpleBlockOnlyMaterialSlot<M> createBlockOnly(
            @NotNull String suffix,
            Supplier<Block> maker
    ) {
        return new SimpleBlockOnlyMaterialSlot(suffix) {
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
