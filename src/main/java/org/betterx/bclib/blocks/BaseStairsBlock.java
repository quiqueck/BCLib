package org.betterx.bclib.blocks;

import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.bclib.interfaces.BlockModelProvider;
import org.betterx.bclib.interfaces.CustomItemProvider;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.storage.loot.LootContext;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public class BaseStairsBlock extends StairBlock implements BlockModelProvider, CustomItemProvider {
    private final Block parent;
    public final boolean fireproof;

    public BaseStairsBlock(Block source) {
        this(source, false);
    }

    public BaseStairsBlock(Block source, boolean fireproof) {
        super(source.defaultBlockState(), Properties.copy(source));
        this.parent = source;
        this.fireproof = fireproof;
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        return Collections.singletonList(new ItemStack(this));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public BlockModel getItemModel(ResourceLocation resourceLocation) {
        return getBlockModel(resourceLocation, defaultBlockState());
    }

    @Override
    @Environment(EnvType.CLIENT)
    public @Nullable BlockModel getBlockModel(ResourceLocation blockId, BlockState blockState) {
        ResourceLocation parentId = BuiltInRegistries.BLOCK.getKey(parent);
        Optional<String> pattern = PatternsHelper.createJson(switch (blockState.getValue(SHAPE)) {
            case STRAIGHT -> BasePatterns.BLOCK_STAIR;
            case INNER_LEFT, INNER_RIGHT -> BasePatterns.BLOCK_STAIR_INNER;
            case OUTER_LEFT, OUTER_RIGHT -> BasePatterns.BLOCK_STAIR_OUTER;
        }, parentId);
        return ModelsHelper.fromPattern(pattern);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public UnbakedModel getModelVariant(
            ResourceLocation stateId,
            BlockState blockState,
            Map<ResourceLocation, UnbakedModel> modelCache
    ) {
        String state;
        StairsShape shape = blockState.getValue(SHAPE);
        state = switch (shape) {
            case INNER_LEFT, INNER_RIGHT -> "_inner";
            case OUTER_LEFT, OUTER_RIGHT -> "_outer";
            default -> "";
        };
        ResourceLocation modelId = new ResourceLocation(stateId.getNamespace(), "block/" + stateId.getPath() + state);
        registerBlockModel(stateId, modelId, blockState, modelCache);

        boolean isTop = blockState.getValue(HALF) == Half.TOP;
        boolean isLeft = shape == StairsShape.INNER_LEFT || shape == StairsShape.OUTER_LEFT;
        boolean isRight = shape == StairsShape.INNER_RIGHT || shape == StairsShape.OUTER_RIGHT;
        int y = 0;
        int x = isTop ? 180 : 0;
        switch (blockState.getValue(FACING)) {
            case NORTH:
                if (isTop && !isRight) y = 270;
                else if (!isTop) y = isLeft ? 180 : 270;
                break;
            case EAST:
                if (isTop && isRight) y = 90;
                else if (!isTop && isLeft) y = 270;
                break;
            case SOUTH:
                if (isTop) y = isRight ? 180 : 90;
                else if (!isLeft) y = 90;
                break;
            case WEST:
            default:
                y = (isTop && isRight) ? 270 : (!isTop && isLeft) ? 90 : 180;
                break;
        }
        BlockModelRotation rotation = BlockModelRotation.by(x, y);
        return ModelsHelper.createMultiVariant(modelId, rotation.getRotation(), true);
    }

    @Override
    public BlockItem getCustomItem(ResourceLocation blockID, Item.Properties settings) {
        if (fireproof) settings = settings.fireResistant();
        return new BlockItem(this, settings);
    }
}
