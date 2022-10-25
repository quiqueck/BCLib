package org.betterx.bclib.blocks;

import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.bclib.interfaces.BlockModelProvider;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.storage.loot.LootContext;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public abstract class BaseButtonBlock extends ButtonBlock implements BlockModelProvider {
    private final Block parent;

    protected BaseButtonBlock(Block parent, Properties properties, boolean sensitive) {
        this(parent, properties, 30, sensitive);
    }

    protected BaseButtonBlock(Block parent, Properties properties, int ticksToStayPressed, boolean sensitive) {
        super(
                properties.noCollission(), ticksToStayPressed, sensitive,
                SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON
        );
        this.parent = parent;
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        return Collections.singletonList(new ItemStack(this));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public BlockModel getItemModel(ResourceLocation blockId) {
        ResourceLocation parentId = Registry.BLOCK.getKey(parent);
        Optional<String> pattern = PatternsHelper.createJson(BasePatterns.ITEM_BUTTON, parentId);
        return ModelsHelper.fromPattern(pattern);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public @Nullable BlockModel getBlockModel(ResourceLocation resourceLocation, BlockState blockState) {
        ResourceLocation parentId = Registry.BLOCK.getKey(parent);
        Optional<String> pattern = blockState.getValue(POWERED)
                ? PatternsHelper.createJson(
                BasePatterns.BLOCK_BUTTON_PRESSED,
                parentId
        )
                : PatternsHelper.createJson(BasePatterns.BLOCK_BUTTON, parentId);
        return ModelsHelper.fromPattern(pattern);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public UnbakedModel getModelVariant(
            ResourceLocation stateId,
            BlockState blockState,
            Map<ResourceLocation, UnbakedModel> modelCache
    ) {
        String powered = blockState.getValue(POWERED) ? "_powered" : "";
        ResourceLocation modelId = new ResourceLocation(stateId.getNamespace(), "block/" + stateId.getPath() + powered);
        registerBlockModel(stateId, modelId, blockState, modelCache);
        AttachFace face = blockState.getValue(FACE);
        boolean isCeiling = face == AttachFace.CEILING;
        int x = 0, y = 0;
        switch (face) {
            case CEILING:
                x = 180;
                break;
            case WALL:
                x = 90;
                break;
            default:
                break;
        }
        switch (blockState.getValue(FACING)) {
            case NORTH:
                if (isCeiling) {
                    y = 180;
                }
                break;
            case EAST:
                y = isCeiling ? 270 : 90;
                break;
            case SOUTH:
                if (!isCeiling) {
                    y = 180;
                }
                break;
            case WEST:
                y = isCeiling ? 90 : 270;
                break;
            default:
                break;
        }
        BlockModelRotation rotation = BlockModelRotation.by(x, y);
        return ModelsHelper.createMultiVariant(modelId, rotation.getRotation(), face == AttachFace.WALL);
    }
}
