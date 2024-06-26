package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.client.models.BCLModels;
import org.betterx.bclib.registry.BaseBlockEntities;
import org.betterx.wover.block.api.BlockTagProvider;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;
import org.betterx.wover.item.api.ItemTagProvider;
import org.betterx.wover.tag.api.event.context.ItemTagBootstrapContext;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;
import org.betterx.wover.tag.api.predefined.CommonBlockTags;
import org.betterx.wover.tag.api.predefined.CommonItemTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BaseChestBlock extends ChestBlock implements BlockModelProvider, BlockTagProvider, ItemTagProvider, DropSelfLootProvider<BaseChestBlock> {
    private final Block parent;

    protected BaseChestBlock(Block source) {
        super(Properties.ofFullCopy(source).noOcclusion(), () -> BaseBlockEntities.CHEST);
        this.parent = source;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return BaseBlockEntities.CHEST.create(blockPos, blockState);
    }

    @Override
    public void provideBlockModels(WoverBlockModelGenerators generator) {
        generator.createChest(parent, this);

        generator.createItemModel(
                this,
                BCLModels.CHEST_ITEM,
                new TextureMapping()
                        .put(TextureSlot.TEXTURE, BuiltInRegistries.BLOCK.getKey(this).withPrefix("entity/chest/"))
        );
    }

    @Override
    public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
        context.add(this, CommonBlockTags.CHEST);
    }

    @Override
    public void registerItemTags(ResourceLocation location, ItemTagBootstrapContext context) {
        context.add(this, CommonItemTags.CHEST);
    }

    public static class Wood extends BaseChestBlock implements BehaviourWood {
        public Wood(Block source) {
            super(source);
        }

        @Override
        public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
            context.add(this, CommonBlockTags.CHEST, CommonBlockTags.WOODEN_CHEST);
        }

        @Override
        public void registerItemTags(ResourceLocation location, ItemTagBootstrapContext context) {
            context.add(this, CommonItemTags.CHEST, CommonItemTags.WOODEN_CHEST);
        }
    }

    public static BaseChestBlock from(Block source) {
        return new BaseChestBlock.Wood(source);
    }
}
