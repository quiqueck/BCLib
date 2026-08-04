package org.betterx.bclib.trait.block;

import de.ambertation.wover.block.api.client.trait.BlockModelTrait;
import de.ambertation.wover.block.api.client.trait.ClientBlockTraits;
import de.ambertation.wover.block.api.model.WoverBlockModelGenerators;
import de.ambertation.wover.core.api.ModCore;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;

import com.google.gson.JsonObject;
import com.mojang.math.Quadrant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.jetbrains.annotations.Nullable;

/**
 * A weighted, multi-variant model trait for the <em>template-child</em> plant/leaves/moss/flower/trunk families
 * BetterNether and BetterEnd ship - the sibling of {@link WeightedCrossModelTrait} for families whose variant
 * models are <em>texture-swap children of a single hand-authored geometry template</em> instead of children of a
 * shared vanilla/plane shape.
 * <p>
 * Where {@link WeightedCrossModelTrait} binds one texture into a shared shape ({@code block/cross},
 * {@code crop_block}, ...) through a single slot, these families keep <em>one</em> hand-authored geometry model as
 * the template (it carries the {@code elements} and multiple {@code #slot} texture placeholders, e.g.
 * {@code #rune_1}/{@code #rune_2}/{@code #rune_3} or {@code #texture}/{@code #leaf}), and every other member is the
 * <em>same mesh</em> with a different set of textures. The blockstate is a bespoke weighted multi-variant list,
 * frequently with per-entry {@code x}/{@code y} rotation and {@code uvlock}. This trait owns exactly that:
 * <ul>
 *     <li>it generates each <em>distinct</em> child model once ({@code {parent: <template>, textures: {slot:
 *     texture, ...}}}), de-duplicating layers that resolve to the same {@code (parent, textures)} pair,</li>
 *     <li>it generates the blockstate as the recovered (weighted) variant list - each variant optionally rotated
 *     and uv-locked - dispatched over the block's property, referencing the kept template directly (via
 *     {@link #model(ResourceLocation)}) or a generated child (via {@link #child(ResourceLocation, Map)}), and</li>
 *     <li>it generates the flat/delegated item model the hand-authored block shipped.</li>
 * </ul>
 * The one hand-authored geometry template per family stays committed - it is the actual bespoke, {@code elements}
 * carrying shape every member reuses. Only the sibling child models, the blockstate and the item model are
 * generated here.
 * <p>
 * Because the template's {@code #slot} placeholders are arbitrary names (not vanilla {@link
 * net.minecraft.client.data.models.model.TextureSlot} constants, whose factory is not public), each child model's
 * JSON ({@code {parent, textures}}) is emitted directly rather than through a {@code ModelTemplate}. The generated
 * child-model file names ({@code <block>_t0}, {@code <block>_t1}, ...) are not required to match the deleted
 * hand-authored ones; the blockstate references whatever names this trait emits, and the <em>effective</em> model
 * (template geometry + merged textures) is identical, which is what the render pipeline (and the
 * semantic-equivalence check) resolves.
 * <p>
 * Like wover's {@code ModelTraitLibrary}, every public factory returns {@code null} outside a datagen environment;
 * the client-only vanilla datagen types are only touched from {@link Impl}, which is loaded solely when
 * {@link ModCore#isDatagen()} is {@code true}. The public surface therefore stays free of client-only types
 * ({@link Layer} carries only {@link ResourceLocation}s and a texture {@link Map}).
 */
public class WeightedTemplateModelTrait {
    /**
     * One weighted variant of a template-child blockstate: either a generated child model (a template
     * {@code parent} plus a slot-name&rarr;texture map) or a direct reference to an existing (kept template)
     * model, plus the variant's weight and blockstate {@code x}/{@code y} rotation and {@code uvlock} flag.
     *
     * @param parent        the shared geometry template this child model parents; {@code null} when {@code
     *                      explicitModel} is set (a direct reference to the kept template)
     * @param textures      the slot-name&rarr;texture map bound into the template; empty when {@code explicitModel}
     *                      is set
     * @param explicitModel a direct reference to an existing model (the kept template, or a template's own model);
     *                      {@code null} when a child is generated from {@code parent}/{@code textures}
     * @param weight        the variant's weight (1 for the common equally-weighted lists)
     * @param xRot          the blockstate variant's {@code x} rotation in degrees (0/90/180/270); {@code 0} for none
     * @param yRot          the blockstate variant's {@code y} rotation in degrees (0/90/180/270); {@code 0} for none
     * @param uvlock        whether the blockstate variant sets {@code uvlock: true}
     */
    public record Layer(
            @Nullable ResourceLocation parent,
            Map<String, ResourceLocation> textures,
            @Nullable ResourceLocation explicitModel,
            int weight,
            int xRot,
            int yRot,
            boolean uvlock
    ) {
        /** A copy of this layer carrying the given blockstate-variant rotation (degrees, each 0/90/180/270). */
        public Layer rotated(int x, int y) {
            return new Layer(parent, textures, explicitModel, weight, x, y, uvlock);
        }

        /** A copy of this layer with the given weight (for weighted variant lists). */
        public Layer weighted(int w) {
            return new Layer(parent, textures, explicitModel, w, xRot, yRot, uvlock);
        }

        /** A copy of this layer with {@code uvlock: true} on its blockstate variant. */
        public Layer uvLocked() {
            return new Layer(parent, textures, explicitModel, weight, xRot, yRot, true);
        }
    }

