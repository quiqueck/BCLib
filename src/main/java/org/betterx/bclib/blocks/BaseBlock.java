package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;
import org.betterx.wover.loot.api.BlockLootProvider;
import org.betterx.wover.loot.api.LootLookupProvider;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootTable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for a default Block.
 * <p>
 * This Block-Type will:
 * <ul>
 * 	 <li>Drop itself</li>
 * 	 <li>Automatically create an Item-Model from the Block-Model</li>
 * </ul>
 */
public class BaseBlock extends Block implements BlockLootProvider, BlockModelProvider {
    /**
     * Creates a new Block with the passed properties
     *
     * @param settings The properties of the Block.
     */
    public BaseBlock(Properties settings) {
        this(settings, true);
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

    @Override
    @Environment(EnvType.CLIENT)
    public void provideBlockModels(WoverBlockModelGenerators generator) {
        generator.createCubeModel(this);
    }

    @Override
    public LootTable.Builder registerBlockLoot(
            @NotNull ResourceLocation location,
            @NotNull LootLookupProvider provider,
            @NotNull ResourceKey<LootTable> tableKey
    ) {
        return provider.drop(this);
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