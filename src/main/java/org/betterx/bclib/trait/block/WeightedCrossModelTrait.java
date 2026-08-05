package org.betterx.bclib.trait.block;

import de.ambertation.wover.block.api.client.trait.BlockModelTrait;
import de.ambertation.wover.block.api.client.trait.ClientBlockTraits;
import de.ambertation.wover.block.api.model.WoverBlockModelGenerators;
import de.ambertation.wover.core.api.ModCore;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;

import com.mojang.math.Quadrant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

/**
 * A weighted, multi-variant model trait for the <em>parent-based</em> plant/grass/moss/coral/vine families
 * BetterNether and BetterEnd ship - the sibling of {@link WeightedPillarModelTrait} for cross/crop/plane-shaped
 * plants instead of pillars.
 * <p>
 * Every one of these plant blocks used to be a hand-authored blockstate whose variants are all
 * <em>parent-based</em> child models ({@code {parent: block/cross | betternether:block/crop_block | ..., textures:
 * {cross|texture: <plant texture>}}}) combined into a (usually weighted) variant list, dispatched over a single
 * block property. Because those variant models only reference a shared plant shape plus a texture, they carry no
 * hand-modelled geometry and must be generated in code. This trait owns exactly that:
 * <ul>
 *     <li>it generates each variant's child model ({@code {parent, textures}}) via a {@link ModelTemplate},</li>
 *     <li>it generates the blockstate as the recovered (weighted) variant list, dispatched over the block's
 *     property, and</li>
 *     <li>it generates the flat/delegated item model the hand-authored block shipped.</li>
 * </ul>
 * The shared shape parents ({@code block/cross}, {@code betternether:block/cross_inverted},
 * {@code betterend:block/crop_block_inverted}, {@code .../cross_no_distortion}, {@code .../plane_bottom}, ...) stay
 * hand-authored - they are the actual (bespoke, {@code elements}-carrying or ambient-occlusion-tuned) shapes every
 * plant reuses. Only the per-plant child models, the blockstate and the item model are generated here.
 * <p>
 * The generated child-model file names are not required to match the deleted hand-authored ones
 * ({@code <plant>_1}, {@code <plant>_2}, ...); the blockstate references whatever names this trait emits, and the
 * <em>effective</em> model (root parent + merged textures) is identical, which is what the render pipeline (and the
 * semantic-equivalence check) resolves.
 * <p>
 * Like wover's {@code ModelTraitLibrary}, every public factory returns {@code null} outside a datagen environment;
 * the client-only vanilla datagen types are only touched from {@link Impl}, which is loaded solely when
 * {@link ModCore#isDatagen()} is {@code true} (see {@link PathBlockTrait} for the same guard). The public surface
 * therefore stays free of client-only types ({@link Layer} carries only {@link Identifier}s plus a
 * server-safe {@link Slot} enum).
 */
public class WeightedCrossModelTrait {
    /**
     * Which texture slot a shape parent binds its plant texture to: cross-shaped parents ({@code block/cross} and
     * its inverted/no-distortion siblings) use {@code cross}; crop/plane-shaped parents ({@code crop_block},
     * {@code plane_bottom}, ...) use {@code texture}.
     */
    public enum Slot {
        /** The {@code cross} texture slot used by {@code block/cross}-derived shapes. */
        CROSS,
        /** The {@code texture} slot used by {@code crop_block}/{@code plane_bottom}-derived shapes. */
        TEXTURE
    }

    /**
     * One weighted variant of a plant blockstate: a parent-based child model
     * ({@code {parent: <parent>, textures: {<slot>: <texture>}}}) plus its weight in the variant list.
     *
     * @param parent  the shared plant shape this child model parents (e.g. {@code minecraft:block/cross} or
     *                {@code betternether:block/cross_inverted})
     * @param slot    which texture slot the {@code texture} is bound to (see {@link Slot})
     * @param texture the plant texture bound into the shape
     * @param weight  the variant's weight (1 for the common equally-weighted lists)
     * @param xRot    the blockstate variant's {@code x} rotation in degrees (0/90/180/270); {@code 0} for none
     * @param yRot    the blockstate variant's {@code y} rotation in degrees (0/90/180/270); {@code 0} for none
     */
    public record Layer(Identifier parent, Slot slot, Identifier texture, int weight, int xRot, int yRot) {
        /**
         * A copy of this layer carrying the given blockstate-variant rotation (degrees, each 0/90/180/270) - some
         * seed/crystal/lotus states place the same shared shape at {@code x}/{@code y} rotations.
         *
         * @param x the {@code x} rotation in degrees
         * @param y the {@code y} rotation in degrees
         * @return the rotated layer
         */
        public Layer rotated(int x, int y) {
            return new Layer(parent, slot, texture, weight, x, y);
        }

