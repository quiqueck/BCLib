package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.BehaviourBuilders;
import org.betterx.bclib.behaviours.BehaviourHelper;
import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.client.models.BCLModels;
import org.betterx.bclib.client.render.BCLRenderLayer;
import org.betterx.bclib.interfaces.RenderLayerProvider;
import org.betterx.wover.block.api.BlockTagProvider;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;
import org.betterx.wover.item.api.ItemTagProvider;
import org.betterx.wover.tag.api.event.context.ItemTagBootstrapContext;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;

import net.minecraft.core.Direction;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.Half;

public abstract class BaseTrapdoorBlock extends TrapDoorBlock implements RenderLayerProvider, BlockModelProvider, BlockTagProvider, ItemTagProvider, DropSelfLootProvider<BaseTrapdoorBlock> {
    protected BaseTrapdoorBlock(BlockBehaviour.Properties properties, BlockSetType type) {
        super(type, properties);
    }


    @Override
    public BCLRenderLayer getRenderLayer() {
        return BCLRenderLayer.CUTOUT;
    }


    @Override
    public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
        context.add(this, BlockTags.TRAPDOORS);
    }

    @Override
    public void registerItemTags(ResourceLocation location, ItemTagBootstrapContext context) {
        context.add(this, ItemTags.TRAPDOORS);
    }

    private VariantProperties.Rotation xRotationForState(boolean isTop, boolean isOpen, Direction dir) {
        return (isTop && isOpen)
                ? VariantProperties.Rotation.R270
                : isTop
                        ? VariantProperties.Rotation.R180
                        : isOpen ? VariantProperties.Rotation.R90 : VariantProperties.Rotation.R0;
    }

    private VariantProperties.Rotation yRotationForState(boolean isTop, boolean isOpen, Direction dir) {
        VariantProperties.Rotation y = VariantProperties.Rotation.R0;
        switch (dir) {
            case EAST:
                y = (isTop && isOpen) ? VariantProperties.Rotation.R270 : VariantProperties.Rotation.R90;
                break;
            case NORTH:
                if (isTop && isOpen) y = VariantProperties.Rotation.R180;
                break;
            case SOUTH:
                y = (isTop && isOpen) ? VariantProperties.Rotation.R0 : VariantProperties.Rotation.R180;
                break;
            case WEST:
                y = (isTop && isOpen) ? VariantProperties.Rotation.R90 : VariantProperties.Rotation.R270;
                break;
            default:
                break;
        }
        return y;
    }

    @Override
    public void provideBlockModels(WoverBlockModelGenerators generator) {
        final var id = TextureMapping.getBlockTexture(this);
        final var mapping = new TextureMapping()
                .put(TextureSlot.TEXTURE, id)
                .put(TextureSlot.SIDE, ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id
                        .getPath()
                        .replace("_trapdoor", "")).withSuffix("_door_side")
                );

        final var model = BCLModels.TRAPDOOR.create(this, mapping, generator.modelOutput());

        final var props = PropertyDispatch.properties(HALF, OPEN, FACING);
        final Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        final boolean[] open = {true, false};
        final Half[] halfs = {Half.TOP, Half.BOTTOM};
        for (Direction dir : directions) {
            for (Half half : halfs) {
                for (boolean isOpen : open) {
                    props.select(half, isOpen, dir, Variant
                            .variant()
                            .with(VariantProperties.MODEL, model)
                            .with(VariantProperties.X_ROT, xRotationForState(half == Half.TOP, isOpen, dir))
                            .with(VariantProperties.Y_ROT, yRotationForState(half == Half.TOP, isOpen, dir))
                    );
                }
            }
        }

        generator.acceptBlockState(MultiVariantGenerator
                .multiVariant(this)
                .with(props));
    }

    public static class Wood extends BaseTrapdoorBlock implements BehaviourWood {
        public Wood(Block source, BlockSetType type, boolean flammable) {
            this(BehaviourBuilders.createTrapDoor(source.defaultMapColor(), flammable).sound(SoundType.WOOD), type);
        }

        public Wood(Properties properties, BlockSetType type) {
            super(properties, type);
        }

        @Override
        public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
            context.add(this, BlockTags.TRAPDOORS, BlockTags.WOODEN_TRAPDOORS);
        }

        @Override
        public void registerItemTags(ResourceLocation location, ItemTagBootstrapContext context) {
            context.add(this, ItemTags.TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
        }
    }

    public static class Stone extends BaseTrapdoorBlock implements BehaviourStone {
        public Stone(Block source, BlockSetType type) {
            this(BehaviourBuilders.createTrapDoor(source.defaultMapColor(), false).sound(SoundType.STONE), type);
        }

        public Stone(Properties properties, BlockSetType type) {
            super(properties, type);
        }
    }

    public static class Metal extends BaseTrapdoorBlock implements BehaviourMetal {
        public Metal(Block source, BlockSetType type) {
            this(BehaviourBuilders.createTrapDoor(source.defaultMapColor(), false).sound(SoundType.METAL), type);
        }

        public Metal(Properties properties, BlockSetType type) {
            super(properties, type);
        }
    }

    public static BaseTrapdoorBlock from(Block source, BlockSetType type, boolean flammable) {
        return BehaviourHelper.from(source, type,
                (s, t) -> new Wood(s, t, flammable),
                Stone::new,
                Metal::new
        );
    }
}
