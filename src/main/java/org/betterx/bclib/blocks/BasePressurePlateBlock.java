package org.betterx.bclib.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;

/**
 * The block model, block/item tags and loot table are no longer provided implicitly - register the
 * appropriate traits at the registration site of any block that needs them:
 * {@code ModelTraitLibrary.pressurePlate(() -> parent)} for the model, {@code BlockTraits.BLOCK_TAG}
 * (e.g. {@code BlockTags.PRESSURE_PLATES}, {@code BlockTags.WOODEN_PRESSURE_PLATES}) for the tags, and
 * {@code BlockTraits.LOOT_TABLE.dropSelf()} for the loot.
 */
public abstract class BasePressurePlateBlock extends PressurePlateBlock {
    private final Block parent;

    protected BasePressurePlateBlock(Block source, BlockSetType type) {
        super(
                type, Properties.ofFullCopy(source).noCollission().noOcclusion().strength(0.5F)
        );
        this.parent = source;
    }

    protected BasePressurePlateBlock(Block source, Properties settings, BlockSetType type) {
        super(
                type, settings.noCollission().noOcclusion().strength(0.5F)
        );
        this.parent = source;
    }

    public Block getParent() {
        return parent;
    }

    public static class Wood extends BasePressurePlateBlock {
        public Wood(Block source, BlockSetType type) {
            super(/*Sensitivity.EVERYTHING,*/ source, type);
        }

        public Wood(Block source, Properties settings, BlockSetType type) {
            super(source, settings, type);
        }
    }

    public static class Stone extends BasePressurePlateBlock {
        public Stone(Block source, BlockSetType type) {
            super(/*Sensitivity.MOBS,*/ source, type);
        }

        public Stone(Block source, Properties settings, BlockSetType type) {
            super(source, settings, type);
        }
    }

    public static class Metal extends BasePressurePlateBlock {
        public Metal(Block source, BlockSetType type) {
            super(/*Sensitivity.MOBS,*/ source, type);
        }

        public Metal(Block source, Properties settings, BlockSetType type) {
            super(source, settings, type);
        }
    }

}
