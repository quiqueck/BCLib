package org.betterx.bclib.client.models;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.furniture.block.BaseChair;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;

import net.minecraft.core.Direction;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import static org.betterx.bclib.furniture.block.AbstractChair.FACING;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class BCLModels {
    public static final TextureSlot CLOTH = TextureSlot.create("cloth");
    public static final TextureSlot TEXTURE1 = TextureSlot.create("texture1");
    public static final TextureSlot GLOW = TextureSlot.create("glow");
    public static final TextureSlot METAL = TextureSlot.create("metal");
    public static final TextureSlot GLASS = TextureSlot.create("glass");
    public static final TextureSlot PANEL = TextureSlot.create("panel");

    public static final ResourceLocation BAR_STOOL_MODEL_LOCATION = BCLib.C.mk("block/bar_stool");
    public static final ModelTemplate BAR_STOOL = new ModelTemplate(
            Optional.of(BAR_STOOL_MODEL_LOCATION),
            Optional.empty(),
            TextureSlot.TEXTURE, CLOTH
    );

    public static final ResourceLocation CHAIR_MODEL_LOCATION = BCLib.C.mk("block/chair");
    public static final ModelTemplate CHAIR = new ModelTemplate(
            Optional.of(CHAIR_MODEL_LOCATION),
            Optional.empty(),
            TextureSlot.TEXTURE
    );

    public static final ModelTemplate CHAIR_TOP = new ModelTemplate(
            Optional.empty(),
            Optional.of("_top"),
            TextureSlot.PARTICLE
    );

    public static final ResourceLocation TABURET_MODEL_LOCATION = BCLib.C.mk("block/taburet");
    public static final ModelTemplate TABURET = new ModelTemplate(
            Optional.of(TABURET_MODEL_LOCATION),
            Optional.empty(),
            TextureSlot.TEXTURE
    );

    public static final ResourceLocation CHEST_MODEL_LOCATION = BCLib.C.mk("block/chest_item");
    public static final ModelTemplate CHEST_ITEM = new ModelTemplate(
            Optional.of(CHEST_MODEL_LOCATION),
            Optional.empty(),
            TextureSlot.TEXTURE
    );

    public static final ResourceLocation PATH_MODEL_LOCATION = BCLib.C.mk("block/path");
    public static final ModelTemplate PATH = new ModelTemplate(
            Optional.of(PATH_MODEL_LOCATION),
            Optional.empty(),
            TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.SIDE
    );

    public static final ResourceLocation LADDER_MODEL_LOCATION = BCLib.C.mk("block/ladder");
    public static final ModelTemplate LADDER = new ModelTemplate(
            Optional.of(LADDER_MODEL_LOCATION),
            Optional.empty(),
            TextureSlot.TEXTURE
    );

    public static final ModelTemplate BULB_LANTERN_FLOOR = new ModelTemplate(
            Optional.of(BCLib.C.mk("block/bulb_lantern_floor")),
            Optional.empty(),
            GLOW, METAL
    );

    public static final ModelTemplate BULB_LANTERN_CEIL = new ModelTemplate(
            Optional.of(BCLib.C.mk("block/bulb_lantern_ceil")),
            Optional.empty(),
            GLOW, METAL
    );

    public static final ModelTemplate STONE_LANTERN_FLOOR = new ModelTemplate(
            Optional.of(BCLib.C.mk("block/stone_lantern_floor")),
            Optional.empty(),
            TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.SIDE, GLASS
    );

    public static final ModelTemplate STONE_LANTERN_CEIL = new ModelTemplate(
            Optional.of(BCLib.C.mk("block/stone_lantern_ceil")),
            Optional.empty(),
            TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.SIDE, GLASS
    );

    public static final ModelTemplate CROSS_SHADED = new ModelTemplate(
            Optional.of(BCLib.C.mk("block/cross_shaded")),
            Optional.empty(),
            TextureSlot.CROSS
    );

    public static final ModelTemplate ANVIL = new ModelTemplate(
            Optional.of(BCLib.C.mk("block/anvil")),
            Optional.empty(),
            TextureSlot.FRONT, TextureSlot.BACK, TextureSlot.TOP, TextureSlot.BOTTOM, PANEL
    );

    public static final ModelTemplate TRAPDOOR = new ModelTemplate(
            Optional.of(BCLib.C.mk("block/sided_trapdoor")),
            Optional.empty(),
            TextureSlot.TEXTURE, TextureSlot.SIDE
    );

    public static final ModelTemplate FURNACE_GLOW = new ModelTemplate(
            Optional.of(BCLib.C.mk("block/furnace_glow")),
            Optional.empty(),
            TextureSlot.TOP, TextureSlot.SIDE, TextureSlot.FRONT, GLOW
    );

    public static void createBarStoolBlockModel(
            WoverBlockModelGenerators generators,
            Block block,
            Block woodType,
            Block clothType
    ) {
        TextureMapping mapping = WoverBlockModelGenerators.textureMappingOf(
                TextureSlot.TEXTURE,
                TextureMapping.getBlockTexture(woodType),
                CLOTH,
                TextureMapping.getBlockTexture(clothType)
        );
        ResourceLocation modelLocation = BAR_STOOL.create(block, mapping, generators.vanillaGenerator.modelOutput);

        var blockStateGenerator = MultiVariantGenerator
                .multiVariant(block)
                .with(getChairFacingPropertyDispatch(modelLocation));
        generators.acceptBlockState(blockStateGenerator);
    }

    public static void createTaburetBlockModel(
            WoverBlockModelGenerators generators,
            Block block,
            Block woodType
    ) {
        TextureMapping mapping = WoverBlockModelGenerators.textureMappingOf(
                TextureSlot.TEXTURE,
                TextureMapping.getBlockTexture(woodType)
        );
        ResourceLocation modelLocation = TABURET.create(block, mapping, generators.vanillaGenerator.modelOutput);

        var blockStateGenerator = MultiVariantGenerator
                .multiVariant(block)
                .with(getChairFacingPropertyDispatch(modelLocation));
        generators.acceptBlockState(blockStateGenerator);
    }

    public static void createChairBlockModel(
            WoverBlockModelGenerators generators,
            Block block,
            Block woodType,
            Block cloth
    ) {
        TextureMapping mapping = WoverBlockModelGenerators.textureMappingOf(
                TextureSlot.TEXTURE,
                TextureMapping.getBlockTexture(woodType),
                TextureSlot.PARTICLE,
                TextureMapping.getBlockTexture(woodType)
        );
        ResourceLocation modelLocation = CHAIR.create(block, mapping, generators.vanillaGenerator.modelOutput);
        ResourceLocation topLocation = generators.particleOnlyModel(woodType);//CHAIR_TOP.create(block, mapping, generators.vanillaGenerator.modelOutput);


        var blockStateGenerator = MultiVariantGenerator
                .multiVariant(block)
                .with(
                        PropertyDispatch
                                .properties(FACING, BaseChair.TOP)
                                .select(
                                        Direction.EAST,
                                        false,
                                        Variant.variant()
                                               .with(VariantProperties.MODEL, modelLocation)
                                               .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                                )
                                .select(
                                        Direction.SOUTH,
                                        false,
                                        Variant.variant()
                                               .with(VariantProperties.MODEL, modelLocation)
                                               .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                                )
                                .select(
                                        Direction.WEST,
                                        false,
                                        Variant.variant()
                                               .with(VariantProperties.MODEL, modelLocation)
                                               .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                                )
                                .select(
                                        Direction.NORTH,
                                        false,
                                        Variant.variant()
                                               .with(VariantProperties.MODEL, modelLocation)
                                )
                                .select(
                                        Direction.NORTH,
                                        true,
                                        Variant.variant()
                                               .with(VariantProperties.MODEL, topLocation)
                                )
                                .select(
                                        Direction.EAST,
                                        true,
                                        Variant.variant()
                                               .with(VariantProperties.MODEL, topLocation)
                                )
                                .select(
                                        Direction.SOUTH,
                                        true,
                                        Variant.variant()
                                               .with(VariantProperties.MODEL, topLocation)
                                )
                                .select(
                                        Direction.WEST,
                                        true,
                                        Variant.variant()
                                               .with(VariantProperties.MODEL, topLocation)
                                )
                );
        generators.acceptBlockState(blockStateGenerator);
    }

    private static PropertyDispatch.@NotNull C1<Direction> getChairFacingPropertyDispatch(ResourceLocation modelLocation) {
        return PropertyDispatch
                .property(FACING)
                .select(
                        Direction.NORTH,
                        Variant.variant()
                               .with(VariantProperties.MODEL, modelLocation)
                               .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                )
                .select(
                        Direction.EAST,
                        Variant.variant()
                               .with(VariantProperties.MODEL, modelLocation)
                )
                .select(
                        Direction.SOUTH,
                        Variant.variant()
                               .with(VariantProperties.MODEL, modelLocation)
                               .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                )
                .select(
                        Direction.WEST,
                        Variant.variant()
                               .with(VariantProperties.MODEL, modelLocation)
                               .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                );
    }

}
