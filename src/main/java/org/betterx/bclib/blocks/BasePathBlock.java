package org.betterx.bclib.blocks;

import org.betterx.bclib.client.models.BCLModels;
import org.betterx.bclib.interfaces.tools.AddMineablePickaxe;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;
import org.betterx.wover.loot.api.BlockLootProvider;
import org.betterx.wover.loot.api.LootLookupProvider;

import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jetbrains.annotations.NotNull;

/**
 * The block model is no longer provided implicitly - register a
 * {@code ClientBlockTraits.MODEL.with((key, block, generator) -> BasePathBlock.provideBlockModel(generator, (BasePathBlock) block))}
 * trait (see {@link #provideBlockModel}) at the registration site of any block that needs one.
 */
public abstract class BasePathBlock extends BaseBlockNotFull implements BlockLootProvider {
    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 15, 16);

    private Block baseBlock;

    public BasePathBlock(BlockBehaviour.Properties props, Block source) {
        super(Properties.ofFullCopy(source).isValidSpawn((state, world, pos, type) -> false));
        this.baseBlock = source;
        if (source instanceof BaseTerrainBlock terrain) {
            this.baseBlock = terrain.getBaseBlock();
            terrain.setPathBlock(this);
        }
    }

    public Block getBaseBlock() {
        return baseBlock;
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

    /**
     * Generates the randomly-rotated top-variant blockstate/model for a path block, using {@code baseBlock}'s
     * texture for the bottom face and the path block's own {@code _top}/{@code _side} textures otherwise.
     *
     * @param generator The generator helper to emit the blockstate/model through
     * @param pathBlock The path block to generate the model for
     */
    @Environment(EnvType.CLIENT)
    public static void provideBlockModel(WoverBlockModelGenerators generator, BasePathBlock pathBlock) {
        var side = TextureMapping.getBlockTexture(pathBlock, "_side");
        side = ResourceLocation.fromNamespaceAndPath(
                side.getNamespace(), side
                        .getPath()
                        .replace("_path", "")
        );

        var mapping = new TextureMapping()
                .put(TextureSlot.SIDE, side)
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(pathBlock, "_top"))
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(pathBlock.getBaseBlock()));
        var location = BCLModels.PATH.create(pathBlock, mapping, generator.modelOutput());

        generator.acceptBlockState(generator.randomTopModelVariant(pathBlock, location));
    }

    @Override
    public LootTable.Builder registerBlockLoot(
            @NotNull ResourceLocation location,
            @NotNull LootLookupProvider provider,
            @NotNull ResourceKey<LootTable> tableKey
    ) {
        return provider.dropWithSilkTouch(this, this.baseBlock, ConstantValue.exactly(1));
    }

    public static class Stone extends BasePathBlock implements AddMineablePickaxe {
        public Stone(BlockBehaviour.Properties props, Block source) {
            super(props, source);
        }
    }
}
