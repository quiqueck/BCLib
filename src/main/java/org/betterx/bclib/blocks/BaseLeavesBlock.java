package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.interfaces.BehaviourLeaves;
import org.betterx.bclib.client.render.BCLRenderLayer;
import org.betterx.bclib.interfaces.RenderLayerProvider;
import org.betterx.bclib.interfaces.RuntimeBlockModelProvider;
import org.betterx.wover.loot.api.BlockLootProvider;
import org.betterx.wover.loot.api.LootLookupProvider;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootTable;

import org.jetbrains.annotations.NotNull;

public class BaseLeavesBlock extends TintedParticleLeavesBlock implements RuntimeBlockModelProvider, RenderLayerProvider, BehaviourLeaves, BlockLootProvider {
    protected final Block sapling;

    public BaseLeavesBlock(
            Block sapling,
            float particleChance,
            BlockBehaviour.Properties properties
    ) {
        super(particleChance, properties);
        this.sapling = sapling;
    }

    public BaseLeavesBlock(
            Block sapling,
            BlockBehaviour.Properties properties
    ) {
        this(sapling, 0.01F, properties);
    }

    @Override
    public BCLRenderLayer getRenderLayer() {
        return BCLRenderLayer.CUTOUT;
    }

    @Override
    public BlockModel getItemModel(ResourceLocation resourceLocation) {
        return getBlockModel(resourceLocation, defaultBlockState());
    }

    @Override
    public float compostingChance() {
        return 0.3f;
    }

    @Override
    public LootTable.Builder registerBlockLoot(
            @NotNull ResourceLocation location,
            @NotNull LootLookupProvider provider,
            @NotNull ResourceKey<LootTable> tableKey
    ) {
        return provider.dropLeaves(this, this.sapling);
    }
}
