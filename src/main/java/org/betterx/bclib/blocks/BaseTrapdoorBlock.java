package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.BehaviourBuilders;
import org.betterx.bclib.behaviours.BehaviourHelper;
import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.bclib.client.render.BCLRenderLayer;
import org.betterx.bclib.interfaces.BlockModelProvider;
import org.betterx.bclib.interfaces.RenderLayerProvider;
import org.betterx.bclib.interfaces.TagProvider;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.Half;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public abstract class BaseTrapdoorBlock extends TrapDoorBlock implements RenderLayerProvider, BlockModelProvider, TagProvider, DropSelfLootProvider<BaseTrapdoorBlock> {
    protected BaseTrapdoorBlock(BlockBehaviour.Properties properties, BlockSetType type) {
        super(properties, type);
    }


    @Override
    public BCLRenderLayer getRenderLayer() {
        return BCLRenderLayer.CUTOUT;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public BlockModel getItemModel(ResourceLocation resourceLocation) {
        return getBlockModel(resourceLocation, defaultBlockState());
    }

    @Override
    @Environment(EnvType.CLIENT)
    public @Nullable BlockModel getBlockModel(ResourceLocation resourceLocation, BlockState blockState) {
        String name = resourceLocation.getPath();
        Optional<String> pattern = PatternsHelper.createJson(
                BasePatterns.BLOCK_TRAPDOOR,
                new HashMap<String, String>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("%modid%", resourceLocation.getNamespace());
                        put("%texture%", name);
                        put("%side%", name.replace("trapdoor", "door_side"));
                    }
                }
        );
        return ModelsHelper.fromPattern(pattern);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public UnbakedModel getModelVariant(
            ResourceLocation stateId,
            BlockState blockState,
            Map<ResourceLocation, UnbakedModel> modelCache
    ) {
        ResourceLocation modelId = new ResourceLocation(stateId.getNamespace(), "block/" + stateId.getPath());
        registerBlockModel(stateId, modelId, blockState, modelCache);
        boolean isTop = blockState.getValue(HALF) == Half.TOP;
        boolean isOpen = blockState.getValue(OPEN);
        int y = 0;
        int x = (isTop && isOpen) ? 270 : isTop ? 180 : isOpen ? 90 : 0;
        switch (blockState.getValue(FACING)) {
            case EAST:
                y = (isTop && isOpen) ? 270 : 90;
                break;
            case NORTH:
                if (isTop && isOpen) y = 180;
                break;
            case SOUTH:
                y = (isTop && isOpen) ? 0 : 180;
                break;
            case WEST:
                y = (isTop && isOpen) ? 90 : 270;
                break;
            default:
                break;
        }
        BlockModelRotation rotation = BlockModelRotation.by(x, y);
        return ModelsHelper.createMultiVariant(modelId, rotation.getRotation(), false);
    }

    @Override
    public void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
        blockTags.add(BlockTags.TRAPDOORS);
        itemTags.add(ItemTags.TRAPDOORS);
    }

    public static class Wood extends BaseTrapdoorBlock implements BehaviourWood {
        public Wood(Block source, BlockSetType type, boolean flammable) {
            this(BehaviourBuilders.createTrapDoor(source.defaultMapColor(), flammable).sound(SoundType.WOOD), type);
        }

        public Wood(Properties properties, BlockSetType type) {
            super(properties, type);
        }

        @Override
        public void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
            super.addTags(blockTags, itemTags);
            blockTags.add(BlockTags.WOODEN_TRAPDOORS);
            itemTags.add(ItemTags.WOODEN_TRAPDOORS);
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
