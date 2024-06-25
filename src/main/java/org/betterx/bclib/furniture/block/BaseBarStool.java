package org.betterx.bclib.furniture.block;

import org.betterx.bclib.behaviours.BehaviourHelper;
import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.client.models.BCLModels;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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

    @Deprecated(forRemoval = true)
    public BaseBarStool(Block baseMaterial) {
        this(baseMaterial, Blocks.RED_WOOL);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        return SHAPE;
    }

    public static class Wood extends BaseBarStool implements BehaviourWood {
        @Deprecated(forRemoval = true)
        public Wood(Block baseMaterial) {
            super(baseMaterial, Blocks.RED_WOOL);
        }

        public Wood(Block baseMaterial, Block clothMaterial) {
            super(baseMaterial, clothMaterial);
        }
    }

    public static class Stone extends BaseBarStool implements BehaviourStone {
        @Deprecated(forRemoval = true)
        public Stone(Block baseMaterial) {
            super(baseMaterial, Blocks.RED_WOOL);
        }

        public Stone(Block baseMaterial, Block clothMaterial) {
            super(baseMaterial, clothMaterial);
        }
    }

    public static class Metal extends BaseBarStool implements BehaviourMetal {
        @Deprecated(forRemoval = true)
        public Metal(Block baseMaterial) {
            super(baseMaterial, Blocks.RED_WOOL);
        }

        public Metal(Block baseMaterial, Block clothMaterial) {
            super(baseMaterial, clothMaterial);
        }
    }

    @Deprecated(forRemoval = true)
    public static BaseBarStool from(Block source) {
        return BehaviourHelper.from(source, Wood::new, Stone::new, Metal::new);
    }

    public static BaseBarStool from(Block baseMaterial, Block clothMaterial) {
        return BehaviourHelper.from(baseMaterial, (b) -> new Wood(b, clothMaterial), (b) -> new Stone(b, clothMaterial), (b) -> new Metal(b, clothMaterial));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void provideBlockModels(WoverBlockModelGenerators generators) {
        BCLModels.createBarStoolBlockModel(generators, this, this.baseMaterial, this.clothMaterial);
    }
}