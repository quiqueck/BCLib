package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.BehaviourBuilders;
import org.betterx.bclib.behaviours.interfaces.BehaviourLeaves;
import org.betterx.bclib.client.render.BCLRenderLayer;
import org.betterx.bclib.interfaces.BlockModelProvider;
import org.betterx.bclib.interfaces.RenderLayerProvider;
import org.betterx.bclib.items.tool.BaseShearsItem;
import org.betterx.bclib.util.MHelper;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class BaseLeavesBlock extends LeavesBlock implements BlockModelProvider, RenderLayerProvider, BehaviourLeaves {
    protected final Block sapling;

    public BaseLeavesBlock(
            Block sapling,
            BlockBehaviour.Properties properties
    ) {
        super(properties);
        this.sapling = sapling;
    }

    @Deprecated(forRemoval = true)
    public BaseLeavesBlock(
            Block sapling,
            MapColor color,
            Consumer<BlockBehaviour.Properties> customizeProperties
    ) {
        super(BaseBlock.acceptAndReturn(customizeProperties, BehaviourBuilders.createLeaves(color, true)));
        this.sapling = sapling;
    }

    @Deprecated(forRemoval = true)
    public BaseLeavesBlock(
            Block sapling,
            MapColor color,
            int light,
            Consumer<BlockBehaviour.Properties> customizeProperties
    ) {
        super(BaseBlock.acceptAndReturn(
                customizeProperties,
                BehaviourBuilders.createLeaves(color, true).lightLevel(state -> light)
        ));
        this.sapling = sapling;
    }

    @Deprecated(forRemoval = true)
    public BaseLeavesBlock(Block sapling, MapColor color) {
        super(BehaviourBuilders.createLeaves(color, true));
        this.sapling = sapling;
    }

    @Deprecated(forRemoval = true)
    public BaseLeavesBlock(Block sapling, MapColor color, int light) {
        super(BehaviourBuilders.createLeaves(color, true).lightLevel(state -> light));
        this.sapling = sapling;
    }

    @Override
    public BCLRenderLayer getRenderLayer() {
        return BCLRenderLayer.CUTOUT;
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return BaseLeavesBlock.getLeaveDrops(this, this.sapling, builder, 16, 16);
    }

    public static List<ItemStack> getLeaveDrops(
            ItemLike leaveBlock,
            Block sapling,
            LootParams.Builder builder,
            int fortuneRate,
            int dropRate
    ) {
        ItemStack tool = builder.getParameter(LootContextParams.TOOL);
        if (tool != null) {
            if (tool != null && BaseShearsItem.isShear(tool) || EnchantmentHelper.getItemEnchantmentLevel(
                    Enchantments.SILK_TOUCH,
                    tool
            ) > 0) {
                return Collections.singletonList(new ItemStack(leaveBlock));
            }
            int fortune = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
            if (MHelper.RANDOM.nextInt(fortuneRate) <= fortune) {
                return Lists.newArrayList(new ItemStack(sapling));
            }
            return Lists.newArrayList();
        }
        return MHelper.RANDOM.nextInt(dropRate) == 0
                ? Lists.newArrayList(new ItemStack(sapling))
                : Lists.newArrayList();
    }

    @Override
    public BlockModel getItemModel(ResourceLocation resourceLocation) {
        return getBlockModel(resourceLocation, defaultBlockState());
    }


    @Override
    public float compostingChance() {
        return 0.3f;
    }
}
