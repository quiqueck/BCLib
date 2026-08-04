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
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import de.ambertation.wover.block.api.BlockProperties.TripleShape;

import com.mojang.math.Quadrant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * The bottom/middle/top shape-blend pillar look BetterNether ships for {@code rubeus_log}/{@code rubeus_bark}
 * (and their kin): a {@link RotatedPillarBlock#AXIS axis}-rotated pillar whose visual is dispatched over a
 * {@link TripleShape} property, where
 * <ul>
 *     <li>{@code bottom} is the un-stripped log/bark full cube,</li>
 *     <li>{@code top} is the stripped full cube, and</li>
 *     <li>{@code middle} is a per-face "blend" cube whose four sides use a transition texture, its top face the
 *     stripped end and its bottom face the un-stripped end - i.e. a {@code cube_bottom_top}.</li>
 * </ul>
 * <p>
 * Every per-shape model is a plain vanilla-template child ({@code cube_column} / {@code cube_all} /
 * {@code cube_bottom_top}) with concrete face textures - no bespoke {@code elements} - so it is generated rather
 * than hand-authored. This trait owns the three shape models, the {@code shape}&times;{@code axis} blockstate
 * (the standard vanilla pillar rotation: {@code y}&rarr;none, {@code z}&rarr;{@code x=90}, {@code x}&rarr;{@code
 * x=90,y=90}) and the item model (delegated to the {@code bottom} shape model, which is the block's own model
 * location, matching the hand-authored {@code item/<name>} &rarr; {@code block/<name>} indirection).
 * <p>
 * Like wover's {@code ModelTraitLibrary}, every public factory returns {@code null} outside a datagen
 * environment; the client-only vanilla datagen types are only touched from {@link Impl}, which is loaded solely
 * when {@link ModCore#isDatagen()} is {@code true}.
 */
public class TripleShapePillarModelTrait {
    /**
     * A {@code log}-style shape-blend pillar: {@code bottom}/{@code top} are {@code cube_column}s (distinct
     * side/end textures), {@code middle} is a {@code cube_bottom_top} transition cube.
     *
     * @param shape         the {@link TripleShape} property to dispatch over ({@code rubeus_log}'s {@code SHAPE})
     * @param bottomSide    the un-stripped side texture (bottom shape's four sides + particle)
     * @param bottomEnd     the un-stripped end texture (bottom shape's top/bottom)
     * @param blendSide     the transition side texture (middle shape's four sides + particle)
     * @param blendTop      the middle shape's top face (usually the stripped end)
     * @param blendBottom   the middle shape's bottom face (usually the un-stripped end)
     * @param topSide       the stripped side texture (top shape's four sides + particle)
     * @param topEnd        the stripped end texture (top shape's top/bottom)
     * @return the model trait, or {@code null} outside of datagen
     */
    public static BlockModelTrait log(
            EnumProperty<TripleShape> shape,
            ResourceLocation bottomSide, ResourceLocation bottomEnd,
            ResourceLocation blendSide, ResourceLocation blendTop, ResourceLocation blendBottom,
            ResourceLocation topSide, ResourceLocation topEnd
    ) {
        return ModCore.isDatagen()
                ? Impl.build(shape, false, bottomSide, bottomEnd, blendSide, blendTop, blendBottom, topSide, topEnd)
                : null;
    }

    /**
     * A {@code bark}-style shape-blend pillar: {@code bottom}/{@code top} are {@code cube_all}s (single texture on
     * every face), {@code middle} is a {@code cube_bottom_top} transition cube.
     *
     * @param shape       the {@link TripleShape} property to dispatch over ({@code rubeus_bark}'s {@code SHAPE})
     * @param bottomAll   the un-stripped texture (bottom shape - all faces + particle)
     * @param blendSide   the transition side texture (middle shape's four sides + particle)
     * @param blendTop    the middle shape's top face (usually the stripped side)
     * @param blendBottom the middle shape's bottom face (usually the un-stripped side)
     * @param topAll      the stripped texture (top shape - all faces + particle)
     * @return the model trait, or {@code null} outside of datagen
     */
    public static BlockModelTrait bark(
            EnumProperty<TripleShape> shape,
            ResourceLocation bottomAll,
            ResourceLocation blendSide, ResourceLocation blendTop, ResourceLocation blendBottom,
            ResourceLocation topAll
    ) {
        return ModCore.isDatagen()
                ? Impl.build(shape, true, bottomAll, bottomAll, blendSide, blendTop, blendBottom, topAll, topAll)
                : null;
    }

    @Environment(EnvType.CLIENT)
    private static class Impl {
        private static Quadrant quadrant(int degrees) {
            return switch (((degrees % 360) + 360) % 360) {
                case 90 -> Quadrant.R90;
                case 180 -> Quadrant.R180;
                case 270 -> Quadrant.R270;
                default -> Quadrant.R0;
            };
        }

        /** A single-model variant, optionally rotated by the standard axis-aligned pillar rotation. */
        private static MultiVariant rotated(ResourceLocation model, int xRot, int yRot) {
            if (xRot == 0 && yRot == 0) {
                return BlockModelGenerators.plainVariant(model);
            }
            Variant v = new Variant(model);
            if (xRot != 0) {
                v = v.withXRot(quadrant(xRot));
            }
            if (yRot != 0) {
                v = v.withYRot(quadrant(yRot));
            }
            return new MultiVariant(WeightedList.<Variant>builder().add(v, 1).build());
        }

        private static BlockModelTrait build(
                EnumProperty<TripleShape> shape,
                boolean bark,
                ResourceLocation bottomSide, ResourceLocation bottomEnd,
                ResourceLocation blendSide, ResourceLocation blendTop, ResourceLocation blendBottom,
                ResourceLocation topSide, ResourceLocation topEnd
        ) {
            return ClientBlockTraits.MODEL.with((key, block, generator) -> {
                final var out = generator.modelOutput();

                // bottom shape == the block's own model location (so the item can delegate to it, matching the
                // hand-authored item/<name> -> block/<name> indirection).
                final ResourceLocation bottomModel;
                final ResourceLocation topModel;
                if (bark) {
                    bottomModel = ModelTemplates.CUBE_ALL.create(
                            block, new TextureMapping().put(TextureSlot.ALL, bottomSide), out);
                    topModel = ModelTemplates.CUBE_ALL.createWithSuffix(
                            block, "_top", new TextureMapping().put(TextureSlot.ALL, topSide), out);
                } else {
                    bottomModel = ModelTemplates.CUBE_COLUMN.create(
                            block,
                            new TextureMapping().put(TextureSlot.SIDE, bottomSide).put(TextureSlot.END, bottomEnd),
                            out);
                    topModel = ModelTemplates.CUBE_COLUMN.createWithSuffix(
                            block, "_top",
                            new TextureMapping().put(TextureSlot.SIDE, topSide).put(TextureSlot.END, topEnd),
                            out);
                }

                final ModelTemplate blendTemplate = ModelTemplates.CUBE_BOTTOM_TOP;
                final ResourceLocation blendModel = blendTemplate.createWithSuffix(
                        block, "_blend",
                        new TextureMapping()
                                .put(TextureSlot.SIDE, blendSide)
                                .put(TextureSlot.TOP, blendTop)
                                .put(TextureSlot.BOTTOM, blendBottom),
                        out);

                // shape x axis dispatch, standard vanilla pillar rotation per axis.
                final EnumProperty<Direction.Axis> axis = RotatedPillarBlock.AXIS;
                PropertyDispatch.C2<MultiVariant, TripleShape, Direction.Axis> dispatch =
                        PropertyDispatch.initial(shape, axis)
                                        .select(TripleShape.BOTTOM, Direction.Axis.Y, rotated(bottomModel, 0, 0))
                                        .select(TripleShape.BOTTOM, Direction.Axis.Z, rotated(bottomModel, 90, 0))
                                        .select(TripleShape.BOTTOM, Direction.Axis.X, rotated(bottomModel, 90, 90))
                                        .select(TripleShape.MIDDLE, Direction.Axis.Y, rotated(blendModel, 0, 0))
                                        .select(TripleShape.MIDDLE, Direction.Axis.Z, rotated(blendModel, 90, 0))
                                        .select(TripleShape.MIDDLE, Direction.Axis.X, rotated(blendModel, 90, 90))
                                        .select(TripleShape.TOP, Direction.Axis.Y, rotated(topModel, 0, 0))
                                        .select(TripleShape.TOP, Direction.Axis.Z, rotated(topModel, 90, 0))
                                        .select(TripleShape.TOP, Direction.Axis.X, rotated(topModel, 90, 90));

                generator.acceptBlockState(MultiVariantGenerator.dispatch(block).with(dispatch));
                generator.delegateItemModel(block, bottomModel);
            });
        }
    }
}
