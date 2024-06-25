package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.wover.block.api.BlockTagProvider;
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

public abstract class BaseBookshelfBlock extends BaseBlock implements BlockTagProvider, BlockLootProvider {
    protected BaseBookshelfBlock(Block source) {
        this(Properties.ofFullCopy(source));
    }

    protected BaseBookshelfBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void provideBlockModels(WoverBlockModelGenerators generator) {
        generator.createBookshelf(this, this);
    }

    protected ResourceLocation replacePath(ResourceLocation blockId) {
        String newPath = blockId.getPath().replace("_bookshelf", "");
        return ResourceLocation.fromNamespaceAndPath(blockId.getNamespace(), newPath);
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
        public Wood(Block source) {
            super(source);
        }

        public Wood(Properties properties) {
            super(properties);
        }
    }

    public static class VanillaWood extends Wood {
        private final Block parent;

        public VanillaWood(Block source) {
            super(source);
            this.parent = source;
        }

        @Override
        @Environment(EnvType.CLIENT)
        public void provideBlockModels(WoverBlockModelGenerators generator) {
            generator.createBookshelf(this, parent);
        }
    }

    public static BaseBookshelfBlock from(Block source) {
        return new BaseBookshelfBlock.Wood(source);
    }
}
