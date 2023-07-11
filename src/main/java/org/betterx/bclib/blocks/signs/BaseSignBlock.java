package org.betterx.bclib.blocks.signs;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.BehaviourBuilders;
import org.betterx.bclib.behaviours.interfaces.BehaviourExplosionResistant;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("deprecation")
public abstract class BaseSignBlock extends StandingSignBlock implements BlockModelProvider, CustomItemProvider, TagProvider, DropSelfLootProvider<BaseSignBlock>, BehaviourExplosionResistant {
    protected final Supplier<BaseWallSignBlock> wallSign;
    private BlockItem customItem;
    private BaseWallSignBlock wallSignBlock;


    @FunctionalInterface
    public interface WallSignProvider {
        BaseWallSignBlock create(Properties properties, WoodType woodType);
    }

    protected BaseSignBlock(WoodType type, MapColor color, boolean flammable, WallSignProvider provider) {
        super(BehaviourBuilders.createSign(color, flammable), type);
        this.wallSign = () -> provider.create(BehaviourBuilders.createWallSign(color, this, flammable), type);
    }

    public BaseWallSignBlock getWallSignBlock() {
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
            customItem = new SignItem(settings, this, getWallSignBlock());
        }
        return customItem;
    }

    @Override
    public void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
        blockTags.add(BlockTags.STANDING_SIGNS);
        itemTags.add(ItemTags.SIGNS);
    }

    public static class Wood extends BaseSignBlock implements BehaviourWood {
        public Wood(WoodType type) {
            this(type, MapColor.WOOD, true);
        }

        public Wood(BCLWoodTypeWrapper type) {
            this(type.type, type.color, type.flammable);
        }

        public Wood(WoodType type, MapColor color, boolean flammable) {
            super(type, color, flammable, BaseWallSignBlock.Wood::new);
        }
    }

    public static BaseSignBlock from(BCLWoodTypeWrapper type) {
        return new BaseSignBlock.Wood(type);
    }
}