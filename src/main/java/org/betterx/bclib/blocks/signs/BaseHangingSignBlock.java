package org.betterx.bclib.blocks.signs;

import org.betterx.bclib.behaviours.BehaviourBuilders;
import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.complexmaterials.BCLWoodTypeWrapper;
import org.betterx.bclib.interfaces.BlockModelProvider;
import org.betterx.bclib.interfaces.CustomItemProvider;
import org.betterx.bclib.interfaces.TagProvider;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;

import java.util.List;
import java.util.function.Supplier;

public abstract class BaseHangingSignBlock extends CeilingHangingSignBlock implements BlockModelProvider, CustomItemProvider, TagProvider {
    protected final Supplier<BaseWallHangingSignBlock> wallSign;
    private BlockItem customItem;
    private BaseWallHangingSignBlock wallSignBlock;

    @FunctionalInterface
    public interface WallSignProvider {
        BaseWallHangingSignBlock create(Properties properties, WoodType woodType);
    }

    protected BaseHangingSignBlock(WoodType type, MapColor color, boolean flammable, WallSignProvider provider) {
        super(BehaviourBuilders.createSign(color, flammable), type);
        this.wallSign = () -> provider.create(BehaviourBuilders.createWallSign(color, this, flammable), type);
    }

    public BaseWallHangingSignBlock getWallSignBlock() {
        if (wallSignBlock == null) {
            wallSignBlock = wallSign.get();
        }
        return wallSignBlock;
    }

    @Override
    public float getYRotationDegrees(BlockState blockState) {
        return RotationSegment.convertToDegrees(blockState.getValue(StandingSignBlock.ROTATION));
    }

    @Override
    public BlockItem getCustomItem(ResourceLocation blockID, Item.Properties settings) {
        if (customItem == null) {
            customItem = new HangingSignItem(this, getWallSignBlock(), settings.stacksTo(16));
        }
        return customItem;
    }

    @Override
    public void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
        blockTags.add(BlockTags.CEILING_HANGING_SIGNS);
        itemTags.add(ItemTags.HANGING_SIGNS);
    }

    public static class Wood extends BaseHangingSignBlock implements BehaviourWood {
        public Wood(WoodType type) {
            this(type, MapColor.WOOD, true);
        }

        public Wood(BCLWoodTypeWrapper type) {
            this(type.type, type.color, type.flammable);
        }

        public Wood(WoodType type, MapColor color, boolean flammable) {
            super(type, color, flammable, BaseWallHangingSignBlock.Wood::new);
        }
    }

    public static class Stone extends BaseHangingSignBlock implements BehaviourStone {
        public Stone(WoodType type) {
            this(type, MapColor.WOOD, true);
        }

        public Stone(BCLWoodTypeWrapper type) {
            this(type.type, type.color, type.flammable);
        }

        public Stone(WoodType type, MapColor color, boolean flammable) {
            super(type, color, flammable, BaseWallHangingSignBlock.Stone::new);
        }
    }

    public static class Metal extends BaseHangingSignBlock implements BehaviourMetal {
        public Metal(WoodType type) {
            this(type, MapColor.WOOD, true);
        }

        public Metal(BCLWoodTypeWrapper type) {
            this(type.type, type.color, type.flammable);
        }

        public Metal(WoodType type, MapColor color, boolean flammable) {
            super(type, color, flammable, BaseWallHangingSignBlock.Stone::new);
        }
    }

    public static BaseHangingSignBlock from(WoodType type) {
        return new BaseHangingSignBlock.Wood(type);
    }
}
