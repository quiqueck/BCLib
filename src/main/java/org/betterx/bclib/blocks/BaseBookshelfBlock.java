package org.betterx.bclib.blocks;

import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
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

public class BaseBookshelfBlock extends BaseBlock {
    public static class WithVanillaWood extends BaseBookshelfBlock {
        public WithVanillaWood(Block source) {
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

    public BaseBookshelfBlock(Block source) {
        this(Properties.copy(source));
    }

    public BaseBookshelfBlock(BlockBehaviour.Properties properties) {
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
}
