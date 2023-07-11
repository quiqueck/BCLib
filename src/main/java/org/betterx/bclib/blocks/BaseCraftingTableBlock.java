package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.bclib.interfaces.BlockModelProvider;
import org.betterx.bclib.interfaces.TagProvider;
import org.betterx.worlds.together.tag.v3.CommonBlockTags;
import org.betterx.worlds.together.tag.v3.CommonItemTags;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public abstract class BaseCraftingTableBlock extends CraftingTableBlock implements BlockModelProvider, TagProvider, DropSelfLootProvider<BaseCraftingTableBlock> {
    protected BaseCraftingTableBlock(Block source) {
        this(Properties.copy(source));
    }

    protected BaseCraftingTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }


    @Override
    @Environment(EnvType.CLIENT)
    public BlockModel getItemModel(ResourceLocation resourceLocation) {
        return getBlockModel(resourceLocation, defaultBlockState());
    }

    @Override
    @Environment(EnvType.CLIENT)
    public @Nullable BlockModel getBlockModel(ResourceLocation blockId, BlockState blockState) {
        String blockName = blockId.getPath();
        Optional<String> pattern = PatternsHelper.createJson(BasePatterns.BLOCK_SIDED, new HashMap<String, String>() {
            private static final long serialVersionUID = 1L;

            {
                put("%modid%", blockId.getNamespace());
                put("%particle%", blockName + "_front");
                put("%down%", blockName + "_bottom");
                put("%up%", blockName + "_top");
                put("%north%", blockName + "_front");
                put("%south%", blockName + "_side");
                put("%west%", blockName + "_front");
                put("%east%", blockName + "_side");
            }
        });
        return ModelsHelper.fromPattern(pattern);
    }

    @Override
    public void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
        blockTags.add(CommonBlockTags.WORKBENCHES);
        itemTags.add(CommonItemTags.WORKBENCHES);
    }

    public static class Wood extends BaseCraftingTableBlock implements BehaviourWood {
        public Wood(Block source) {
            super(source);
        }

        public Wood(BlockBehaviour.Properties properties) {
            super(properties);
        }
    }

    public static BaseCraftingTableBlock from(Block source) {
        return new BaseCraftingTableBlock.Wood(source);
    }
}
