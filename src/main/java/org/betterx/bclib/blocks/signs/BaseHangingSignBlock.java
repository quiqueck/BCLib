package org.betterx.bclib.blocks.signs;

import org.betterx.bclib.behaviours.BehaviourBuilders;
import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.complexmaterials.BCLWoodTypeWrapper;
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
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Supplier;

public abstract class BaseHangingSignBlock extends CeilingHangingSignBlock implements BlockModelProvider, CustomBlockItemProvider, BlockTagProvider, ItemTagProvider {
    protected final Supplier<BaseWallHangingSignBlock> wallSign;
    private BlockItem customItem;
    private BaseWallHangingSignBlock wallSignBlock;
    private final Block parent;

    @FunctionalInterface
    public interface WallSignProvider {
        BaseWallHangingSignBlock create(Properties properties, WoodType woodType);
    }

    protected BaseHangingSignBlock(
            Block parent,
            WoodType type,
            MapColor color,
            boolean flammable,
            WallSignProvider provider
    ) {
        super(type, BehaviourBuilders.createSign(color, flammable));
        this.parent = parent;
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
    public BlockItem getCustomBlockItem(ResourceLocation blockID, Item.Properties settings) {
        if (customItem == null) {
            customItem = new HangingSignItem(this, getWallSignBlock(), settings.stacksTo(16));
        }
        return customItem;
    }

    @Override
    public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
        context.add(this, BlockTags.CEILING_HANGING_SIGNS);
    }

    @Override
    public void registerItemTags(ResourceLocation location, ItemTagBootstrapContext context) {
        context.add(this, ItemTags.HANGING_SIGNS);
    }

    @Override
    public void provideBlockModels(WoverBlockModelGenerators generator) {
        generator.createHangingSign(parent, this, getWallSignBlock());
    }

    public static class Wood extends BaseHangingSignBlock implements BehaviourWood {
        public Wood(Block parent, WoodType type) {
            this(parent, type, MapColor.WOOD, true);
        }

        public Wood(Block parent, BCLWoodTypeWrapper type) {
            this(parent, type.type, type.color, type.flammable);
        }

        public Wood(Block parent, WoodType type, MapColor color, boolean flammable) {
            super(parent, type, color, flammable, BaseWallHangingSignBlock.Wood::new);
        }
    }

    public static class Stone extends BaseHangingSignBlock implements BehaviourStone {
        public Stone(Block parent, WoodType type) {
            this(parent, type, MapColor.WOOD, true);
        }

        public Stone(Block parent, BCLWoodTypeWrapper type) {
            this(parent, type.type, type.color, type.flammable);
        }

        public Stone(Block parent, WoodType type, MapColor color, boolean flammable) {
            super(parent, type, color, flammable, BaseWallHangingSignBlock.Stone::new);
        }
    }

    public static class Metal extends BaseHangingSignBlock implements BehaviourMetal {
        public Metal(Block parent, WoodType type) {
            this(parent, type, MapColor.WOOD, true);
        }

        public Metal(Block parent, BCLWoodTypeWrapper type) {
            this(parent, type.type, type.color, type.flammable);
        }

        public Metal(Block parent, WoodType type, MapColor color, boolean flammable) {
            super(parent, type, color, flammable, BaseWallHangingSignBlock.Stone::new);
        }
    }

    public static BaseHangingSignBlock from(Block parent, WoodType type) {
        return new BaseHangingSignBlock.Wood(parent, type);
    }
}
