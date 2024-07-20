package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.client.models.BCLModels;
import org.betterx.bclib.registry.BaseBlockEntities;
import org.betterx.wover.block.api.BlockTagProvider;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;
import org.betterx.wover.item.api.ItemTagProvider;
import org.betterx.wover.loot.api.BlockLootProvider;
import org.betterx.wover.loot.api.LootLookupProvider;
import org.betterx.wover.tag.api.event.context.ItemTagBootstrapContext;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;
import org.betterx.wover.tag.api.predefined.CommonBlockTags;
import org.betterx.wover.tag.api.predefined.CommonItemTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public abstract class BaseChestBlock extends ChestBlock implements BlockModelProvider, BlockTagProvider, ItemTagProvider, BlockLootProvider {
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

    @Override
    public LootTable.Builder registerBlockLoot(
            @NotNull ResourceLocation location,
            @NotNull LootLookupProvider provider,
            @NotNull ResourceKey<LootTable> tableKey
    ) {
        LootTable.Builder builder = LootTable.lootTable();
        var pool = LootPool.lootPool()
                           .setRolls(ConstantValue.exactly(1.0f))
                           .add(LootItem.lootTableItem(this).apply(CopyComponentsFunction
                                   .copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
                                   .include(DataComponents.CUSTOM_NAME)
                                   .include(DataComponents.CONTAINER)
                                   .include(DataComponents.LOCK)
                                   .include(DataComponents.CONTAINER_LOOT)))
                           .when(ExplosionCondition.survivesExplosion());
        builder.setRandomSequence(this.asItem().builtInRegistryHolder().unwrapKey().orElseThrow().location());

        return builder.withPool(pool);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState blockState, LootParams.Builder builder) {
        return super.getDrops(blockState, builder);
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
