package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.BehaviourHelper;
import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.behaviours.interfaces.BehaviourObsidian;
import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.wover.block.api.BlockTagProvider;
import org.betterx.wover.block.api.CustomBlockItemProvider;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;
import org.betterx.wover.item.api.ItemTagProvider;
import org.betterx.wover.tag.api.event.context.ItemTagBootstrapContext;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public abstract class BaseStairsBlock extends StairBlock implements BlockModelProvider, CustomBlockItemProvider, BlockTagProvider, ItemTagProvider, DropSelfLootProvider<BaseStairsBlock> {
    private final Block parent;
    public final boolean fireproof;

    protected BaseStairsBlock(Block source, boolean fireproof) {
        super(source.defaultBlockState(), Properties.ofFullCopy(source));
        this.parent = source;
        this.fireproof = fireproof;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void provideBlockModels(WoverBlockModelGenerators generator) {
        generator.createStairs(parent, this);
    }

    @Override
    public BlockItem getCustomBlockItem(ResourceLocation blockID, Item.Properties settings) {
        if (fireproof) settings = settings.fireResistant();
        return new BlockItem(this, settings);
    }

    @Override
    public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
        context.add(this, BlockTags.STAIRS);
    }

    @Override
    public void registerItemTags(ResourceLocation location, ItemTagBootstrapContext context) {
        context.add(this, ItemTags.STAIRS);
    }

    public static class Stone extends BaseStairsBlock implements BehaviourStone {
        public Stone(Block source) {
            this(source, true);
        }

        public Stone(Block source, boolean fireproof) {
            super(source, fireproof);
        }
    }

    public static class Metal extends BaseStairsBlock implements BehaviourMetal {
        public Metal(Block source) {
            this(source, true);
        }

        public Metal(Block source, boolean fireproof) {
            super(source, fireproof);
        }
    }

    public static class Wood extends BaseStairsBlock implements BehaviourWood {
        public Wood(Block source) {
            this(source, false);
        }

        public Wood(Block source, boolean fireproof) {
            super(source, fireproof);
        }


        @Override
        public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
            context.add(this, BlockTags.STAIRS, BlockTags.WOODEN_STAIRS);
        }

        @Override
        public void registerItemTags(ResourceLocation location, ItemTagBootstrapContext context) {
            context.add(this, ItemTags.STAIRS, ItemTags.WOODEN_STAIRS);
        }
    }

    public static class Obsidian extends BaseStairsBlock implements BehaviourObsidian {
        public Obsidian(Block source) {
            this(source, true);
        }

        public Obsidian(Block source, boolean fireproof) {
            super(source, fireproof);
        }

    }

    public static BaseStairsBlock from(Block source, boolean flammable) {
        return BehaviourHelper.from(
                source,
                (block) -> new Wood(block, flammable),
                (block) -> new Stone(block, !flammable),
                (block) -> new Metal(block, !flammable),
                (block) -> new Obsidian(block, !flammable),
                null
        );

    }
}
