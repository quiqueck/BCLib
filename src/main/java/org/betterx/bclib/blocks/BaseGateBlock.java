package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.wover.block.api.BlockTagProvider;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;

import net.minecraft.core.Direction;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.properties.WoodType;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.function.BiFunction;

public abstract class BaseGateBlock extends FenceGateBlock implements BlockModelProvider, BlockTagProvider, DropSelfLootProvider<BaseGateBlock> {
    private final Block parent;

    protected BaseGateBlock(Block source, WoodType type) {
        super(type, Properties.ofFullCopy(source).noOcclusion());
        this.parent = source;
    }

    @Override
    public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
        context.add(BlockTags.FENCE_GATES, this);
    }

    @Environment(EnvType.CLIENT)
    private VariantProperties.Rotation yRotationForState(Direction dir) {
        VariantProperties.Rotation y = VariantProperties.Rotation.R0;
        switch (dir) {
            case EAST:
                y = VariantProperties.Rotation.R90;
                break;
            case NORTH:
                break;
            case SOUTH:
                y = VariantProperties.Rotation.R180;
                break;
            case WEST:
                y = VariantProperties.Rotation.R270;
                break;
            default:
                break;
        }
        return y;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void provideBlockModels(WoverBlockModelGenerators generator) {
        final var id = TextureMapping.getBlockTexture(parent);
        final var mapping = new TextureMapping()
                .put(TextureSlot.TEXTURE, id);
        final var modelOpen = ModelTemplates.FENCE_GATE_OPEN.createWithSuffix(this, "_open", mapping, generator.modelOutput());
        final var modelClosed = ModelTemplates.FENCE_GATE_CLOSED.createWithSuffix(this, "_closed", mapping, generator.modelOutput());

        final var modelWallOpen = ModelTemplates.FENCE_GATE_WALL_OPEN.createWithSuffix(this, "_wall_open", mapping, generator.modelOutput());
        final var modelWallClosed = ModelTemplates.FENCE_GATE_WALL_CLOSED.createWithSuffix(this, "_wall_closed", mapping, generator.modelOutput());

        final BiFunction<Boolean, Boolean, ResourceLocation> modelForState = (inWall, isOpen) -> inWall
                ? (isOpen ? modelWallOpen : modelWallClosed)
                : (isOpen ? modelOpen : modelClosed);

        final var props = PropertyDispatch.properties(IN_WALL, OPEN, FACING);
        final Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        final boolean[] open = {true, false};

        for (Direction dir : directions) {
            for (boolean inWall : open) {
                for (boolean isOpen : open) {
                    props.select(inWall, isOpen, dir, Variant
                            .variant()
                            .with(VariantProperties.MODEL, modelForState.apply(inWall, isOpen))
                            .with(VariantProperties.Y_ROT, yRotationForState(dir))
                    );
                }
            }
        }

        generator.acceptBlockState(MultiVariantGenerator
                .multiVariant(this)
                .with(props));

        generator.delegateItemModel(this, modelClosed);
    }

    public static class Wood extends BaseGateBlock implements BehaviourWood {
        public Wood(Block source, WoodType type) {
            super(source, type);
        }
    }

    public static BaseGateBlock from(Block source, WoodType type) {
        return new BaseGateBlock.Wood(source, type);
    }
}