        /**
         * A copy of this layer with the given weight (for weighted variant lists).
         *
         * @param w the new weight
         * @return the re-weighted layer
         */
        public Layer weighted(int w) {
            return new Layer(parent, slot, texture, w, xRot, yRot);
        }
    }

    /**
     * One case of a property-dispatched plant blockstate: the weighted variant list to use when the dispatched
     * property equals {@code value}. Used by {@link #propertyDispatch(Property, List, Item)}.
     *
     * @param value    the property value this case selects on (an enum constant, {@link Integer} age, ...)
     * @param variants the weighted variant list for that value
     * @param <T>      the property's value type
     */
    public record Case<T extends Comparable<T>>(T value, List<Layer> variants) {
        /** A case mapping {@code value} to a single (weight-1) variant. */
        public static <T extends Comparable<T>> Case<T> of(T value, Layer variant) {
            return new Case<>(value, List.of(variant));
        }

        /** A case mapping {@code value} to the given weighted variant list. */
        public static <T extends Comparable<T>> Case<T> of(T value, List<Layer> variants) {
            return new Case<>(value, variants);
        }
    }

    /**
     * One case of a two-property-dispatched plant blockstate (e.g. {@code facing} x {@code shape} for the
     * end-lotus leaf). Used by {@link #property2Dispatch(Property, Property, List, Item)}.
     *
     * @param a        the first property's value
     * @param b        the second property's value
     * @param variants the weighted variant list for that {@code (a, b)} pair
     * @param <A>      the first property's value type
     * @param <B>      the second property's value type
     */
    public record Case2<A extends Comparable<A>, B extends Comparable<B>>(A a, B b, List<Layer> variants) {
        /** A case mapping {@code (a, b)} to a single (weight-1) variant. */
        public static <A extends Comparable<A>, B extends Comparable<B>> Case2<A, B> of(A a, B b, Layer variant) {
            return new Case2<>(a, b, List.of(variant));
        }

        /** A case mapping {@code (a, b)} to the given weighted variant list. */
        public static <A extends Comparable<A>, B extends Comparable<B>> Case2<A, B> of(A a, B b, List<Layer> variants) {
            return new Case2<>(a, b, variants);
        }
    }

    /**
     * How a plant block's inventory item model is generated: either a flat {@code item/generated} icon from an
     * explicit texture, or an item model delegated to the block's own texture.
     */
    public static final class Item {
        private final boolean delegate;
        @Nullable
        private final Identifier flatTexture;

        private Item(boolean delegate, @Nullable Identifier flatTexture) {
            this.delegate = delegate;
            this.flatTexture = flatTexture;
        }

        /**
         * A flat {@code item/generated} icon whose {@code layer0} is {@code texture} (or the block's own texture
         * when {@code null}) - matches the hand-authored {@code models/item/<name>.json} these plants shipped.
         *
         * @param texture the {@code layer0} texture, or {@code null} for the block's own texture
         * @return the item spec
         */
        public static Item flat(@Nullable Identifier texture) {
            return new Item(false, texture);
        }

        /**
         * An item model delegated to the block's own texture (wover's {@code externalModelDelegatedItem()}
         * behaviour) - for plants that ship no dedicated {@code item/<name>} model.
         *
         * @return the item spec
         */
        public static Item delegated() {
            return new Item(true, null);
        }
    }

    /**
     * A single-state plant (empty variant key): one or more weighted variants, no property dispatch - e.g.
     * BetterNether's {@code eye_vine} (one {@code block/cross} variant).
     *
     * @param variants the weighted variant list
     * @param item     how to generate the item model
     * @return the model trait, or {@code null} outside of datagen
     */
    public static BlockModelTrait simple(List<Layer> variants, Item item) {
        return ModCore.isDatagen() ? Impl.simple(variants, item) : null;
    }

