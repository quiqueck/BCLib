package org.betterx.bclib.mixin.common;

import org.betterx.bclib.api.v2.BonemealAPI;
import org.betterx.bclib.api.v2.levelgen.biomes.BiomeAPI;
import org.betterx.bclib.util.BlocksHelper;
import org.betterx.bclib.util.MHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiConsumer;

@Mixin(BoneMealItem.class)
public class BoneMealItemMixin {
    @Unique
    private static final MutableBlockPos BCLIB_BLOCK_POS = new MutableBlockPos();

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void bclib_onUse(UseOnContext context, CallbackInfoReturnable<InteractionResult> info) {
        Level world = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        if (!world.isClientSide()) {
            if (BonemealAPI.isTerrain(world.getBlockState(blockPos).getBlock())) {
                boolean consume = false;
                if (BonemealAPI.isSpreadableTerrain(world.getBlockState(blockPos).getBlock())) {
                    BlockState terrain = bclib_getSpreadable(world, blockPos);
                    if (terrain != null) {
                        BlocksHelper.setWithoutUpdate(world, blockPos, terrain);
                        consume = true;
                    }
                } else {
                    BlockState stateAbove = world.getBlockState(blockPos.above());
                    if (!stateAbove.getFluidState().isEmpty()) {
                        if (stateAbove.is(Blocks.WATER)) {
                            consume = bclib_growWaterGrass(world, blockPos);
                        }
                    } else if (stateAbove.isAir()) {
                        consume = bclib_growLandGrass(world, blockPos);
                    }
                }
                if (consume) {
                    if (!context.getPlayer().isCreative()) {
                        context.getItemInHand().shrink(1);
                    }
                    world.levelEvent(2005, blockPos, 0);
                    info.setReturnValue(InteractionResult.SUCCESS);
                    info.cancel();
                }
            }
        }
    }

    @Unique
    private boolean bclib_growLandGrass(Level level, BlockPos pos) {
        int y1 = pos.getY() + 3;
        int y2 = pos.getY() - 3;
        boolean result = false;
        for (byte i = 0; i < 64; i++) {
            int x = (int) (pos.getX() + level.random.nextGaussian() * 2);
            int z = (int) (pos.getZ() + level.random.nextGaussian() * 2);
            BCLIB_BLOCK_POS.setX(x);
            BCLIB_BLOCK_POS.setZ(z);
            for (int y = y1; y >= y2; y--) {
                BCLIB_BLOCK_POS.setY(y);
                BlockPos down = BCLIB_BLOCK_POS.below();
                if (level.isEmptyBlock(BCLIB_BLOCK_POS) && !level.isEmptyBlock(down)) {
                    BiConsumer<Level, BlockPos> grass = bclib_getLandGrassState(level, down);
                    if (grass != null) {
                        grass.accept(level, BCLIB_BLOCK_POS);
                        result = true;
                    }
                    break;
                }
            }
        }
        return result;
    }

    @Unique
    private boolean bclib_growWaterGrass(Level level, BlockPos pos) {
        int y1 = pos.getY() + 3;
        int y2 = pos.getY() - 3;
        boolean result = false;
        for (byte i = 0; i < 64; i++) {
            int x = (int) (pos.getX() + level.random.nextGaussian() * 2);
            int z = (int) (pos.getZ() + level.random.nextGaussian() * 2);
            BCLIB_BLOCK_POS.setX(x);
            BCLIB_BLOCK_POS.setZ(z);
            for (int y = y1; y >= y2; y--) {
                BCLIB_BLOCK_POS.setY(y);
                BlockPos down = BCLIB_BLOCK_POS.below();
                if (BlocksHelper.isFluid(level.getBlockState(BCLIB_BLOCK_POS)) && !BlocksHelper.isFluid(level.getBlockState(
                        down))) {
                    BiConsumer<Level, BlockPos> grass = bclib_getWaterGrassState(level, down);
                    if (grass != null) {
                        grass.accept(level, BCLIB_BLOCK_POS);
                        result = true;
                    }
                    break;
                }
            }
        }
        return result;
    }

    @Unique
    private BiConsumer<Level, BlockPos> bclib_getLandGrassState(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return BonemealAPI.getLandGrass(BiomeAPI.getBiomeID(level.getBiome(pos)), state.getBlock(), level.getRandom());
    }

    @Unique
    private BiConsumer<Level, BlockPos> bclib_getWaterGrassState(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return BonemealAPI.getWaterGrass(BiomeAPI.getBiomeID(level.getBiome(pos)), state.getBlock(), level.getRandom());
    }

    @Unique
    private BlockState bclib_getSpreadable(Level level, BlockPos pos) {
        Vec3i[] offsets = MHelper.getOffsets(level.getRandom());
        BlockState center = level.getBlockState(pos);
        for (Vec3i dir : offsets) {
            BlockPos p = pos.offset(dir);
            BlockState state = level.getBlockState(p);
            Block terrain = BonemealAPI.getSpreadable(state.getBlock());
            if (center.is(terrain)) {
                if (bclib_haveSameProperties(state, center)) {
                    for (Property property : center.getProperties()) {
                        state = state.setValue(property, center.getValue(property));
                    }
                }
                return state;
            }
        }
        return null;
    }

    @Unique
    private boolean bclib_haveSameProperties(BlockState state1, BlockState state2) {
        Property<?>[] properties1 = state1.getProperties().toArray(new Property[0]);
        Property<?>[] properties2 = state2.getProperties().toArray(new Property[0]);
        if (properties1.length != properties2.length) {
            return false;
        }
        for (int i = 0; i < properties1.length; i++) {
            String name1 = properties1[i].getName();
            String name2 = properties2[i].getName();
            if (!name1.equals(name2)) {
                return false;
            }
        }
        return true;
    }
}