package org.betterx.bclib.blocks;

import org.betterx.bclib.blockentities.BaseFurnaceBlockEntity;
import org.betterx.bclib.client.models.BCLModels;
import org.betterx.bclib.registry.BaseBlockEntities;
import de.ambertation.wover.block.api.client.trait.BlockModelTrait;
import de.ambertation.wover.block.api.client.trait.ClientBlockTraits;
import de.ambertation.wover.block.api.trait.BlockTraitLookup;
import de.ambertation.wover.core.api.ModCore;
import de.ambertation.wover.sets.api.blocks.BlockSet;

import static net.minecraft.client.data.models.BlockModelGenerators.*;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BaseFurnaceBlock extends FurnaceBlock {
    public BaseFurnaceBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
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
    private static void addRotationModels(
            PropertyDispatch.C2<MultiVariant, Boolean, Direction> prop,
            ResourceLocation furnaceModel,
            boolean lit
    ) {
        var modelVariant = plainVariant(furnaceModel);
        prop.select(
                lit, Direction.EAST,
                modelVariant.with(Y_ROT_90)
        );
        prop.select(
                lit, Direction.SOUTH,
                modelVariant.with(Y_ROT_180)
        );
        prop.select(
                lit, Direction.WEST,
                modelVariant.with(Y_ROT_270)
        );
        prop.select(
                lit, Direction.NORTH,
                modelVariant
        );
    }

    /**
     * Kept in a separate class file (not just an @Environment(CLIENT) method) since
     * BaseFurnaceBlock is always loaded on the server; a lambda body's synthetic method does not
     * inherit the annotation from its enclosing method, so leaving it here would strand vanilla
     * client-only type references in a class file the server actually has to verify.
     */
    public static BlockModelTrait buildModel(BlockSet<?> set, BlockTraitLookup traitLookup) {
        return ModCore.isDatagen() ? ClientModel.build() : null;
    }

    @Environment(EnvType.CLIENT)
    private static class ClientModel {
        private static BlockModelTrait build() {
            return ClientBlockTraits.MODEL.with(
                    (key, block, generator) -> {
                        final var baseTexture = TextureMapping.getBlockTexture(block);
                        TextureMapping mapping = new TextureMapping()
                                .put(TextureSlot.TOP, baseTexture.withSuffix("_top"))
                                .put(TextureSlot.SIDE, baseTexture.withSuffix("_side"))
                                .put(TextureSlot.FRONT, baseTexture.withSuffix("_front"))
                                .put(TextureSlot.BOTTOM, baseTexture.withSuffix("_top"));
                        final var furnaceModel = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(
                                block,
                                mapping,
                                generator.modelOutput()
                        );

                        TextureMapping mappingGlow = new TextureMapping()
                                .put(TextureSlot.TOP, baseTexture.withSuffix("_top"))
                                .put(TextureSlot.SIDE, baseTexture.withSuffix("_side"))
                                .put(TextureSlot.FRONT, baseTexture.withSuffix("_front_on"))
                                .put(TextureSlot.BOTTOM, baseTexture.withSuffix("_top"))
                                .put(BCLModels.GLOW, baseTexture.withSuffix("_glow"));
                        final var glowModel = BCLModels.FURNACE_GLOW.createWithSuffix(
                                block,
                                "_lit",
                                mappingGlow,
                                generator.modelOutput()
                        );

                        final var prop = PropertyDispatch.initial(LIT, FACING);
                        addRotationModels(prop, furnaceModel, false);
                        addRotationModels(prop, glowModel, true);

                        generator.acceptBlockState(MultiVariantGenerator.dispatch(block).with(prop));
                        generator.delegateItemModel(block, furnaceModel);
                    });
        }
    }

    @Override
    public @NotNull List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drop = Lists.newArrayList(new ItemStack(this));
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof BaseFurnaceBlockEntity entity) {
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
        if (level instanceof ServerLevel) {
            return createTickerHelper(
                    blockEntityType,
                    blockEntityType2,
                    (tickLevel, pos, state, furnaceBlockEntity) -> {
                        if (tickLevel instanceof ServerLevel serverTickLevel) {
                            AbstractFurnaceBlockEntity.serverTick(
                                    serverTickLevel,
                                    pos, state,
                                    furnaceBlockEntity
                            );
                        }
                    }
            );
        }
        return null;
    }
}