    /**
     * A plant dispatched over one boolean property (e.g. {@link org.betterx.bclib.blocks.BaseSimpleVineBlock#BOTTOM}
     * for the vines): a weighted variant list for each of the property's two values - e.g. BetterNether's
     * {@code golden_vine}/{@code black_vine}/{@code blooming_vine}.
     *
     * @param property  the boolean property to dispatch over
     * @param whenFalse the weighted variant list when {@code property == false}
     * @param whenTrue  the weighted variant list when {@code property == true}
     * @param item      how to generate the item model
     * @return the model trait, or {@code null} outside of datagen
     */
    public static BlockModelTrait booleanDispatch(
            BooleanProperty property,
            List<Layer> whenFalse,
            List<Layer> whenTrue,
            Item item
    ) {
        return ModCore.isDatagen() ? Impl.booleanDispatch(property, whenFalse, whenTrue, item) : null;
    }

    /**
     * A {@code block/cross} variant bound through the {@code cross} slot.
     *
     * @param texture the plant texture
     * @return the layer (weight 1)
     */
    public static Layer cross(Identifier texture) {
        return new Layer(Identifier.withDefaultNamespace("block/cross"), Slot.CROSS, texture, 1, 0, 0);
    }

    /**
     * A cross-derived variant with a custom parent (e.g. {@code betternether:block/cross_inverted}) bound through
     * the {@code cross} slot.
     *
     * @param parent  the shared cross-shaped parent
     * @param texture the plant texture
     * @return the layer (weight 1)
     */
    public static Layer crossParent(Identifier parent, Identifier texture) {
        return new Layer(parent, Slot.CROSS, texture, 1, 0, 0);
    }

    /**
     * A crop/plane-derived variant (e.g. {@code betternether:block/crop_block}) bound through the {@code texture}
     * slot.
     *
     * @param parent  the shared crop/plane-shaped parent
     * @param texture the plant texture
     * @return the layer (weight 1)
     */
    public static Layer cropParent(Identifier parent, Identifier texture) {
        return new Layer(parent, Slot.TEXTURE, texture, 1, 0, 0);
    }

    /**
     * A plant dispatched over a single arbitrary property (an enum like {@code TripleShape}/{@code facing}, or an
     * {@link Integer} {@code age}): a weighted variant list - each variant optionally rotated - for each of the
     * property's listed values. Every value the property can take MUST have a case, or vanilla's
     * {@link MultiVariantGenerator} rejects the incomplete dispatch.
     *
     * @param property the property to dispatch over
     * @param cases    one {@link Case} per property value
     * @param item     how to generate the item model
     * @param <T>      the property's value type
     * @return the model trait, or {@code null} outside of datagen
     */
    public static <T extends Comparable<T>> BlockModelTrait propertyDispatch(
            Property<T> property,
            List<Case<T>> cases,
            Item item
    ) {
        return ModCore.isDatagen() ? Impl.propertyDispatch(property, cases, item) : null;
    }

    /**
     * A plant dispatched over two properties (e.g. {@code facing} x {@code shape} for the end-lotus leaf): a
     * weighted, optionally-rotated variant list for each listed {@code (a, b)} pair. Every reachable combination
     * MUST have a case.
     *
     * @param a     the first property
     * @param b     the second property
     * @param cases one {@link Case2} per {@code (a, b)} pair
     * @param item  how to generate the item model
     * @param <A>  the first property's value type
     * @param <B>  the second property's value type
     * @return the model trait, or {@code null} outside of datagen
     */
    public static <A extends Comparable<A>, B extends Comparable<B>> BlockModelTrait property2Dispatch(
            Property<A> a,
            Property<B> b,
            List<Case2<A, B>> cases,
            Item item
    ) {
        return ModCore.isDatagen() ? Impl.property2Dispatch(a, b, cases, item) : null;
    }

    @Environment(EnvType.CLIENT)
    private static class Impl {
        private static TextureSlot slot(Slot slot) {
            return slot == Slot.CROSS ? TextureSlot.CROSS : TextureSlot.TEXTURE;
        }

        private static Identifier emitModel(
                Block block,
                WoverBlockModelGenerators generator,
                Layer layer,
                String suffix
        ) {
            final TextureSlot texSlot = slot(layer.slot());
            final var template = new ModelTemplate(Optional.of(layer.parent()), Optional.of(suffix), texSlot);
            return template.create(
                    block,
                    new TextureMapping().put(texSlot, new Material(layer.texture())),
                    generator.modelOutput()
            );
        }

        private static Quadrant quadrant(int degrees) {
            return switch (((degrees % 360) + 360) % 360) {
                case 90 -> Quadrant.R90;
                case 180 -> Quadrant.R180;
                case 270 -> Quadrant.R270;
                default -> Quadrant.R0;
            };
        }

