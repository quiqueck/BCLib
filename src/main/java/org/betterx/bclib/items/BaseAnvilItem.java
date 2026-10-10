package org.betterx.bclib.items;

import org.betterx.bclib.blocks.LeveledAnvilBlock;
import org.betterx.bclib.util.BCLDataComponents;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
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
    public final static String DURABILITY = "durability";

    public BaseAnvilItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected BlockState getPlacementState(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = super.getPlacementState(blockPlaceContext);
        ItemStack stack = blockPlaceContext.getItemInHand();
        CustomData anvilData = stack.getOrDefault(BCLDataComponents.ANVIL_ENTITY_DATA, CustomData.EMPTY);

        if (blockState != null) {
            @SuppressWarnings("deprecation")
            CompoundTag tag = anvilData.copyTag();
            blockState = applyWear(blockState, tag);
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
        @SuppressWarnings("deprecation")
        CompoundTag tag = anvilData.copyTag();
        if (!tag.contains(DESTRUCTION)) return;

        LeveledAnvilBlock block = (LeveledAnvilBlock) ((BaseAnvilItem) itemStack.getItem()).getBlock();
        BlockState state = applyWear(block.defaultBlockState(), tag);
        // durability counts 0..max inclusive, so every destruction stage spans max + 1 uses
        int stageLength = block.getMaxDurability() + 1;
        int used = state.getValue(LeveledAnvilBlock.DESTRUCTION) * stageLength + state.getValue(block.getDurabilityProp());
        if (used > 0) {
            float damage = used * 100f / (3 * stageLength);
            String percents = String.format(Locale.ROOT, "%.0f%%", damage);
            consumer.accept(Component.translatable("message.bclib.anvil_damage").append(": " + percents));
        }
    }

    /**
     * Applies the wear stored by {@link LeveledAnvilBlock#getDrops} to {@code blockState}. Current drops store
     * the destruction stage and the durability as separate keys; older drops only carry a single combined
     * {@code destruction * max + durability} value.
     */
    private static BlockState applyWear(BlockState blockState, CompoundTag tag) {
        LeveledAnvilBlock block = (LeveledAnvilBlock) blockState.getBlock();
        IntegerProperty durabilityProp = block.getDurabilityProp();
        int maxDurability = block.getMaxDurability();
        int destructionValue;
        int durabilityValue;
        if (tag.contains(DURABILITY)) {
            destructionValue = Mth.clamp(tag.getIntOr(DESTRUCTION, 0), 0, 2);
            durabilityValue = Mth.clamp(tag.getIntOr(DURABILITY, 0), 0, maxDurability);
        } else {
            // The legacy combined value is ambiguous, since durability itself ranges 0..max: a fully worn
            // anvil (destruction 2, durability max) encodes as 3 * max. Clamp both parts to their property
            // ranges instead of decoding a destruction stage of 3.
            int destruction = tag.getIntOr(DESTRUCTION, 0);
            destructionValue = Mth.clamp(destruction / maxDurability, 0, 2);
            durabilityValue = Mth.clamp(destruction - destructionValue * maxDurability, 0, maxDurability);
        }
        return blockState.setValue(durabilityProp, durabilityValue)
                         .setValue(LeveledAnvilBlock.DESTRUCTION, destructionValue);
    }
}
