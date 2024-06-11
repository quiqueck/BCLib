package org.betterx.bclib.items;

import org.betterx.bclib.blocks.BaseAnvilBlock;
import org.betterx.bclib.interfaces.ItemModelProvider;
import org.betterx.bclib.interfaces.RuntimeBlockModelProvider;
import org.betterx.bclib.util.BCLDataComponents;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import java.util.Locale;

public class BaseAnvilItem extends BlockItem implements ItemModelProvider {
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
        int destruction = anvilData.contains(DESTRUCTION) ? anvilData.getUnsafe().getInt(DESTRUCTION) : 0;
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
            List<Component> list,
            TooltipFlag tooltipFlag
    ) {
        CustomData anvilData = itemStack.getOrDefault(BCLDataComponents.ANVIL_ENTITY_DATA, CustomData.EMPTY);
        if (!anvilData.contains(DESTRUCTION)) return;

        @SuppressWarnings("deprecation")
        int destruction = anvilData.getUnsafe().getInt(DESTRUCTION);
        if (destruction > 0) {
            BaseAnvilBlock block = (BaseAnvilBlock) ((BaseAnvilItem) itemStack.getItem()).getBlock();
            int maxValue = block.getMaxDurability() * 3;
            float damage = maxValue - destruction;
            String percents = String.format(Locale.ROOT, "%.0f%%", damage);
            list.add(Component.translatable("message.bclib.anvil_damage").append(": " + percents));
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public BlockModel getItemModel(ResourceLocation resourceLocation) {
        Block anvilBlock = getBlock();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(anvilBlock);
        return ((RuntimeBlockModelProvider) anvilBlock).getBlockModel(blockId, anvilBlock.defaultBlockState());
    }
}
