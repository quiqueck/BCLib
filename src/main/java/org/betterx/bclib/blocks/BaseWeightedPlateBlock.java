package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeightedPressurePlateBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;

/**
 * The block model is no longer provided implicitly - register
 * {@code ModelTraitLibrary.pressurePlate(() -> parent)} (or an equivalent {@code ClientBlockTraits.MODEL}
 * trait) at the registration site of any block that needs one.
 */
public class BaseWeightedPlateBlock extends WeightedPressurePlateBlock implements DropSelfLootProvider<BaseWeightedPlateBlock> {
    private final Block parent;

    public BaseWeightedPlateBlock(Block source, BlockSetType type) {
        super(
                15,
                type,
                Properties.ofFullCopy(source)
                          .noCollission()
                          .noOcclusion()
                          .requiresCorrectToolForDrops()
                          .strength(0.5F)
        );
        this.parent = source;
    }

    public Block getParent() {
        return parent;
    }
}
