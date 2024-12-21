package org.betterx.bclib.blocks;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.betterx.bclib.behaviours.BehaviourBuilders;
import org.betterx.bclib.behaviours.interfaces.BehaviourOre;
import org.betterx.bclib.interfaces.BlockModelProvider;
import org.betterx.bclib.interfaces.TagProvider;
import org.betterx.bclib.util.LootUtil;
import org.betterx.bclib.util.MHelper;

import net.fabricmc.fabric.api.mininglevel.v1.MiningLevelManager;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class BaseOreBlock extends DropExperienceBlock implements BlockModelProvider, TagProvider, BehaviourOre {
    private final Supplier<Item> dropItem;
    private final int minCount;
    private final int maxCount;
    private final int miningLevel;

    public BaseOreBlock(Supplier<Item> drop, int minCount, int maxCount, int experience) {
        this(drop, minCount, maxCount, experience, Tiers.STONE.getLevel());
    }

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

    public BaseOreBlock(Properties properties, Supplier<Item> drop, int minCount, int maxCount, int experience) {
        this(properties, drop, minCount, maxCount, experience, 0);
    }

    public BaseOreBlock(
            Properties properties,
            Supplier<Item> drop,
            int minCount,
            int maxCount,
            int experience,
            int miningLevel
    ) {
        super(properties, UniformInt.of(experience > 0 ? 1 : 0, experience));
        this.dropItem = drop;
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.miningLevel = miningLevel;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return LootUtil
                .getDrops(this, state, builder)
                .orElseGet(
                        () -> BaseOreBlock.getDroppedItems(
                                this,
                                dropItem.get(),
                                maxCount,
                                minCount,
                                builder
                        )
                );
    }

    public static List<ItemStack> getDroppedItems(
            ItemLike block,
            Item dropItem,
            int maxCount,
            int minCount,
            LootParams.Builder builder
    ) {
        if (dropItem == null) {
            return Collections.emptyList();
        }
        ItemStack tool = builder.getParameter(LootContextParams.TOOL);
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0) {
            return Collections.singletonList(new ItemStack(block));
        }
        int count;
        int enchantment = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
        if (enchantment > 0) {
            int min = Mth.clamp(minCount + enchantment, minCount, maxCount);
            int max = maxCount + (enchantment / Enchantments.BLOCK_FORTUNE.getMaxLevel());
            if (min == max) {
                return Collections.singletonList(new ItemStack(dropItem, max));
            }
            count = MHelper.randRange(min, max, MHelper.RANDOM_SOURCE);
        } else {
            count = MHelper.randRange(minCount, maxCount, MHelper.RANDOM_SOURCE);
        }
        return Collections.singletonList(new ItemStack(dropItem, count));
    }

    /**
     * @deprecated Use {@link #getDroppedItems(ItemLike, Item, int, int, Builder)} and block tags for the mining level instead.
     */
    @Deprecated(forRemoval = true)
    public static List<ItemStack> getDroppedItems(
            ItemLike block,
            Item dropItem,
            int maxCount,
            int minCount,
            int miningLevel,
            BlockState state,
            LootParams.Builder builder
    ) {
        ItemStack tool = builder.getParameter(LootContextParams.TOOL);
        if (!tool.isCorrectToolForDrops(state)) {
            return Collections.emptyList();
        }
        boolean canMine = miningLevel == 0;
        if (tool.getItem() instanceof TieredItem tired) {
            canMine = tired.getTier().getLevel() >= miningLevel;
        }
        if (!canMine) {
            return Collections.emptyList();
        }
        return getDroppedItems(block, dropItem, maxCount, minCount, builder);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public BlockModel getItemModel(ResourceLocation resourceLocation) {
        return getBlockModel(resourceLocation, defaultBlockState());
    }

    @Override
    public void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
        blockTags.add(MiningLevelManager.getBlockTag(this.miningLevel));
    }
}
