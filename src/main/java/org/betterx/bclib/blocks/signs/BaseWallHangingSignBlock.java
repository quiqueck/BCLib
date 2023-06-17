package org.betterx.bclib.blocks.signs;

import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.interfaces.TagProvider;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.properties.WoodType;

import java.util.List;

public abstract class BaseWallHangingSignBlock extends WallHangingSignBlock implements TagProvider {
    protected BaseWallHangingSignBlock(
            Properties properties,
            WoodType woodType
    ) {
        super(properties, woodType);
    }

    @Override
    public void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
        blockTags.add(BlockTags.WALL_HANGING_SIGNS);
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
