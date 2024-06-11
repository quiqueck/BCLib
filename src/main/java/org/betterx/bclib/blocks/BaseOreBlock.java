package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.BehaviourBuilders;
import org.betterx.bclib.behaviours.interfaces.BehaviourOre;
import org.betterx.bclib.interfaces.RuntimeBlockModelProvider;
import org.betterx.bclib.util.LegacyTiers;
import org.betterx.wover.block.api.BlockTagProvider;
import org.betterx.wover.loot.api.BlockLootProvider;
import org.betterx.wover.loot.api.LootLookupProvider;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BaseOreBlock extends DropExperienceBlock implements RuntimeBlockModelProvider, BlockTagProvider, BehaviourOre, BlockLootProvider {
    private final Supplier<Item> dropItem;
    private final int minCount;
    private final int maxCount;
    private final TagKey<Block> miningTag;

    public BaseOreBlock(Supplier<Item> drop, int minCount, int maxCount, int experience) {
        this(drop, minCount, maxCount, experience, null);
    }

    public BaseOreBlock(Supplier<Item> drop, int minCount, int maxCount, int experience, TagKey<Block> miningTag) {
        this(
                BehaviourBuilders
                        .createStone(MapColor.SAND)
                        .requiresCorrectToolForDrops()
                        .destroyTime(3F)
                        .explosionResistance(9F)
                        .sound(SoundType.STONE),
                drop, minCount, maxCount, experience, miningTag
        );
    }

    public BaseOreBlock(Properties properties, Supplier<Item> drop, int minCount, int maxCount, int experience) {
        this(properties, drop, minCount, maxCount, experience, null);
    }

    public BaseOreBlock(
            Properties properties,
            Supplier<Item> drop,
            int minCount,
            int maxCount,
            int experience,
            TagKey<Block> miningTag
    ) {
        super(UniformInt.of(experience > 0 ? 1 : 0, experience), properties);
        this.dropItem = drop;
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.miningTag = miningTag;
    }

    @Deprecated(forRemoval = true)
    public BaseOreBlock(Supplier<Item> drop, int minCount, int maxCount, int experience, int miningLevel) {
        this(
                BehaviourBuilders
                        .createStone(MapColor.SAND)
                        .requiresCorrectToolForDrops()
                        .destroyTime(3F)
                        .explosionResistance(9F)
                        .sound(SoundType.STONE),
                drop, minCount, maxCount, experience, miningLevel
        );
    }

    @Deprecated(forRemoval = true)
    public BaseOreBlock(
            Properties properties,
            Supplier<Item> drop,
            int minCount,
            int maxCount,
            int experience,
            int miningLevel
    ) {
        this(properties, drop, minCount, maxCount, experience, LegacyTiers
                .forLevel(miningLevel)
                .map(t -> t.toolRequirementTag)
                .orElse(null));
    }

    @Override
    public BlockModel getItemModel(ResourceLocation resourceLocation) {
        return getBlockModel(resourceLocation, defaultBlockState());
    }


    @Override
    public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
        if (this.miningTag != null) {
            context.add(this.miningTag, this);
        }
    }

    @Override
    public @Nullable LootTable.Builder registerBlockLoot(
            @NotNull ResourceLocation location,
            @NotNull LootLookupProvider provider,
            @NotNull ResourceKey<LootTable> tableKey
    ) {
        return provider.dropOre(this, dropItem.get(), UniformGenerator.between(minCount, maxCount));
    }
}
