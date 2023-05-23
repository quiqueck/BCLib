package org.betterx.bclib.blocks.signs;

import org.betterx.bclib.behaviours.BehaviourBuilders;
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

public abstract class BaseHangingSignBlock extends CeilingHangingSignBlock implements BlockModelProvider, CustomItemProvider, TagProvider {
    public final BaseWallHangingSignBlock wallSign;

    @FunctionalInterface
    public interface WallSignProvider {
        BaseWallHangingSignBlock create(Properties properties, WoodType woodType);
    }

    protected BaseHangingSignBlock(WoodType type, MapColor color, boolean flammable, WallSignProvider provider) {
        super(BehaviourBuilders.createSign(color, flammable), type);
        this.wallSign = provider.create(BehaviourBuilders.createWallSign(color, this, flammable), type);
    }


    @Override
    public float getYRotationDegrees(BlockState blockState) {
        return RotationSegment.convertToDegrees(blockState.getValue(StandingSignBlock.ROTATION));
    }

    @Override
    public BlockItem getCustomItem(ResourceLocation blockID, Item.Properties settings) {
        return new HangingSignItem(this, wallSign, settings.stacksTo(16));
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

    public static BaseHangingSignBlock from(WoodType type) {
        return new BaseHangingSignBlock.Wood(type);
    }
}
