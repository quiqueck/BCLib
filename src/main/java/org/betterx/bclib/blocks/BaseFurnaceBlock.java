package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.blockentities.BaseFurnaceBlockEntity;
import org.betterx.bclib.client.models.BCLModels;
import org.betterx.bclib.client.render.BCLRenderLayer;
import org.betterx.bclib.interfaces.RenderLayerProvider;
import org.betterx.bclib.registry.BaseBlockEntities;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.google.common.collect.Lists;

import java.util.List;
import org.jetbrains.annotations.Nullable;

public abstract class BaseFurnaceBlock extends FurnaceBlock implements RenderLayerProvider, BlockModelProvider {
    public BaseFurnaceBlock(Block source) {
        this(Properties.ofFullCopy(source).lightLevel(state -> state.getValue(LIT) ? 13 : 0));
    }

    public BaseFurnaceBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BaseFurnaceBlockEntity(blockPos, blockState);
    }

    @Override
    protected void openContainer(Level world, BlockPos pos, Player player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof BaseFurnaceBlockEntity) {
            player.openMenu((MenuProvider) blockEntity);
            player.awardStat(Stats.INTERACT_WITH_FURNACE);
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void provideBlockModels(WoverBlockModelGenerators generator) {
        final var baseTexture = TextureMapping.getBlockTexture(this);
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.TOP, baseTexture.withSuffix("_top"))
                .put(TextureSlot.SIDE, baseTexture.withSuffix("_side"))
                .put(TextureSlot.FRONT, baseTexture.withSuffix("_front"))
                .put(TextureSlot.BOTTOM, baseTexture.withSuffix("_top"));
        final var furnaceModel = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(this, mapping, generator.modelOutput());

        TextureMapping mappingGlow = new TextureMapping()
                .put(TextureSlot.TOP, baseTexture.withSuffix("_top"))
                .put(TextureSlot.SIDE, baseTexture.withSuffix("_side"))
                .put(TextureSlot.FRONT, baseTexture.withSuffix("_front_on"))
                .put(TextureSlot.BOTTOM, baseTexture.withSuffix("_top"))
                .put(BCLModels.GLOW, baseTexture.withSuffix("_glow"));
        final var glowModel = BCLModels.FURNACE_GLOW.createWithSuffix(this, "_lit", mappingGlow, generator.modelOutput());

        final var prop = PropertyDispatch.properties(LIT, FACING);
        addRotationModels(prop, furnaceModel, false);
        addRotationModels(prop, glowModel, true);

        generator.acceptBlockState(MultiVariantGenerator.multiVariant(this).with(prop));
    }

    @Environment(EnvType.CLIENT)
    private static void addRotationModels(
            PropertyDispatch.C2<Boolean, Direction> prop,
            ResourceLocation furnaceModel,
            boolean lit
    ) {
        prop.select(lit, Direction.EAST,
                Variant.variant()
                       .with(VariantProperties.MODEL, furnaceModel)
                       .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
        );
        prop.select(lit, Direction.SOUTH,
                Variant.variant()
                       .with(VariantProperties.MODEL, furnaceModel)
                       .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
        );
        prop.select(lit, Direction.WEST,
                Variant.variant()
                       .with(VariantProperties.MODEL, furnaceModel)
                       .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
        );
        prop.select(lit, Direction.NORTH,
                Variant.variant()
                       .with(VariantProperties.MODEL, furnaceModel)
        );
    }

    @Override
    public BCLRenderLayer getRenderLayer() {
        return BCLRenderLayer.CUTOUT;
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drop = Lists.newArrayList(new ItemStack(this));
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof BaseFurnaceBlockEntity) {
            BaseFurnaceBlockEntity entity = (BaseFurnaceBlockEntity) blockEntity;
            for (int i = 0; i < entity.getContainerSize(); i++) {
                drop.add(entity.getItem(i));
            }
        }
        return drop;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState blockState,
            BlockEntityType<T> blockEntityType
    ) {
        return createFurnaceTicker(level, blockEntityType, BaseBlockEntities.FURNACE);
    }

    @Nullable
    protected static <T extends BlockEntity> BlockEntityTicker<T> createFurnaceTicker(
            Level level,
            BlockEntityType<T> blockEntityType,
            BlockEntityType<? extends AbstractFurnaceBlockEntity> blockEntityType2
    ) {
        return level.isClientSide ? null : createTickerHelper(
                blockEntityType,
                blockEntityType2,
                AbstractFurnaceBlockEntity::serverTick
        );
    }

    public static class Stone extends BaseFurnaceBlock implements BehaviourStone {
        public Stone(Block source) {
            super(source);
        }

        public Stone(BlockBehaviour.Properties properties) {
            super(properties);
        }
    }
}