    /**
     * A generated child model of a shared geometry {@code template}, binding {@code textures} (slot name &rarr;
     * texture) into its {@code #slot} placeholders. Distinct {@code (template, textures)} pairs generate one child
     * model each; layers that resolve to the same pair reuse it.
     *
     * @param template the shared geometry template model to parent the child from
     * @param textures the slot-name&rarr;texture map to bind into the template
     * @return the layer (weight 1, no rotation, no uvlock)
     */
    public static Layer child(ResourceLocation template, Map<String, ResourceLocation> textures) {
        return new Layer(template, Map.copyOf(textures), null, 1, 0, 0, false);
    }

    /**
     * A direct reference to an existing model - the kept, hand-authored template's own model - used as a
     * blockstate variant without generating a child.
     *
     * @param existingModel the model to reference directly
     * @return the layer (weight 1, no rotation, no uvlock)
     */
    public static Layer model(ResourceLocation existingModel) {
        return new Layer(null, Map.of(), existingModel, 1, 0, 0, false);
    }

    /**
     * One case of a property-dispatched template-child blockstate: the weighted variant list to use when the
     * dispatched property equals {@code value}.
     *
     * @param value    the property value this case selects on
     * @param variants the weighted variant list for that value
     * @param <T>      the property's value type
     */
    public record Case<T extends Comparable<T>>(T value, List<Layer> variants) {
        /** A case mapping {@code value} to the given weighted variant list. */
        public static <T extends Comparable<T>> Case<T> of(T value, List<Layer> variants) {
            return new Case<>(value, variants);
        }
    }

    /**
     * How the block's inventory item model is generated: a flat {@code item/generated} icon, an item delegated to
     * the block's own texture, an item delegated to an explicit model (the kept template), or none.
     */
    public static final class Item {
        private enum Kind {FLAT, DELEGATE_BLOCK, DELEGATE_MODEL, NONE}

        private final Kind kind;
        @Nullable
        private final ResourceLocation ref;

        private Item(Kind kind, @Nullable ResourceLocation ref) {
            this.kind = kind;
            this.ref = ref;
        }

        /** A flat {@code item/generated} icon whose {@code layer0} is {@code texture} (block's own if {@code null}). */
        public static Item flat(@Nullable ResourceLocation texture) {
            return new Item(Kind.FLAT, texture);
        }

        /** An item model delegated to the block's own texture. */
        public static Item delegated() {
            return new Item(Kind.DELEGATE_BLOCK, null);
        }

        /** An item model delegated to an explicit model - e.g. the kept template ({@code block/<name>_1}). */
        public static Item delegatedTo(ResourceLocation model) {
            return new Item(Kind.DELEGATE_MODEL, model);
        }

        /** No item model (e.g. block-only families). */
        public static Item none() {
            return new Item(Kind.NONE, null);
        }
    }

    /**
     * A single-state template-child block (empty variant key): one or more weighted variants, no property
     * dispatch.
     *
     * @param variants the weighted variant list
     * @param item     how to generate the item model
     * @return the model trait, or {@code null} outside of datagen
     */
    public static BlockModelTrait simple(List<Layer> variants, Item item) {
        return ModCore.isDatagen() ? Impl.simple(variants, item) : null;
    }

    /**
     * A template-child block dispatched over one boolean property: a weighted variant list for each of the
     * property's two values.
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
     * A template-child block dispatched over a single arbitrary property. Every value the property can take MUST
     * have a case.
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

    @Environment(EnvType.CLIENT)
    private static class Impl {
        /**
         * Resolves the (distinct) child models a variant list needs, de-duplicating by {@code (parent, textures)}.
         * Generated child models are named {@code <block>_t0}, {@code <block>_t1}, ... in first-seen order across
         * every variant list of the block (the counter and cache are shared for the whole blockstate).
         */
        private static final class ChildModels {
            private final Block block;
            private final WoverBlockModelGenerators generator;
            private final ResourceLocation blockModel;
            private final Map<String, ResourceLocation> cache = new HashMap<>();
            private int counter = 0;

