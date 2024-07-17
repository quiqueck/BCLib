package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.wover.block.api.BlockTagProvider;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;
import org.betterx.wover.loot.api.BlockLootProvider;
import org.betterx.wover.loot.api.LootLookupProvider;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;
import org.betterx.wover.tag.api.predefined.CommonBlockTags;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseBookshelfBlock extends BaseBlock implements BlockTagProvider, BlockLootProvider, BlockModelProvider {
    private final Block topBlock;

    protected BaseBookshelfBlock(Block topBlock) {
        this(topBlock, Properties.ofFullCopy(topBlock));
    }

    protected BaseBookshelfBlock(Block topBlock, BlockBehaviour.Properties properties) {
        super(properties);
        this.topBlock = topBlock;
    }

//    @Deprecated(forRemoval = true)
//    protected BaseBookshelfBlock(BlockBehaviour.Properties properties) {
//        super(properties);
//        this.parent = this;
//    }

    @Override
    @Environment(EnvType.CLIENT)
    public void provideBlockModels(WoverBlockModelGenerators generator) {
        generator.createBookshelf(this, this.topBlock);
    }

    @Override
    public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
        context.add(this, CommonBlockTags.BOOKSHELVES);
    }

    @Override
    public @Nullable LootTable.Builder registerBlockLoot(
            @NotNull ResourceLocation location,
            @NotNull LootLookupProvider provider,
            @NotNull ResourceKey<LootTable> tableKey
    ) {
        return provider.dropWithSilkTouch(this, Items.BOOK, ConstantValue.exactly(3));
    }

    public static class Wood extends BaseBookshelfBlock implements BehaviourWood {
        public Wood(Block topBlock) {
            super(topBlock);
        }

        public Wood(Block topBlock, Properties properties) {
            super(topBlock, properties);
        }

//        @Deprecated(forRemoval = true)
//        public Wood(Properties properties) {
//            super(properties);
//        }
    }

    public static class VanillaWood extends Wood {
        public VanillaWood(Block topBlock) {
            super(topBlock);
        }
    }

    public static BaseBookshelfBlock from(Block topBlock) {
        return new BaseBookshelfBlock.Wood(topBlock);
    }
}
