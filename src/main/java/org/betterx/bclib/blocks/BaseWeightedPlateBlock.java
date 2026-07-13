package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeightedPressurePlateBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;

public class BaseWeightedPlateBlock extends WeightedPressurePlateBlock implements BlockModelProvider, DropSelfLootProvider<BaseWeightedPlateBlock> {
    private final Block parent;

    public BaseWeightedPlateBlock(Block source, BlockSetType type) {
        super(
                15,
                type,
                Properties.ofFullCopy(source)
                          .noCollission()
                          .noOcclusion()
                          .requiresCorrectToolForDrops()
                          .strength(0.5F)
        );
        this.parent = source;
    }

//    @Override
//    @Environment(EnvType.CLIENT)
//    public BlockModel getItemModel(ResourceLocation resourceLocation) {
//        return getBlockModel(resourceLocation, defaultBlockState());
//    }
//
//    @Override
//    @Environment(EnvType.CLIENT)
//    public @Nullable BlockModel getBlockModel(ResourceLocation resourceLocation, BlockState blockState) {
//        ResourceLocation parentId = BuiltInRegistries.BLOCK.getKey(parent);
//        Optional<String> pattern;
//        if (blockState.getValue(POWER) > 0) {
//            pattern = PatternsHelper.createJson(BasePatterns.BLOCK_PLATE_DOWN, parentId);
//        } else {
//            pattern = PatternsHelper.createJson(BasePatterns.BLOCK_PLATE_UP, parentId);
//        }
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
//        String state = blockState.getValue(POWER) > 0 ? "_down" : "_up";
//        ResourceLocation modelId = RuntimeBlockModelProvider.remapResourceLocation(
//                stateId,
//                blockState,
//                state
//        );
//        registerBlockModel(stateId, modelId, blockState, modelCache);
//        return ModelsHelper.createBlockSimple(modelId.id());
//    }

    @Override
    public void provideBlockModels(WoverBlockModelGenerators generator) {
        generator.createPressurePlate(this.parent, this);
    }
}
