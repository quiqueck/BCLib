package ru.bclib.blocks;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import ru.bclib.client.models.BasePatterns;
import ru.bclib.client.models.ModelsHelper;
import ru.bclib.client.models.PatternsHelper;
import ru.bclib.interfaces.BlockModelProvider;
import ru.bclib.interfaces.LootProvider;

import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public abstract class BaseButtonBlock extends ButtonBlock implements BlockModelProvider, LootProvider {
	private final Block parent;

	protected BaseButtonBlock(Block parent, Properties properties, boolean sensitive) {
		super(sensitive, properties.noCollission());
		this.parent = parent;
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
	public UnbakedModel getModelVariant(ResourceLocation stateId,
										BlockState blockState,
										Map<ResourceLocation, UnbakedModel> modelCache) {
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