            ChildModels(Block block, WoverBlockModelGenerators generator) {
                this.block = block;
                this.generator = generator;
                // <ns>:block/<name> - the block's own model location.
                this.blockModel = TextureMapping.getBlockTexture(block);
            }

            ResourceLocation resolve(Layer layer) {
                if (layer.explicitModel() != null) {
                    return layer.explicitModel();
                }
                final String key = key(layer.parent(), layer.textures());
                final ResourceLocation cached = cache.get(key);
                if (cached != null) {
                    return cached;
                }
                final ResourceLocation id = blockModel.withSuffix("_t" + counter++);
                emit(id, layer.parent(), layer.textures());
                cache.put(key, id);
                return id;
            }

            private void emit(ResourceLocation id, ResourceLocation parent, Map<String, ResourceLocation> textures) {
                // Emit {parent, textures} directly: the template's #slot placeholders are arbitrary names, not
                // public TextureSlot constants, so ModelTemplate/TextureMapping can't express them.
                final JsonObject json = new JsonObject();
                json.addProperty("parent", parent.toString());
                final JsonObject tex = new JsonObject();
                // Sorted for a stable, deterministic child-model JSON.
                for (Map.Entry<String, ResourceLocation> e : new TreeMap<>(textures).entrySet()) {
                    tex.addProperty(e.getKey(), e.getValue().toString());
                }
                json.add("textures", tex);
                final ModelInstance model = () -> json;
                generator.modelOutput().accept(id, model);
            }

            private static String key(ResourceLocation parent, Map<String, ResourceLocation> textures) {
                final StringBuilder sb = new StringBuilder(parent.toString()).append('|');
                for (Map.Entry<String, ResourceLocation> e : new TreeMap<>(textures).entrySet()) {
                    sb.append(e.getKey()).append('=').append(e.getValue()).append(';');
                }
                return sb.toString();
            }
        }

        private static Quadrant quadrant(int degrees) {
            return switch (((degrees % 360) + 360) % 360) {
                case 90 -> Quadrant.R90;
                case 180 -> Quadrant.R180;
                case 270 -> Quadrant.R270;
                default -> Quadrant.R0;
            };
        }

        private static Variant variant(ResourceLocation model, Layer layer) {
            Variant v = new Variant(model);
            if (layer.xRot() != 0) {
                v = v.withXRot(quadrant(layer.xRot()));
            }
            if (layer.yRot() != 0) {
                v = v.withYRot(quadrant(layer.yRot()));
            }
            if (layer.uvlock()) {
                v = v.withUvLock(true);
            }
            return v;
        }

        private static MultiVariant variants(ChildModels models, List<Layer> layers) {
            if (layers.size() == 1) {
                final Layer only = layers.get(0);
                if (only.weight() == 1 && only.xRot() == 0 && only.yRot() == 0 && !only.uvlock()) {
                    return BlockModelGenerators.plainVariant(models.resolve(only));
                }
            }
            final var weighted = WeightedList.<Variant>builder();
            for (Layer layer : layers) {
                weighted.add(variant(models.resolve(layer), layer), layer.weight());
            }
            return new MultiVariant(weighted.build());
        }

        private static void applyItem(Block block, WoverBlockModelGenerators generator, Item item) {
            if (block.asItem() == Items.AIR) {
                return;
            }
            switch (item.kind) {
                case FLAT -> generator.createFlatItem(block, item.ref);
                case DELEGATE_BLOCK -> generator.delegateItemModel(block);
                case DELEGATE_MODEL -> generator.delegateItemModel(block, item.ref);
                case NONE -> { /* no item model */ }
            }
        }

        private static BlockModelTrait simple(List<Layer> variants, Item item) {
            return ClientBlockTraits.MODEL.with((key, block, generator) -> {
                final ChildModels models = new ChildModels(block, generator);
                final MultiVariant mv = variants(models, variants);
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
                final ChildModels models = new ChildModels(block, generator);
                final MultiVariant falseVariant = variants(models, whenFalse);
                final MultiVariant trueVariant = variants(models, whenTrue);
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
                final ChildModels models = new ChildModels(block, generator);
                final List<MultiVariant> built = new ArrayList<>();
                for (Case<T> c : cases) {
                    built.add(variants(models, c.variants()));
                }
                PropertyDispatch.C1<MultiVariant, T> dispatch = null;
                for (int i = 0; i < cases.size(); i++) {
                    dispatch = dispatch == null
                            ? PropertyDispatch.initial(property).select(cases.get(i).value(), built.get(i))
                            : dispatch.select(cases.get(i).value(), built.get(i));
                }
                generator.acceptBlockState(MultiVariantGenerator.dispatch(block).with(dispatch));
                applyItem(block, generator, item);
            });
        }
    }
}
