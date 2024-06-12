package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.wover.block.api.BlockTagProvider;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;
import org.betterx.wover.item.api.ItemTagProvider;
import org.betterx.wover.tag.api.event.context.ItemTagBootstrapContext;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;

import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public abstract class BaseCraftingTableBlock extends CraftingTableBlock implements DropSelfLootProvider<BaseCraftingTableBlock>, BlockModelProvider, BlockTagProvider, ItemTagProvider {
    protected BaseCraftingTableBlock(Block source) {
        this(Properties.ofFullCopy(source));
    }

    protected BaseCraftingTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static TextureMapping craftingTableTextureMapping(Block block, Block block2) {
        return (new TextureMapping())
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block, "_front"))
                .put(TextureSlot.DOWN, TextureMapping.getBlockTexture(block2, "_bottom"))
                .put(TextureSlot.UP, TextureMapping.getBlockTexture(block, "_top"))
                .put(TextureSlot.NORTH, TextureMapping.getBlockTexture(block, "_front"))
                .put(TextureSlot.EAST, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.WEST, TextureMapping.getBlockTexture(block, "_front"));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void provideBlockModels(WoverBlockModelGenerators generators) {
        generators.vanillaGenerator.createCraftingTableLike(this, this, BaseCraftingTableBlock::craftingTableTextureMapping);
    }

    @Override
    public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
        context.add(org.betterx.wover.tag.api.predefined.CommonBlockTags.WORKBENCHES, this);
    }

    @Override
    public void registerItemTags(ResourceLocation location, ItemTagBootstrapContext context) {
        context.add(this, org.betterx.wover.tag.api.predefined.CommonItemTags.WORKBENCHES);
    }

//    @Override
//    @Environment(EnvType.CLIENT)
//    public BlockModel getItemModel(ResourceLocation resourceLocation) {
//        return getBlockModel(resourceLocation, defaultBlockState());
//    }
//
//    @Override
//    @Environment(EnvType.CLIENT)
//    public @Nullable BlockModel getBlockModel(ResourceLocation blockId, BlockState blockState) {
//        String blockName = blockId.getPath();
//        Optional<String> pattern = PatternsHelper.createJson(BasePatterns.BLOCK_SIDED, new HashMap<String, String>() {
//            private static final long serialVersionUID = 1L;
//
//            {
//                put("%modid%", blockId.getNamespace());
//                put("%particle%", blockName + "_front");
//                put("%down%", blockName + "_bottom");
//                put("%up%", blockName + "_top");
//                put("%north%", blockName + "_front");
//                put("%south%", blockName + "_side");
//                put("%west%", blockName + "_front");
//                put("%east%", blockName + "_side");
//            }
//        });
//        return ModelsHelper.fromPattern(pattern);
//    }

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
