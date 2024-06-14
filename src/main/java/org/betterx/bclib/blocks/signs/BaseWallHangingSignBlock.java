package org.betterx.bclib.blocks.signs;

import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.wover.block.api.BlockTagProvider;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.properties.WoodType;

public abstract class BaseWallHangingSignBlock extends WallHangingSignBlock implements BlockTagProvider {
    protected BaseWallHangingSignBlock(
            Properties properties,
            WoodType woodType
    ) {
        super(woodType, properties);
    }

    @Override
    public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
        context.add(this, BlockTags.WALL_HANGING_SIGNS);
    }

    public static class Wood extends BaseWallHangingSignBlock implements BehaviourWood {
        public Wood(Properties properties, WoodType woodType) {
            super(properties, woodType);
        }
    }

    public static class Stone extends BaseWallHangingSignBlock implements BehaviourStone {
        public Stone(Properties properties, WoodType woodType) {
            super(properties, woodType);
        }
    }

    public static class Metal extends BaseWallHangingSignBlock implements BehaviourMetal {
        public Metal(Properties properties, WoodType woodType) {
            super(properties, woodType);
        }
    }
}
