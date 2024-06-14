package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.bclib.interfaces.RuntimeBlockModelProvider;
import org.betterx.bclib.registry.BaseBlockEntities;
import org.betterx.wover.block.api.BlockTagProvider;
import org.betterx.wover.item.api.ItemTagProvider;
import org.betterx.wover.tag.api.event.context.ItemTagBootstrapContext;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;
import org.betterx.wover.tag.api.predefined.CommonBlockTags;
import org.betterx.wover.tag.api.predefined.CommonItemTags;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public abstract class BaseChestBlock extends ChestBlock implements RuntimeBlockModelProvider, BlockTagProvider, ItemTagProvider, DropSelfLootProvider<BaseChestBlock> {
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
    @Environment(EnvType.CLIENT)
    public BlockModel getItemModel(ResourceLocation blockId) {
        Optional<String> pattern = PatternsHelper.createJson(BasePatterns.ITEM_CHEST, blockId);
        return ModelsHelper.fromPattern(pattern);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public @Nullable BlockModel getBlockModel(ResourceLocation resourceLocation, BlockState blockState) {
        ResourceLocation parentId = BuiltInRegistries.BLOCK.getKey(parent);
        return ModelsHelper.createBlockEmpty(parentId);
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
