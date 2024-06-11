package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.bclib.interfaces.BlockModelProvider;
import org.betterx.wover.loot.api.BlockLootProvider;
import org.betterx.wover.loot.api.LootLookupProvider;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BasePathBlock extends BaseBlockNotFull implements BlockLootProvider {
    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 15, 16);

    private Block baseBlock;

    public BasePathBlock(Block source) {
        super(Properties.ofFullCopy(source).isValidSpawn((state, world, pos, type) -> false));
        this.baseBlock = source;
        if (source instanceof BaseTerrainBlock terrain) {
            this.baseBlock = terrain.getBaseBlock();
            terrain.setPathBlock(this);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getCollisionShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        return SHAPE;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public BlockModel getItemModel(ResourceLocation blockId) {
        return getBlockModel(blockId, defaultBlockState());
    }

    @Override
    @Environment(EnvType.CLIENT)
    public @Nullable BlockModel getBlockModel(ResourceLocation blockId, BlockState blockState) {
        String name = blockId.getPath();
        ResourceLocation bottomId = BuiltInRegistries.BLOCK.getKey(baseBlock);
        String bottom = bottomId.getNamespace() + ":block/" + bottomId.getPath();
        Map<String, String> textures = Maps.newHashMap();
        textures.put("%modid%", blockId.getNamespace());
        textures.put("%top%", name + "_top");
        textures.put("%side%", name.replace("_path", "") + "_side");
        textures.put("%bottom%", bottom);
        Optional<String> pattern = PatternsHelper.createJson(BasePatterns.BLOCK_PATH, textures);
        return ModelsHelper.fromPattern(pattern);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public UnbakedModel getModelVariant(
            ModelResourceLocation stateId,
            BlockState blockState,
            Map<ResourceLocation, UnbakedModel> modelCache
    ) {
        ModelResourceLocation modelId = BlockModelProvider.remapModelResourceLocation(stateId, blockState);
        registerBlockModel(stateId, modelId, blockState, modelCache);
        return ModelsHelper.createRandomTopModel(modelId.id());
    }

    @Override
    public LootTable.Builder registerBlockLoot(
            @NotNull ResourceLocation location,
            @NotNull LootLookupProvider provider,
            @NotNull ResourceKey<LootTable> tableKey
    ) {
        return provider.dropWithSilkTouch(this, this.baseBlock, ConstantValue.exactly(1));
    }

    public static class Stone extends BasePathBlock implements BehaviourStone {
        public Stone(Block source) {
            super(source);
        }
    }
}
