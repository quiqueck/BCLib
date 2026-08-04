package org.betterx.bclib.furniture.slots;

import org.betterx.bclib.client.models.BCLModels;
import org.betterx.bclib.furniture.block.BaseBarStool;
import de.ambertation.wover.block.api.BlockDefinition;
import de.ambertation.wover.block.api.BlockRegistry;
import de.ambertation.wover.block.api.client.trait.BlockModelTrait;
import de.ambertation.wover.block.api.client.trait.ClientBlockTraits;
import de.ambertation.wover.block.api.trait.BlockRecipeTrait;
import de.ambertation.wover.block.api.trait.BlockTraitLookup;
import de.ambertation.wover.block.api.trait.BlockTraits;
import de.ambertation.wover.recipe.api.RecipeBuilder;
import de.ambertation.wover.sets.api.blocks.BlockSet;
import de.ambertation.wover.sets.api.blocks.SlotFromDefinition;
import de.ambertation.wover.sets.api.blocks.SlotType;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Builds a bar-stool slot: a {@link BaseBarStool} derived from the set's slab, with a model generated from the
 * set's planks and cloth textures and a {@code "##"/"II"/"II"} recipe from slab + sticks.
 * <p>
 * The generated model needs no dedicated textures - {@link BCLModels#createBarStoolBlockModel} re-textures the
 * shared {@code bclib:block/bar_stool} model with the source and cloth blocks' textures.
 */
public class BarStool extends SlotFromDefinition {
    /** Resolves the cloth block whose texture the stool's seat uses. */
    protected final Supplier<Block> cloth;
    /** The slot the stool's block properties, behaviour and recipe material are taken from. */
    protected final SlotType sourceSlot;
    /** The slot whose texture the generated model uses. */
    protected final SlotType textureSlot;

    /**
     * Creates a factory for the {@link SlotType#BAR_STOOL} slot, built from the set's {@link SlotType#SLAB} and
     * textured with its {@link SlotType#PLANKS}.
     *
     * @param cloth resolves the cloth block, deferred so it may be configured after this factory is created
     */
    public BarStool(Supplier<Block> cloth) {
        this(SlotType.BAR_STOOL, SlotType.SLAB, SlotType.PLANKS, cloth);
    }

    /**
     * @param slot        the slot to register this bar stool under
     * @param sourceSlot  the slot the block properties, behaviour and recipe material are taken from
     * @param textureSlot the slot whose texture the generated model uses
     * @param cloth       resolves the cloth block, deferred so it may be configured after this factory is created
     */
    public BarStool(SlotType slot, SlotType sourceSlot, SlotType textureSlot, Supplier<Block> cloth) {
        super(slot);
        this.sourceSlot = sourceSlot;
        this.textureSlot = textureSlot;
        this.cloth = cloth;
    }

    @Override
    protected @Nullable BlockDefinition<?, ?> startBlockDefinition(
            @NotNull BlockRegistry registry,
            @NotNull BlockSet<?> set,
            @NotNull String name
    ) {
        final Block source = set.getBlockWithFallback(sourceSlot);
        final Block clothMaterial = cloth.get();
        // BehaviourHelper.from() retired (WP6.14): this slot is only ever used by wood sets, and every
        // wood-set bar stool already resolved to the Wood variant (BehaviourHelper.isMetal() is frozen
        // false, and no wood set's slab has a BASEDRUM instrument), so the choice is made explicit here.
        return registry
                .<BaseBarStool>defineDefaultBlock(
                        name,
                        def -> new BaseBarStool.Wood(source, clothMaterial, def.getProperties())
                )
                .replacePropertiesWithCopy(source);
    }

    @Override
    protected void addSlotSpecificDefinitions(BlockSet<?> set, BlockDefinition<?, ?> def) {
        def.addTags(BlockTags.MINEABLE_WITH_AXE);
        // The self-drop loot the base class used to generate through the retired BlockLootProvider interface.
        def.addTrait(BlockTraits.LOOT_TABLE.dropSelf());
    }

    @Override
    protected BlockRecipeTrait buildRecipe(BlockSet<?> set, BlockTraitLookup traitLookup) {
        // The recipe is built before the block exists, so the material lookup has to stay deferred
        final var source = set.recipeMaterialWithFallback(sourceSlot);
        return BlockTraits.RECIPE.with((key, block, context) -> RecipeBuilder
                .crafting(key.location(), block)
                .shape("##", "II", "II")
                .addMaterial('#', source)
                .addMaterial('I', Items.STICK)
                .group("bar_stool")
                .outputCount(1)
                .category(RecipeCategory.DECORATIONS)
                .build(context));
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected BlockModelTrait buildModel(BlockSet<?> set, BlockTraitLookup traitLookup) {
        return ClientModel.build(set, textureSlot, cloth);
    }

    /**
     * The model lambda must not live directly in {@link #buildModel}: annotations like
     * {@code @Environment(CLIENT)} on an enclosing method are not applied to the synthetic method javac
     * generates for the lambda body, so Fabric's stripper leaves that synthetic method (and its references to
     * client-only datagen types such as {@link BCLModels}) behind in this class file. Since this slot class is
     * always loaded on the server (it is part of every wooden set's slot map), verifying that orphaned method
     * would crash server startup. Keeping the lambda in a separate, never-unconditionally-loaded class file
     * avoids that.
     */
    @Environment(EnvType.CLIENT)
    private static class ClientModel {
        private static BlockModelTrait build(BlockSet<?> set, SlotType textureSlot, Supplier<Block> cloth) {
            return ClientBlockTraits.MODEL.with((key, block, generator) -> BCLModels.createBarStoolBlockModel(
                    generator,
                    block,
                    set.getBlockWithFallback(textureSlot),
                    cloth.get()
            ));
        }
    }
}
