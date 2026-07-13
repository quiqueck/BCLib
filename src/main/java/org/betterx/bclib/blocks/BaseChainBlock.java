package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.client.render.BCLRenderLayer;
import org.betterx.bclib.interfaces.RenderLayerProvider;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;

import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public abstract class BaseChainBlock extends ChainBlock implements BlockModelProvider, RenderLayerProvider, DropSelfLootProvider<BaseChainBlock> {
    public BaseChainBlock(MapColor color) {
        this(Properties.ofFullCopy(Blocks.CHAIN).mapColor(color));
    }

    public BaseChainBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void provideBlockModels(WoverBlockModelGenerators generator) {
        generator.createChainModel(this, TextureMapping.getBlockTexture(this));
    }

//    @Override
//    @Environment(EnvType.CLIENT)
//    public BlockModel getItemModel(ResourceLocation blockId) {
//        return ModelsHelper.createItemModel(blockId);
//    }
//
//    @Override
//    @Environment(EnvType.CLIENT)
//    public @Nullable BlockModel getBlockModel(ResourceLocation blockId, BlockState blockState) {
//        Optional<String> pattern = PatternsHelper.createJson(BasePatterns.BLOCK_CHAIN, blockId);
//        return ModelsHelper.fromPattern(pattern);
//    }
//
//    @Override
//    @Environment(EnvType.CLIENT)
//    public MultiVariant getModelVariant(
//            ResourceLocation stateId,
//            BlockState blockState,
//            Map<ResourceLocation, UnbakedModel> modelCache
//    ) {
//        Direction.Axis axis = blockState.getValue(AXIS);
//        ResourceLocation modelId = RuntimeBlockModelProvider.remapResourceLocation(stateId, blockState);
//        registerBlockModel(stateId, modelId, blockState, modelCache);
//        return ModelsHelper.createRotatedModel(modelId, axis);
//    }

    @Override
    public BCLRenderLayer getRenderLayer() {
        return BCLRenderLayer.CUTOUT;
    }

    public static class Metal extends BaseChainBlock implements BehaviourMetal {

        public Metal(MapColor color) {
            super(color);
        }

        public Metal(Properties properties) {
            super(properties);
        }
    }
}
