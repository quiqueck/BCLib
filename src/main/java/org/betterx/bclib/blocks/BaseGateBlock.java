package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.wover.block.api.BlockTagProvider;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.properties.WoodType;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public abstract class BaseGateBlock extends FenceGateBlock implements BlockModelProvider, BlockTagProvider, DropSelfLootProvider<BaseGateBlock> {
    private final Block parent;

    protected BaseGateBlock(Block source, WoodType type) {
        super(type, Properties.ofFullCopy(source).noOcclusion());
        this.parent = source;
    }

    @Override
    public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
        context.add(BlockTags.FENCE_GATES, this);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void provideBlockModels(WoverBlockModelGenerators generator) {
        generator.createFenceGate(parent, this);
    }

    public static class Wood extends BaseGateBlock implements BehaviourWood {
        public Wood(Block source, WoodType type) {
            super(source, type);
        }
    }

    public static BaseGateBlock from(Block source, WoodType type) {
        return new BaseGateBlock.Wood(source, type);
    }
}