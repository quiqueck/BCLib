package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.wover.block.api.BlockTagProvider;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;
import org.betterx.wover.tag.api.predefined.CommonBlockTags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;

public abstract class BaseComposterBlock extends ComposterBlock implements BlockModelProvider, BlockTagProvider, DropSelfLootProvider<BaseComposterBlock> {
    protected BaseComposterBlock(Block source) {
        super(Properties.ofFullCopy(source));
    }

    @Override
    public void provideBlockModels(WoverBlockModelGenerators generators) {
        generators.createComposter(this);
    }

    @Override
    public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
        context.add(this, CommonBlockTags.COMPOSTER, org.betterx.wover.tag.api.predefined.CommonPoiTags.FARMER_WORKSTATION);
    }

    public static class Wood extends BaseComposterBlock implements BehaviourWood {
        public Wood(Block source) {
            super(source);
        }

        @Override
        public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
            context.add(this, CommonBlockTags.COMPOSTER, CommonBlockTags.WOODEN_COMPOSTER, org.betterx.wover.tag.api.predefined.CommonPoiTags.FARMER_WORKSTATION);
        }
    }

    public static BaseComposterBlock from(Block source) {
        return new BaseComposterBlock.Wood(source);
    }
}