        private static Variant variant(Identifier model, Layer layer) {
            Variant v = new Variant(model);
            if (layer.xRot() != 0) {
                v = v.withXRot(quadrant(layer.xRot()));
            }
            if (layer.yRot() != 0) {
                v = v.withYRot(quadrant(layer.yRot()));
            }
            return v;
        }

        private static MultiVariant variants(
                Block block,
                WoverBlockModelGenerators generator,
                List<Layer> layers,
                String statePrefix
        ) {
            if (layers.size() == 1 && layers.get(0).weight() == 1
                    && layers.get(0).xRot() == 0 && layers.get(0).yRot() == 0) {
                return BlockModelGenerators.plainVariant(emitModel(block, generator, layers.get(0), statePrefix));
            }
            final var weighted = WeightedList.<Variant>builder();
            for (int i = 0; i < layers.size(); i++) {
                final Layer layer = layers.get(i);
                final Identifier loc = emitModel(
                        block, generator, layer, layers.size() == 1 ? statePrefix : statePrefix + "_" + i);
                weighted.add(variant(loc, layer), layer.weight());
            }
            return new MultiVariant(weighted.build());
        }

        private static void applyItem(Block block, WoverBlockModelGenerators generator, Item item) {
            // Matches wover's externalModel*/ModelTraitLibrary guard: item-less blocks (e.g. registerBlockNI)
            // have Items.AIR as their item and must not register an item model (which would target
            // minecraft:items/air).
            if (block.asItem() == Items.AIR) {
                return;
            }
            if (item.delegate) {
                generator.delegateItemModel(block);
            } else {
                generator.createFlatItem(block, item.flatTexture);
            }
        }

        private static BlockModelTrait simple(List<Layer> variants, Item item) {
            return ClientBlockTraits.MODEL.with((key, block, generator) -> {
                final MultiVariant mv = variants(block, generator, variants, "");
                generator.acceptBlockState(BlockModelGenerators.createSimpleBlock(block, mv));
                applyItem(block, generator, item);
            });
        }

        private static BlockModelTrait booleanDispatch(
                BooleanProperty property,
                List<Layer> whenFalse,
                List<Layer> whenTrue,
                Item item
        ) {
            return ClientBlockTraits.MODEL.with((key, block, generator) -> {
                final MultiVariant falseVariant = variants(block, generator, whenFalse, "_false");
                final MultiVariant trueVariant = variants(block, generator, whenTrue, "_true");
                generator.acceptBlockState(
                        MultiVariantGenerator.dispatch(block).with(
                                PropertyDispatch.initial(property)
                                                .select(false, falseVariant)
                                                .select(true, trueVariant)
                        )
                );
                applyItem(block, generator, item);
            });
        }

        private static <T extends Comparable<T>> BlockModelTrait propertyDispatch(
                Property<T> property,
                List<Case<T>> cases,
                Item item
        ) {
            return ClientBlockTraits.MODEL.with((key, block, generator) -> {
                PropertyDispatch.C1<MultiVariant, T> dispatch = null;
                for (Case<T> c : cases) {
                    final MultiVariant mv = variants(
                            block, generator, c.variants(), "_" + property.getName(c.value()));
                    dispatch = dispatch == null
                            ? PropertyDispatch.initial(property).select(c.value(), mv)
                            : dispatch.select(c.value(), mv);
                }
                generator.acceptBlockState(MultiVariantGenerator.dispatch(block).with(dispatch));
                applyItem(block, generator, item);
            });
        }

        private static <A extends Comparable<A>, B extends Comparable<B>> BlockModelTrait property2Dispatch(
                Property<A> a,
                Property<B> b,
                List<Case2<A, B>> cases,
                Item item
        ) {
            return ClientBlockTraits.MODEL.with((key, block, generator) -> {
                PropertyDispatch.C2<MultiVariant, A, B> dispatch = null;
                for (Case2<A, B> c : cases) {
                    final MultiVariant mv = variants(
                            block, generator, c.variants(),
                            "_" + a.getName(c.a()) + "_" + b.getName(c.b()));
                    dispatch = dispatch == null
                            ? PropertyDispatch.initial(a, b).select(c.a(), c.b(), mv)
                            : dispatch.select(c.a(), c.b(), mv);
                }
                generator.acceptBlockState(MultiVariantGenerator.dispatch(block).with(dispatch));
                applyItem(block, generator, item);
            });
        }
    }
}
