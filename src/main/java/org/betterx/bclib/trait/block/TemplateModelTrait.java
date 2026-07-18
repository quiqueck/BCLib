package org.betterx.bclib.trait.block;

import org.betterx.wover.block.api.client.trait.BlockModelTrait;
import org.betterx.wover.block.api.client.trait.ClientBlockTraits;
import org.betterx.wover.core.api.ModCore;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Optional;

/**
 * Model traits that generate a per-block <em>child</em> model from a shared, hand-authored
 * <em>template parent</em> - the pattern BetterNether/BetterEnd use for bespoke shapes (the custom ladder,
 * the custom trapdoor) that every wood family reuses with only different textures.
 * <p>
 * Instead of hand-authoring one full model per block (particle/texture placeholders plus the whole element
 * list), a single template model keeps the geometry (with {@code #texture}/{@code #side}/{@code #particle}
 * placeholders as its own textures) and each block only needs a tiny child model
 * ({@code {parent: <template>, textures: {...}}}) plus its blockstate and item model. This trait owns those
 * three generated pieces so the child model, blockstate and item model no longer have to be committed as
 * static assets - only the shared template parent stays hand-authored.
 * <p>
 * Like wover's {@code ModelTraitLibrary}, every public factory returns {@code null} outside a datagen
 * environment; the client-only vanilla datagen types are only touched from {@link Impl}, which is loaded
 * solely when {@link ModCore#isDatagen()} is {@code true} (see {@link PathBlockTrait} for the same guard).
 */
public class TemplateModelTrait {
    /**
     * A ladder-shaped block whose model parents a shared template (e.g. BetterNether's
     * {@code nether_reed_ladder}). Generates the child model (particle/texture from the block's own texture),
     * the horizontal-facing blockstate (the standard ladder rotation), and a flat item model - matching
     * wover's own {@code ModelTraitLibrary.ladder()}/{@code createLadder}, but with a custom parent instead of
     * vanilla {@code minecraft:block/ladder}.
     *
     * @param templateParent the shared ladder template model to parent the child model from
     * @return the model trait, or {@code null} outside of datagen
     */
    public static BlockModelTrait ladder(ResourceLocation templateParent) {
        return ModCore.isDatagen() ? Impl.ladder(templateParent) : null;
    }

    /**
     * A (non-orientable) trapdoor-shaped block whose model parents a shared template. Generates the child model
     * (particle/texture, and optionally a distinct {@code #side} texture, from the block's own texture), the
     * standard 16-variant facing/half/open blockstate (a single model rotated per state, as BetterNether's
     * trapdoors do), and delegates the item model to the block model.
     *
     * @param templateParent the shared trapdoor template model to parent the child model from
     * @param withSide        whether the template also needs a {@code #side} texture slot (mapped to the block's
     *                        own {@code _side} texture); {@code false} maps every face to the block's own texture
     * @return the model trait, or {@code null} outside of datagen
     */
    public static BlockModelTrait trapdoor(ResourceLocation templateParent, boolean withSide) {
        return ModCore.isDatagen() ? Impl.trapdoor(templateParent, withSide) : null;
    }

    @Environment(EnvType.CLIENT)
    private static class Impl {
        private static BlockModelTrait ladder(ResourceLocation templateParent) {
            return ClientBlockTraits.MODEL.with((key, block, generator) -> {
                final var tex = TextureMapping.getBlockTexture(block);
                final var mapping = new TextureMapping()
                        .put(TextureSlot.PARTICLE, tex)
                        .put(TextureSlot.TEXTURE, tex);
                final var template = new ModelTemplate(
                        Optional.of(templateParent),
                        Optional.empty(),
                        TextureSlot.PARTICLE,
                        TextureSlot.TEXTURE
                );
                final var model = template.create(block, mapping, generator.modelOutput());

                final PropertyDispatch<VariantMutator> rotation =
                        PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
                                        .select(Direction.EAST, BlockModelGenerators.Y_ROT_90)
                                        .select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180)
                                        .select(Direction.WEST, BlockModelGenerators.Y_ROT_270)
                                        .select(Direction.NORTH, BlockModelGenerators.NOP);
                generator.acceptBlockState(
                        MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model)).with(rotation)
                );

