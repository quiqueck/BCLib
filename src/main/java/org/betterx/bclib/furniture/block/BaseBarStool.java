package org.betterx.bclib.furniture.block;

import org.betterx.bclib.behaviours.BehaviourHelper;
import org.betterx.bclib.client.models.BCLModels;
import org.betterx.bclib.interfaces.tools.AddMineableAxe;
import org.betterx.bclib.interfaces.tools.AddMineablePickaxe;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public abstract class BaseBarStool extends AbstractChair {
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 16, 12);
    public final Block clothMaterial;

    public BaseBarStool(Block baseMaterial, Block clothMaterial) {
        super(baseMaterial, 15);
        this.clothMaterial = Objects.requireNonNull(clothMaterial, "Bar Stool cloth material cannot be null (" + baseMaterial.getDescriptionId() + ")");
    }

    public BaseBarStool(Block baseMaterial, Block clothMaterial, BlockBehaviour.Properties settings) {
        super(baseMaterial, settings, 15);
        this.clothMaterial = Objects.requireNonNull(clothMaterial, "Bar Stool cloth material cannot be null (" + baseMaterial.getDescriptionId() + ")");
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        return SHAPE;
    }

    public static class Wood extends BaseBarStool implements AddMineableAxe {
        public Wood(Block baseMaterial, Block clothMaterial) {
            super(baseMaterial, clothMaterial);
        }

        public Wood(Block baseMaterial, Block clothMaterial, BlockBehaviour.Properties settings) {
            super(baseMaterial, clothMaterial, settings);
        }
    }

    public static class Stone extends BaseBarStool implements AddMineablePickaxe {
        public Stone(Block baseMaterial, Block clothMaterial) {
            super(baseMaterial, clothMaterial);
        }

        public Stone(Block baseMaterial, Block clothMaterial, BlockBehaviour.Properties settings) {
            super(baseMaterial, clothMaterial, settings);
        }
    }

    public static class Metal extends BaseBarStool implements AddMineablePickaxe {
        public Metal(Block baseMaterial, Block clothMaterial) {
            super(baseMaterial, clothMaterial);
        }

        public Metal(Block baseMaterial, Block clothMaterial, BlockBehaviour.Properties settings) {
            super(baseMaterial, clothMaterial, settings);
        }
    }

    public static BaseBarStool from(Block baseMaterial, Block clothMaterial) {
        return BehaviourHelper.from(baseMaterial, (b) -> new Wood(b, clothMaterial), (b) -> new Stone(b, clothMaterial), (b) -> new Metal(b, clothMaterial));
    }

    public static BaseBarStool from(Block baseMaterial, Block clothMaterial, BlockBehaviour.Properties settings) {
        return BehaviourHelper.from(
                baseMaterial,
                (b) -> new Wood(b, clothMaterial, settings),
                (b) -> new Stone(b, clothMaterial, settings),
                (b) -> new Metal(b, clothMaterial, settings)
        );
    }

    /**
     * Generates the block model for a bar stool, using {@code baseMaterial}/{@code clothMaterial}'s textures.
     *
     * @param generator The generator helper to emit the blockstate/model through
     * @param barStoolBlock The bar stool block to generate the model for
     */
    @Environment(EnvType.CLIENT)
    public static void provideBlockModel(WoverBlockModelGenerators generator, BaseBarStool barStoolBlock) {
        BCLModels.createBarStoolBlockModel(generator, barStoolBlock, barStoolBlock.baseMaterial, barStoolBlock.clothMaterial);
    }
}