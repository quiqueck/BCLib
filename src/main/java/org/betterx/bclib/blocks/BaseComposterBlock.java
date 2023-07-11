package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.bclib.interfaces.BlockModelProvider;
import org.betterx.bclib.interfaces.TagProvider;
import org.betterx.worlds.together.tag.v3.CommonBlockTags;
import org.betterx.worlds.together.tag.v3.CommonPoiTags;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public abstract class BaseComposterBlock extends ComposterBlock implements BlockModelProvider, TagProvider, DropSelfLootProvider<BaseComposterBlock> {
    protected BaseComposterBlock(Block source) {
        super(Properties.copy(source));
    }


    @Override
    @Environment(EnvType.CLIENT)
    public BlockModel getItemModel(ResourceLocation resourceLocation) {
        return getBlockModel(resourceLocation, defaultBlockState());
    }

    @Override
    @Environment(EnvType.CLIENT)
    public @Nullable BlockModel getBlockModel(ResourceLocation blockId, BlockState blockState) {
        Optional<String> pattern = PatternsHelper.createJson(BasePatterns.BLOCK_COMPOSTER, blockId);
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

        ModelsHelper.MultiPartBuilder builder = ModelsHelper.MultiPartBuilder.create(stateDefinition);
        LEVEL.getPossibleValues().forEach(level -> {
            if (level > 0) {
                ResourceLocation contentId;
                if (level > 7) {
                    contentId = new ResourceLocation("block/composter_contents_ready");
                } else {
                    contentId = new ResourceLocation("block/composter_contents" + level);
                }
                builder.part(contentId).setCondition(state -> state.getValue(LEVEL).equals(level)).add();
            }
        });
        builder.part(modelId).add();

        return builder.build();
    }

    @Override
    public void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
        blockTags.add(CommonBlockTags.COMPOSTER);
        blockTags.add(CommonPoiTags.FARMER_WORKSTATION);
    }

    public static class Wood extends BaseComposterBlock implements BehaviourWood {
        public Wood(Block source) {
            super(source);
        }

        @Override
        public void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
            super.addTags(blockTags, itemTags);
            blockTags.add(CommonBlockTags.WOODEN_COMPOSTER);
        }
    }

    public static BaseComposterBlock from(Block source) {
        return new BaseComposterBlock.Wood(source);
    }
}
