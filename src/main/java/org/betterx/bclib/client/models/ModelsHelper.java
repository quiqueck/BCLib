package org.betterx.bclib.client.models;

import com.mojang.math.Quadrant;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedList;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.StringReader;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class ModelsHelper {
    //TODO: @Deprecated(forRemoval = true, since = "1.21.7")
    public static BlockModel fromPattern(Optional<String> pattern) {
        return pattern.map(s -> {
            StringReader reader = new StringReader(s);
            return BlockModel.fromStream(reader);
        }).orElse(null);
    }

    public static BlockModel createItemModel(ResourceLocation resourceLocation) {
        return fromPattern(PatternsHelper.createItemGenerated(resourceLocation));
    }

    public static BlockModel createHandheldItem(ResourceLocation resourceLocation) {
        return fromPattern(PatternsHelper.createItemHandheld(resourceLocation));
    }

    public static BlockModel createBlockItem(ResourceLocation resourceLocation) {
        Optional<String> pattern = PatternsHelper.createJson(BasePatterns.ITEM_BLOCK, resourceLocation);
        return fromPattern(pattern);
    }

    public static BlockModel createBlockEmpty(ResourceLocation resourceLocation) {
        Optional<String> pattern = PatternsHelper.createJson(BasePatterns.BLOCK_EMPTY, resourceLocation);
        return fromPattern(pattern);
    }

    public static MultiVariant createMultiVariant(
            ResourceLocation resourceLocation,
            Quadrant rotX,
            Quadrant rotY,
            boolean uvLock
    ) {

        Variant variant = new Variant(resourceLocation, new Variant.SimpleModelState(rotX, rotY, uvLock));
        return new MultiVariant(WeightedList.<Variant>builder().add(variant).build());
    }

    public static MultiVariant createBlockSimple(ResourceLocation resourceLocation) {
        return createMultiVariant(resourceLocation, Quadrant.R0, Quadrant.R0, false);
    }

    public static MultiVariant createFacingModel(
            ResourceLocation resourceLocation,
            Direction facing,
            boolean uvLock,
            boolean inverted
    ) {
        if (inverted) {
            facing = facing.getOpposite();
        }
        Quadrant qY;
        switch (facing) {
            case NORTH:
                qY = Quadrant.R180;
                break;
            case SOUTH:
                qY = Quadrant.R0;
                break;
            case WEST:
                qY = Quadrant.R90;
                break;
            case EAST:
                qY = Quadrant.R270;
                break;
            default:
                throw new IllegalArgumentException("Invalid facing direction: " + facing);
        }
        return createMultiVariant(resourceLocation, Quadrant.R0, qY, uvLock);
    }

    public static MultiVariant createRotatedModel(ResourceLocation resourceLocation, Direction.Axis axis) {
        Quadrant qX, qY;
        switch (axis) {
            case X:
                qX = Quadrant.R90;
                qY = Quadrant.R90;
                break;
            case Z:
                qX = Quadrant.R90;
                qY = Quadrant.R0;
                break;
            default:
                qX = Quadrant.R0;
                qY = Quadrant.R0;
                break;
        }
        return createMultiVariant(resourceLocation, qX, qY, false);
    }

    public static MultiVariant createRandomTopModel(ResourceLocation resourceLocation) {
        return new MultiVariant(
                WeightedList.<Variant>builder()
                            .add(
                                    new Variant(
                                            resourceLocation,
                                            new Variant.SimpleModelState(Quadrant.R0, Quadrant.R0, false)
                                    ), 1
                            )
                            .add(
                                    new Variant(
                                            resourceLocation,
                                            new Variant.SimpleModelState(Quadrant.R0, Quadrant.R90, false)
                                    ), 1
                            )
                            .add(
                                    new Variant(
                                            resourceLocation,
                                            new Variant.SimpleModelState(Quadrant.R0, Quadrant.R180, false)
                                    ), 1
                            )
                            .add(
                                    new Variant(
                                            resourceLocation,
                                            new Variant.SimpleModelState(Quadrant.R0, Quadrant.R270, false)
                                    ), 1
                            )
                            .build()
        );
    }
}
