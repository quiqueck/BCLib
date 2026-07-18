package org.betterx.bclib.blocks;

import org.betterx.bclib.interfaces.tools.AddMineableAxe;
import org.betterx.bclib.interfaces.tools.AddMineablePickaxe;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Consumer;

/**
 * Base class for a default Block.
 * <p>
 * Loot is no longer provided implicitly - register a {@code BlockTraits.LOOT_TABLE} trait
 * at the registration site for blocks that need one. Likewise, the block model is no longer
 * provided implicitly - register {@code ModelTraitLibrary.cubeWithFlatItem()} (or an equivalent
 * {@code ClientBlockTraits.MODEL} trait) at the registration site of any block that needs one.
 */
public class BaseBlock extends Block {
    /**
     * Creates a new Block with the passed properties
     *
     * @param settings The properties of the Block.
     */
    public BaseBlock(Properties settings) {
        this(settings, false);
    }

    protected BaseBlock(Properties settings, boolean emptyLootTable) {
        super(emptyLootTable ? settings.noLootTable() : settings);
    }

//    /**
//     * {@inheritDoc}
//     * <p>
//     * This implementation will load the Block-Model and return it as the Item-Model
//     */
//    @Override
//    public BlockModel getItemModel(ResourceLocation blockId) {
//        return getBlockModel(blockId, defaultBlockState());
//    }

    /**
     * This method is used internally.
     * <p>
     * It is called from Block-Contructors, to allow the augmentation of the blocks
     * preset properties.
     * <p>
     * For example in {@link BaseLeavesBlock#BaseLeavesBlock(Block, MapColor, Consumer)}
     *
     * @param customizeProperties A {@link Consumer} to call with the preset properties
     * @param settings            The properties as created by the Block
     * @return The reconfigured {@code settings}
     */
    static Properties acceptAndReturn(
            Consumer<Properties> customizeProperties,
            Properties settings
    ) {
        customizeProperties.accept(settings);
        return settings;
    }

    public static class Wood extends BaseBlock implements AddMineableAxe {
        public Wood(Properties settings) {
            super(settings);
        }
    }

    public static class Stone extends BaseBlock implements AddMineablePickaxe {
        public Stone(Properties settings) {
            super(settings);
        }
    }

    public static class Metal extends BaseBlock implements AddMineablePickaxe {
        public Metal(Properties settings) {
            super(settings);
        }
    }
}