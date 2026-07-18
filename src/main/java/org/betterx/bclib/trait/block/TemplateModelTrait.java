package org.betterx.bclib.trait.block;

import org.betterx.wover.block.api.client.trait.BlockModelTrait;
import org.betterx.wover.block.api.client.trait.ClientBlockTraits;
import org.betterx.wover.core.api.ModCore;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedList;
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

    /**
     * A full-cube-shaped block whose model is a plain texture-swap child of a shared, hand-authored cube template
     * (e.g. BetterEnd's {@code menger_sponge} fractal mesh or its {@code tint_cube}). Generates the child model
     * ({@code {parent: <template>, textures: {texture: <block texture>}}}, plus a {@code particle} slot when the
     * template does not resolve its own particle), the plain single-variant blockstate, and an item model delegated
     * to that block model - matching wover's {@code externalModelDelegatedItem()} for such blocks, but with the model
     * generated instead of hand-authored.
     *
     * @param templateParent the shared cube template model to parent the child model from
     * @param withParticle   whether the template needs an explicit {@code #particle} slot (mapped to the block's own
     *                       texture); {@code false} when the template already declares {@code "particle": "#texture"}
     * @return the model trait, or {@code null} outside of datagen
     */
    public static BlockModelTrait cube(ResourceLocation templateParent, boolean withParticle) {
        return ModCore.isDatagen() ? Impl.cube(templateParent, withParticle) : null;
    }

    /**
     * A block whose model is a texture-swap child of a shared, hand-authored template (e.g. BetterEnd's {@code charnia}
     * mesh) placed with a random {@code 0/90/180/270} Y rotation. Generates the child model
     * ({@code {parent: <template>, textures: {texture: <block texture>}}}) and a single-condition blockstate whose
     * variant is the four equally-weighted Y rotations of that model - the randomized look BetterEnd's hand-authored
     * blockstates shipped. The item model is left as the block's hand-authored static flat model
     * ({@code item/<name>}), matching wover's {@code externalModel()} item handling (these blocks carry a dedicated
     * inventory icon distinct from their block texture).
     *
     * @param templateParent the shared template model to parent the child model from
     * @return the model trait, or {@code null} outside of datagen
     */
    public static BlockModelTrait randomYRotation(ResourceLocation templateParent) {
        return ModCore.isDatagen() ? Impl.randomYRotation(templateParent) : null;
    }

    /**
     * A stairs-shaped block whose straight/inner/outer models are plain texture-swap children of shared,
     * hand-authored stair templates (e.g. BetterEnd's emissive {@code lit_stairs}/{@code lit_stairs_inner}/
     * {@code lit_stairs_outer} meshes, whose {@code shade:false} directional UVs must not be uv-locked).
     * Generates the three child models ({@code {parent: <template>, textures: {bottom, top, side}}}), the full
     * 40-variant facing/half/shape blockstate as the standard vanilla stair rotation matrix <em>without</em>
     * {@code uvlock} (the hand-authored form these lit templates ship), and an item model delegated to the
     * straight block model.
     *
     * @param templateStraight the shared straight-stairs template model
     * @param templateInner    the shared inner-corner template model
     * @param templateOuter    the shared outer-corner template model
     * @param bottomTexture    the {@code bottom} face texture
     * @param topTexture       the {@code top} face texture
     * @param sideTexture      the {@code side} face texture
     * @return the model trait, or {@code null} outside of datagen
     */
    public static BlockModelTrait stairs(
            ResourceLocation templateStraight,
            ResourceLocation templateInner,
            ResourceLocation templateOuter,
            ResourceLocation bottomTexture,
            ResourceLocation topTexture,
            ResourceLocation sideTexture
    ) {
        return ModCore.isDatagen()
                ? Impl.stairs(templateStraight, templateInner, templateOuter, bottomTexture, topTexture, sideTexture)
                : null;
    }

    /**
     * A wall-shaped block whose post/low-side/tall-side models are plain texture-swap children of shared,
     * hand-authored wall templates (e.g. BetterEnd's emissive {@code lit_wall_post}/{@code lit_wall_side}/
     * {@code lit_wall_side_tall} meshes). Generates the three child models
     * ({@code {parent: <template>, textures: {wall}}}), the standard vanilla wall multipart blockstate (via
     * {@link BlockModelGenerators#createWall}, i.e. the uv-locked post/low/tall multipart the hand-authored form
     * ships), and the {@code wall_inventory} item model.
     *
     * @param templatePost     the shared wall-post template model
     * @param templateSide     the shared low-side template model
     * @param templateSideTall the shared tall-side template model
     * @param wallTexture      the {@code wall} texture bound into every template
     * @return the model trait, or {@code null} outside of datagen
     */
    public static BlockModelTrait wall(
            ResourceLocation templatePost,
            ResourceLocation templateSide,
            ResourceLocation templateSideTall,
            ResourceLocation wallTexture
    ) {
        return ModCore.isDatagen()
                ? Impl.wall(templatePost, templateSide, templateSideTall, wallTexture)
                : null;
    }

    /**
     * A fence-shaped block whose post/side models are plain texture-swap children of shared, hand-authored fence
     * templates (e.g. BetterNether's {@code nether_reed_fence_post}/{@code nether_reed_fence_side}, whose bespoke
     * top/lower-bar geometry every mushroom-fence child reuses with only different textures). Generates the two
     * child models ({@code {parent: <template>, textures: {particle, texture, top}}}), the standard vanilla fence
     * multipart blockstate (via {@link BlockModelGenerators#createFence}, i.e. the uv-locked post/side multipart),
     * and a {@code fence_inventory} item model from a separate inventory texture (fences ship a distinct plank
     * inventory icon rather than reusing their fence-side texture).
     *
     * @param templatePost     the shared fence-post template model
     * @param templateSide     the shared fence-side template model
     * @param sideTexture      the {@code particle}/{@code texture} texture bound into both templates
     * @param topTexture       the {@code top} texture bound into both templates
     * @param inventoryTexture the {@code texture} for the {@code fence_inventory} item model
     * @return the model trait, or {@code null} outside of datagen
     */
    public static BlockModelTrait fence(
            ResourceLocation templatePost,
            ResourceLocation templateSide,
            ResourceLocation sideTexture,
            ResourceLocation topTexture,
            ResourceLocation inventoryTexture
    ) {
        return ModCore.isDatagen()
                ? Impl.fence(templatePost, templateSide, sideTexture, topTexture, inventoryTexture)
                : null;
    }

    @Environment(EnvType.CLIENT)
    private static class Impl {
        private static BlockModelTrait fence(
                ResourceLocation templatePost,
                ResourceLocation templateSide,
                ResourceLocation sideTexture,
                ResourceLocation topTexture,
                ResourceLocation inventoryTexture
        ) {
            return ClientBlockTraits.MODEL.with((key, block, generator) -> {
                final var mapping = new TextureMapping()
                        .put(TextureSlot.PARTICLE, sideTexture)
                        .put(TextureSlot.TEXTURE, sideTexture)
                        .put(TextureSlot.TOP, topTexture);
                final TextureSlot[] slots = {TextureSlot.PARTICLE, TextureSlot.TEXTURE, TextureSlot.TOP};
                final var post = new ModelTemplate(Optional.of(templatePost), Optional.of("_post"), slots)
                        .create(block, mapping, generator.modelOutput());
                final var side = new ModelTemplate(Optional.of(templateSide), Optional.of("_side"), slots)
                        .create(block, mapping, generator.modelOutput());

                generator.acceptBlockState(BlockModelGenerators.createFence(
                        block,
                        BlockModelGenerators.plainVariant(post),
                        BlockModelGenerators.plainVariant(side)
                ));

                // A fence's inventory icon uses a distinct (plank) texture, not the fence-side texture.
                generator.delegateItemModel(block, ModelTemplates.FENCE_INVENTORY.create(
                        block, new TextureMapping().put(TextureSlot.TEXTURE, inventoryTexture),
                        generator.modelOutput()));
            });
        }

        private static BlockModelTrait stairs(
                ResourceLocation templateStraight,
                ResourceLocation templateInner,
                ResourceLocation templateOuter,
                ResourceLocation bottomTexture,
                ResourceLocation topTexture,
                ResourceLocation sideTexture
        ) {
            return ClientBlockTraits.MODEL.with((key, block, generator) -> {
                final var mapping = new TextureMapping()
                        .put(TextureSlot.BOTTOM, bottomTexture)
                        .put(TextureSlot.TOP, topTexture)
                        .put(TextureSlot.SIDE, sideTexture);
                final TextureSlot[] slots = {TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE};
                final var straight = new ModelTemplate(Optional.of(templateStraight), Optional.empty(), slots)
                        .create(block, mapping, generator.modelOutput());
                final var inner = new ModelTemplate(Optional.of(templateInner), Optional.of("_inner"), slots)
                        .create(block, mapping, generator.modelOutput());
                final var outer = new ModelTemplate(Optional.of(templateOuter), Optional.of("_outer"), slots)
                        .create(block, mapping, generator.modelOutput());

                // The standard vanilla stair rotation matrix, but WITHOUT uvlock: the lit_* templates bake
                // directional UVs (shade:false) that uv-locking would corrupt, so the hand-authored blockstate
                // omits it. Rotations themselves are identical to vanilla createStairs.
                PropertyDispatch.C3<MultiVariant, Direction, net.minecraft.world.level.block.state.properties.Half,
                        net.minecraft.world.level.block.state.properties.StairsShape> dispatch =
                        PropertyDispatch.initial(
                                BlockStateProperties.HORIZONTAL_FACING,
                                BlockStateProperties.HALF,
                                BlockStateProperties.STAIRS_SHAPE);
                for (Direction facing : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
                    final int yStraight = switch (facing) {
                        case EAST -> 0;
                        case SOUTH -> 90;
                        case WEST -> 180;
                        default -> 270; // NORTH
                    };
                    for (var half : net.minecraft.world.level.block.state.properties.Half.values()) {
                        final boolean top = half == net.minecraft.world.level.block.state.properties.Half.TOP;
                        for (var shape : net.minecraft.world.level.block.state.properties.StairsShape.values()) {
                            final ResourceLocation model = switch (shape) {
                                case STRAIGHT -> straight;
                                case INNER_LEFT, INNER_RIGHT -> inner;
                                case OUTER_LEFT, OUTER_RIGHT -> outer;
                            };
                            final boolean left = shape == net.minecraft.world.level.block.state.properties.StairsShape.OUTER_LEFT
                                    || shape == net.minecraft.world.level.block.state.properties.StairsShape.INNER_LEFT;
                            final boolean right = shape == net.minecraft.world.level.block.state.properties.StairsShape.OUTER_RIGHT
                                    || shape == net.minecraft.world.level.block.state.properties.StairsShape.INNER_RIGHT;
                            final int y = top
                                    ? (right ? (yStraight + 90) % 360 : yStraight)
                                    : (left ? (yStraight + 270) % 360 : yStraight);
                            dispatch = dispatch.select(facing, half, shape, stairVariant(model, top, y));
                        }
                    }
                }
                generator.acceptBlockState(MultiVariantGenerator.dispatch(block).with(dispatch));
                generator.delegateItemModel(block, straight);
            });
        }

        private static MultiVariant stairVariant(ResourceLocation model, boolean top, int y) {
            MultiVariant mv = BlockModelGenerators.plainVariant(model);
            if (top) {
                mv = mv.with(BlockModelGenerators.X_ROT_180);
            }
            mv = switch (y) {
                case 90 -> mv.with(BlockModelGenerators.Y_ROT_90);
                case 180 -> mv.with(BlockModelGenerators.Y_ROT_180);
                case 270 -> mv.with(BlockModelGenerators.Y_ROT_270);
                default -> mv;
            };
            return mv;
        }

        private static BlockModelTrait wall(
                ResourceLocation templatePost,
                ResourceLocation templateSide,
                ResourceLocation templateSideTall,
                ResourceLocation wallTexture
        ) {
            return ClientBlockTraits.MODEL.with((key, block, generator) -> {
                final var mapping = new TextureMapping().put(TextureSlot.WALL, wallTexture);
                final var post = new ModelTemplate(Optional.of(templatePost), Optional.of("_post"), TextureSlot.WALL)
                        .create(block, mapping, generator.modelOutput());
                final var side = new ModelTemplate(Optional.of(templateSide), Optional.of("_side"), TextureSlot.WALL)
                        .create(block, mapping, generator.modelOutput());
                final var sideTall = new ModelTemplate(Optional.of(templateSideTall), Optional.of("_side_tall"), TextureSlot.WALL)
                        .create(block, mapping, generator.modelOutput());

                generator.acceptBlockState(BlockModelGenerators.createWall(
                        block,
                        BlockModelGenerators.plainVariant(post),
                        BlockModelGenerators.plainVariant(side),
                        BlockModelGenerators.plainVariant(sideTall)
                ));

                // The wall_inventory item model (wover's createWall does this via a private createInventoryModel).
                generator.delegateItemModel(block, ModelTemplates.WALL_INVENTORY.create(
                        block, mapping, generator.modelOutput()));
            });
        }

        private static BlockModelTrait cube(ResourceLocation templateParent, boolean withParticle) {
            return ClientBlockTraits.MODEL.with((key, block, generator) -> {
                final var tex = TextureMapping.getBlockTexture(block);
                final var mapping = new TextureMapping().put(TextureSlot.TEXTURE, tex);
                final TextureSlot[] slots;
                if (withParticle) {
                    mapping.put(TextureSlot.PARTICLE, tex);
                    slots = new TextureSlot[]{TextureSlot.PARTICLE, TextureSlot.TEXTURE};
                } else {
                    slots = new TextureSlot[]{TextureSlot.TEXTURE};
                }
                final var template = new ModelTemplate(Optional.of(templateParent), Optional.empty(), slots);
                final var model = template.create(block, mapping, generator.modelOutput());

                generator.acceptBlockState(
                        BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(model))
                );
                generator.delegateItemModel(block, model);
            });
        }

        private static BlockModelTrait randomYRotation(ResourceLocation templateParent) {
            return ClientBlockTraits.MODEL.with((key, block, generator) -> {
                final var tex = TextureMapping.getBlockTexture(block);
                final var mapping = new TextureMapping().put(TextureSlot.TEXTURE, tex);
                final var template = new ModelTemplate(Optional.of(templateParent), Optional.empty(), TextureSlot.TEXTURE);
                final var model = template.create(block, mapping, generator.modelOutput());

                final Variant base = BlockModelGenerators.plainModel(model);
                final var variants = WeightedList.<Variant>builder();
                variants.add(base, 1);
                variants.add(base.with(BlockModelGenerators.Y_ROT_90), 1);
                variants.add(base.with(BlockModelGenerators.Y_ROT_180), 1);
                variants.add(base.with(BlockModelGenerators.Y_ROT_270), 1);
                generator.acceptBlockState(
                        BlockModelGenerators.createSimpleBlock(block, new MultiVariant(variants.build()))
                );

                // The inventory icon stays the hand-authored static flat item model (item/<name>), exactly as
                // wover's externalModel() delegates it - these blocks ship a dedicated item texture.
                generator.delegateItemModel(block, key.location().withPrefix("item/"));
            });
        }

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
