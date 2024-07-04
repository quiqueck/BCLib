package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.client.models.BCLModels;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;
import org.betterx.wover.loot.api.BlockLootProvider;
import org.betterx.wover.loot.api.LootLookupProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
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

import org.jetbrains.annotations.NotNull;

public abstract class BasePathBlock extends BaseBlockNotFull implements BlockLootProvider, BlockModelProvider {
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
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        return SHAPE;
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter view,
            BlockPos pos,
            CollisionContext ePos
    ) {
        return SHAPE;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void provideBlockModels(WoverBlockModelGenerators generator) {
        var side = TextureMapping.getBlockTexture(this, "_side");
        side = ResourceLocation.fromNamespaceAndPath(side.getNamespace(), side
                .getPath()
                .replace("_path", ""));

        var mapping = new TextureMapping()
                .put(TextureSlot.SIDE, side)
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(this, "_top"))
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(baseBlock));
        var location = BCLModels.PATH.create(this, mapping, generator.modelOutput());

        generator.acceptBlockState(generator.randomTopModelVariant(this, location));

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
