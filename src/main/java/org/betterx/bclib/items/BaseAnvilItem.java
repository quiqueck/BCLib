package org.betterx.bclib.items;

import org.betterx.bclib.blocks.BaseAnvilBlock;
import org.betterx.bclib.interfaces.BlockModelProvider;
import org.betterx.bclib.interfaces.ItemModelProvider;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public class BaseAnvilItem extends BlockItem implements ItemModelProvider {
    public final static String DESTRUCTION = "destruction";
    public final static String DURABILITY = "durability";

    public BaseAnvilItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected BlockState getPlacementState(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = super.getPlacementState(blockPlaceContext);
        if (blockState != null) {
            CompoundTag tag = blockPlaceContext.getItemInHand().getTag();
            blockState = applyWear(blockState, tag == null ? new CompoundTag() : tag);
        }
        return blockState;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(
            ItemStack itemStack,
            @Nullable Level level,
            List<Component> list,
            TooltipFlag tooltipFlag
    ) {
        CompoundTag tag = itemStack.getTag();
        if (tag == null || !tag.contains(DESTRUCTION)) return;

        BaseAnvilBlock block = (BaseAnvilBlock) ((BaseAnvilItem) itemStack.getItem()).getBlock();
        BlockState state = applyWear(block.defaultBlockState(), tag);
        // durability counts 0..max inclusive, so every destruction stage spans max + 1 uses
        int stageLength = block.getMaxDurability() + 1;
        int used = state.getValue(BaseAnvilBlock.DESTRUCTION) * stageLength + state.getValue(block.getDurabilityProp());
        if (used > 0) {
            float damage = used * 100f / (3 * stageLength);
            String percents = String.format(Locale.ROOT, "%.0f%%", damage);
            list.add(Component.translatable("message.bclib.anvil_damage").append(": " + percents));
        }
    }

    /**
     * Applies the wear stored by {@link BaseAnvilBlock#getDrops} to {@code blockState}. Current drops store
     * the destruction stage and the durability as separate keys; older drops only carry a single combined
     * {@code destruction * max + durability} value.
     */
    private static BlockState applyWear(BlockState blockState, CompoundTag tag) {
        BaseAnvilBlock block = (BaseAnvilBlock) blockState.getBlock();
        IntegerProperty durabilityProp = block.getDurabilityProp();
        int maxDurability = block.getMaxDurability();
        int destructionValue;
        int durabilityValue;
        if (tag.contains(DURABILITY)) {
            destructionValue = Mth.clamp(tag.getInt(DESTRUCTION), 0, 2);
            durabilityValue = Mth.clamp(tag.getInt(DURABILITY), 0, maxDurability);
        } else {
            // The legacy combined value is ambiguous, since durability itself ranges 0..max: a fully worn
            // anvil (destruction 2, durability max) encodes as 3 * max. Clamp both parts to their property
            // ranges instead of decoding a destruction stage of 3.
            int destruction = tag.getInt(DESTRUCTION);
            destructionValue = Mth.clamp(destruction / maxDurability, 0, 2);
            durabilityValue = Mth.clamp(destruction - destructionValue * maxDurability, 0, maxDurability);
        }
        return blockState.setValue(durabilityProp, durabilityValue)
                         .setValue(BaseAnvilBlock.DESTRUCTION, destructionValue);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public BlockModel getItemModel(ResourceLocation resourceLocation) {
        Block anvilBlock = getBlock();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(anvilBlock);
        return ((BlockModelProvider) anvilBlock).getBlockModel(blockId, anvilBlock.defaultBlockState());
    }
}
