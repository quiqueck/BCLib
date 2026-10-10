package org.betterx.bclib.furniture.slots;

import org.betterx.bclib.client.models.BCLModels;
import org.betterx.bclib.furniture.block.BaseTaburet;
import org.betterx.bclib.trait.block.BCLModelKeys;
import de.ambertation.wover.block.api.BlockDefinition;
import de.ambertation.wover.block.api.BlockRegistry;
import de.ambertation.wover.block.api.client.model.ClientBlockModelRegistry;
import de.ambertation.wover.block.api.client.trait.BlockModelTrait.ModelFactory;
import de.ambertation.wover.block.api.model.BlockModelBinding;
import de.ambertation.wover.block.api.model.ModelKey;
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Builds a taburet (backless stool) slot: a {@link BaseTaburet} derived from the set's slab, with a model
 * generated from the set's planks texture and a {@code "##"/"II"} recipe from slab + sticks.
 * <p>
 * The generated model needs no dedicated textures - {@link BCLModels#createTaburetBlockModel} re-textures the
 * shared {@code bclib:block/taburet} model with the source block's texture.
 */
public class Taburet extends SlotFromDefinition {
    /** The slot the taburet's block properties, behaviour and recipe material are taken from. */
    protected final SlotType sourceSlot;
    /** The slot whose texture the generated model uses. */
    protected final SlotType textureSlot;

    /**
     * Creates a factory for the {@link SlotType#TABURET} slot, built from the set's
     * {@link SlotType#SLAB} and textured with its {@link SlotType#PLANKS}.
     */
    public Taburet() {
        this(SlotType.TABURET, SlotType.SLAB, SlotType.PLANKS);
    }

    /**
     * @param slot        the slot to register this taburet under
     * @param sourceSlot  the slot the block properties, behaviour and recipe material are taken from
     * @param textureSlot the slot whose texture the generated model uses
     */
    public Taburet(SlotType slot, SlotType sourceSlot, SlotType textureSlot) {
        super(slot);
        this.sourceSlot = sourceSlot;
        this.textureSlot = textureSlot;
    }

    @Override
    protected @Nullable BlockDefinition<?, ?> startBlockDefinition(
            @NotNull BlockRegistry registry,
            @NotNull BlockSet<?> set,
            @NotNull String name
    ) {
        final Block source = set.getBlockWithFallback(sourceSlot);
        // BehaviourHelper.from() retired (WP6.14): this slot is only ever used by wood sets, and every
        // wood-set taburet already resolved to the Wood variant (BehaviourHelper.isMetal() is frozen false,
        // and no wood set's slab has a BASEDRUM instrument), so the choice is made explicit here.
        return registry
                .<BaseTaburet>defineDefaultBlock(name, def -> new BaseTaburet.Wood(source, def.getProperties()))
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
                .shape("##", "II")
                .addMaterial('#', source)
                .addMaterial('I', Items.STICK)
                .group("taburet")
                .outputCount(1)
                .category(RecipeCategory.DECORATIONS)
                .build(context));
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected BlockModelBinding buildModel(BlockSet<?> set, BlockTraitLookup traitLookup) {
        return BlockModelBinding.of(MODEL, new ModelParams(set, textureSlot));
    }

    private record ModelParams(BlockSet<?> set, SlotType textureSlot) {
    }

    private static final ModelKey<ModelParams> MODEL = BCLModelKeys.key("taburet");

    /**
     * Registers the client generators behind this class's model keys. Called from the mod's
     * {@code wover.client.traits} entrypoint.
     */
    @Environment(EnvType.CLIENT)
    public static void registerClientModels() {
        ClientModel.register();
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
        private static void register() {
            ClientBlockModelRegistry.register(
                    MODEL,
                    (key, block, generator, payload) -> buildFrom(payload).provideBlockModels(key, block, generator)
            );
        }

        private static ModelFactory buildFrom(ModelParams p) {
            return build(p.set(), p.textureSlot());
        }

        private static ModelFactory build(BlockSet<?> set, SlotType textureSlot) {
            return (key, block, generator) -> BCLModels.createTaburetBlockModel(
                    generator,
                    block,
                    set.getBlockWithFallback(textureSlot)
            );
        }
    }
}
