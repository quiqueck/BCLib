package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.bclib.interfaces.TagProvider;
import org.betterx.worlds.together.tag.v3.CommonBlockTags;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public abstract class BaseBookshelfBlock extends BaseBlock implements TagProvider {
    protected BaseBookshelfBlock(Block source) {
        this(Properties.copy(source));
    }

    protected BaseBookshelfBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        ItemStack tool = builder.getParameter(LootContextParams.TOOL);
        if (tool != null) {
            int silk = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool);
            if (silk > 0) {
                return Collections.singletonList(new ItemStack(this));
            }
        }
        return Collections.singletonList(new ItemStack(Items.BOOK, 3));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public @Nullable BlockModel getBlockModel(ResourceLocation blockId, BlockState blockState) {
        Optional<String> pattern = PatternsHelper.createJson(BasePatterns.BLOCK_BOOKSHELF, replacePath(blockId));
        return ModelsHelper.fromPattern(pattern);
    }

    protected ResourceLocation replacePath(ResourceLocation blockId) {
        String newPath = blockId.getPath().replace("_bookshelf", "");
        return new ResourceLocation(blockId.getNamespace(), newPath);
    }

    @Override
    public void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
        blockTags.add(CommonBlockTags.BOOKSHELVES);
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
