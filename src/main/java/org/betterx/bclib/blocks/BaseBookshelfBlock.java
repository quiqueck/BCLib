package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.wover.block.api.BlockTagProvider;
import org.betterx.wover.loot.api.BlockLootProvider;
import org.betterx.wover.loot.api.LootLookupProvider;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;
import org.betterx.wover.tag.api.predefined.CommonBlockTags;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Optional;
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
    public @Nullable BlockModel getBlockModel(ResourceLocation blockId, BlockState blockState) {
        Optional<String> pattern = PatternsHelper.createJson(BasePatterns.BLOCK_BOOKSHELF, replacePath(blockId));
        return ModelsHelper.fromPattern(pattern);
    }

    protected ResourceLocation replacePath(ResourceLocation blockId) {
        String newPath = blockId.getPath().replace("_bookshelf", "");
        return ResourceLocation.fromNamespaceAndPath(blockId.getNamespace(), newPath);
    }

    @Override
    public void registerItemTags(ResourceLocation location, TagBootstrapContext<Block> context) {
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
        public VanillaWood(Block source) {
            super(source);
        }

        @Override
        @Environment(EnvType.CLIENT)
        public @Nullable BlockModel getBlockModel(ResourceLocation blockId, BlockState blockState) {
            Optional<String> pattern = PatternsHelper.createJson(
                    BasePatterns.VANILLA_WOOD_BOOKSHELF,
                    replacePath(blockId)
            );
            return ModelsHelper.fromPattern(pattern);
        }
    }

    public static BaseBookshelfBlock from(Block source) {
        return new BaseBookshelfBlock.Wood(source);
    }
}
