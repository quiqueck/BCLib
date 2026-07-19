package org.betterx.bclib.items;

import org.betterx.bclib.blocks.BaseAnvilBlock;
import org.betterx.bclib.util.BCLDataComponents;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.Locale;
import java.util.function.Consumer;

public class BaseAnvilItem extends BlockItem {
    public final static String DESTRUCTION = "destruction";

    public BaseAnvilItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected BlockState getPlacementState(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = super.getPlacementState(blockPlaceContext);
        ItemStack stack = blockPlaceContext.getItemInHand();
        CustomData anvilData = stack.getOrDefault(BCLDataComponents.ANVIL_ENTITY_DATA, CustomData.EMPTY);

        @SuppressWarnings("deprecation")

        int destruction = anvilData.getUnsafe().getInt(DESTRUCTION).orElse(0);
        if (blockState != null) {
            BaseAnvilBlock block = (BaseAnvilBlock) blockState.getBlock();
            IntegerProperty durabilityProp = block.getDurabilityProp();
            if (destruction == 0) {
                blockState = blockState.setValue(durabilityProp, 0).setValue(BaseAnvilBlock.DESTRUCTION, 0);
            } else {
                int destructionValue = destruction / block.getMaxDurability();
                int durabilityValue = destruction - destructionValue * block.getMaxDurability();
                blockState = blockState.setValue(durabilityProp, durabilityValue)
                                       .setValue(BaseAnvilBlock.DESTRUCTION, destructionValue);
            }
        }

        return blockState;
    }

    @Override
    public void appendHoverText(
            ItemStack itemStack,
            TooltipContext tooltipContext,
            TooltipDisplay tooltipDisplay,
            Consumer<Component> consumer,
            TooltipFlag tooltipFlag
    ) {
        CustomData anvilData = itemStack.getOrDefault(BCLDataComponents.ANVIL_ENTITY_DATA, CustomData.EMPTY);
        if (!anvilData.contains(DESTRUCTION)) return;

        @SuppressWarnings("deprecation")
        int destruction = anvilData.getUnsafe().getInt(DESTRUCTION).orElse(0);
        if (destruction > 0) {
            BaseAnvilBlock block = (BaseAnvilBlock) ((BaseAnvilItem) itemStack.getItem()).getBlock();
            int maxValue = block.getMaxDurability() * 3;
            float damage = maxValue - destruction;
            String percents = String.format(Locale.ROOT, "%.0f%%", damage);
            consumer.accept(Component.translatable("message.bclib.anvil_damage").append(": " + percents));
        }
    }
}