                generator.vanillaGenerator.registerSimpleFlatItemModel(block);
                generator.markItemModelProvided(block);
            });
        }

        private static BlockModelTrait trapdoor(ResourceLocation templateParent, boolean withSide) {
            return ClientBlockTraits.MODEL.with((key, block, generator) -> {
                final var tex = TextureMapping.getBlockTexture(block);
                final var mapping = new TextureMapping()
                        .put(TextureSlot.PARTICLE, tex)
                        .put(TextureSlot.TEXTURE, tex);
                final TextureSlot[] slots;
                if (withSide) {
                    mapping.put(TextureSlot.SIDE, tex.withSuffix("_side"));
                    slots = new TextureSlot[]{TextureSlot.PARTICLE, TextureSlot.TEXTURE, TextureSlot.SIDE};
                } else {
                    slots = new TextureSlot[]{TextureSlot.PARTICLE, TextureSlot.TEXTURE};
                }
                final var template = new ModelTemplate(Optional.of(templateParent), Optional.empty(), slots);
                final var model = template.create(block, mapping, generator.modelOutput());

                // A single model rotated per facing/half/open, matching BetterNether's hand-authored
                // trapdoor blockstate (its trapdoor is one custom mesh, not vanilla's 3 bottom/top/open models).
                final PropertyDispatch<VariantMutator> rotation =
                        PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING,
                                        BlockStateProperties.HALF,
                                        BlockStateProperties.OPEN)
                                        // half=bottom, open=false : flat on the floor
                                        .select(Direction.NORTH, net.minecraft.world.level.block.state.properties.Half.BOTTOM, false, BlockModelGenerators.NOP)
                                        .select(Direction.SOUTH, net.minecraft.world.level.block.state.properties.Half.BOTTOM, false, BlockModelGenerators.Y_ROT_180)
                                        .select(Direction.EAST, net.minecraft.world.level.block.state.properties.Half.BOTTOM, false, BlockModelGenerators.Y_ROT_90)
                                        .select(Direction.WEST, net.minecraft.world.level.block.state.properties.Half.BOTTOM, false, BlockModelGenerators.Y_ROT_270)
                                        // half=top, open=false : flipped to the ceiling (x180)
                                        .select(Direction.NORTH, net.minecraft.world.level.block.state.properties.Half.TOP, false, BlockModelGenerators.X_ROT_180)
                                        .select(Direction.SOUTH, net.minecraft.world.level.block.state.properties.Half.TOP, false, BlockModelGenerators.X_ROT_180.then(BlockModelGenerators.Y_ROT_180))
                                        .select(Direction.EAST, net.minecraft.world.level.block.state.properties.Half.TOP, false, BlockModelGenerators.X_ROT_180.then(BlockModelGenerators.Y_ROT_90))
                                        .select(Direction.WEST, net.minecraft.world.level.block.state.properties.Half.TOP, false, BlockModelGenerators.X_ROT_180.then(BlockModelGenerators.Y_ROT_270))
                                        // half=bottom, open=true : swung up from the floor (x90)
                                        .select(Direction.NORTH, net.minecraft.world.level.block.state.properties.Half.BOTTOM, true, BlockModelGenerators.X_ROT_90)
                                        .select(Direction.SOUTH, net.minecraft.world.level.block.state.properties.Half.BOTTOM, true, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_180))
                                        .select(Direction.EAST, net.minecraft.world.level.block.state.properties.Half.BOTTOM, true, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90))
                                        .select(Direction.WEST, net.minecraft.world.level.block.state.properties.Half.BOTTOM, true, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_270))
                                        // half=top, open=true : swung down from the ceiling (x270)
                                        .select(Direction.NORTH, net.minecraft.world.level.block.state.properties.Half.TOP, true, BlockModelGenerators.X_ROT_270.then(BlockModelGenerators.Y_ROT_180))
                                        .select(Direction.SOUTH, net.minecraft.world.level.block.state.properties.Half.TOP, true, BlockModelGenerators.X_ROT_270)
                                        .select(Direction.EAST, net.minecraft.world.level.block.state.properties.Half.TOP, true, BlockModelGenerators.X_ROT_270.then(BlockModelGenerators.Y_ROT_270))
                                        .select(Direction.WEST, net.minecraft.world.level.block.state.properties.Half.TOP, true, BlockModelGenerators.X_ROT_270.then(BlockModelGenerators.Y_ROT_90));
                generator.acceptBlockState(
                        MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model)).with(rotation)
                );

                generator.delegateItemModel(block, model);
            });
        }
    }
}
