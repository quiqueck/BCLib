package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.interfaces.BlockModelProvider;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Base class for a default Block.
 * <p>
 * This Block-Type will:
 * <ul>
 * 	 <li>Drop itself</li>
 * 	 <li>Automatically create an Item-Model from the Block-Model</li>
 * </ul>
 */
public class BaseBlock extends Block implements BlockModelProvider {
    /**
     * Creates a new Block with the passed properties
     *
     * @param settings The properties of the Block.
     */
    public BaseBlock(Properties settings) {
        super(settings);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation will drop the Block itself
     */
    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return Collections.singletonList(new ItemStack(this));
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation will load the Block-Model and return it as the Item-Model
     */
    @Override
    public BlockModel getItemModel(ResourceLocation blockId) {
        return getBlockModel(blockId, defaultBlockState());
    }

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

    public static class Wood extends BaseBlock implements BehaviourWood {
        public Wood(Properties settings) {
            super(settings);
        }
    }

    public static class Stone extends BaseBlock implements BehaviourStone {
        public Stone(Properties settings) {
            super(settings);
        }
    }

    public static class Metal extends BaseBlock implements BehaviourMetal {
        public Metal(Properties settings) {
            super(settings);
        }
    }
}