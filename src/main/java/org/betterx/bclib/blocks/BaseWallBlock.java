package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.wover.block.api.BlockTagProvider;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBlock;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public abstract class BaseWallBlock extends WallBlock implements DropSelfLootProvider<BaseWallBlock>, BlockModelProvider, BlockTagProvider {
    private final Block parent;

    protected BaseWallBlock(Block source) {
        super(Properties.ofFullCopy(source).noOcclusion());
        this.parent = source;
    }

    @Override
    public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
        context.add(this, BlockTags.WALLS);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void provideBlockModels(WoverBlockModelGenerators generator) {
        generator.modelFor(parent).createWall(this);
    }

    public static class Stone extends BaseWallBlock implements BehaviourStone {
        public Stone(Block source) {
            super(source);
        }
    }

    public static class Wood extends BaseWallBlock implements BehaviourWood {
        public Wood(Block source) {
            super(source);
        }
    }
}
