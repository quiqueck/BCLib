package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.BehaviourHelper;
import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.wover.block.api.BlockTagProvider;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;
import org.betterx.wover.item.api.ItemTagProvider;
import org.betterx.wover.tag.api.event.context.ItemTagBootstrapContext;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;

public abstract class BasePressurePlateBlock extends PressurePlateBlock implements BlockModelProvider, BlockTagProvider, DropSelfLootProvider<BasePressurePlateBlock> {
    private final Block parent;

    protected BasePressurePlateBlock(Block source, BlockSetType type) {
        super(
                type, Properties.ofFullCopy(source).noCollission().noOcclusion().strength(0.5F)
        );
        this.parent = source;
    }


    @Override
    public void provideBlockModels(WoverBlockModelGenerators generators) {
        generators.modelFor(parent).createPressurePlate(this);
    }

    @Override
    public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
        context.add(BlockTags.PRESSURE_PLATES, this);
    }

    public static class Wood extends BasePressurePlateBlock implements BehaviourWood, ItemTagProvider {
        public Wood(Block source, BlockSetType type) {
            super(/*Sensitivity.EVERYTHING,*/ source, type);
        }

        @Override
        public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
            context.add(this, BlockTags.PRESSURE_PLATES, BlockTags.WOODEN_PRESSURE_PLATES);
        }

        @Override
        public void registerItemTags(ResourceLocation location, ItemTagBootstrapContext context) {
            context.add(this, ItemTags.WOODEN_PRESSURE_PLATES);
        }
    }

    public static class Stone extends BasePressurePlateBlock implements BehaviourStone {
        public Stone(Block source, BlockSetType type) {
            super(/*Sensitivity.MOBS,*/ source, type);
        }
    }

    public static class Metal extends BasePressurePlateBlock implements BehaviourMetal {
        public Metal(Block source, BlockSetType type) {
            super(/*Sensitivity.MOBS,*/ source, type);
        }
    }

    public static BasePressurePlateBlock from(Block source, BlockSetType type) {
        return BehaviourHelper.from(source, type,
                Wood::new, Stone::new, Metal::new
        );
    }
}
