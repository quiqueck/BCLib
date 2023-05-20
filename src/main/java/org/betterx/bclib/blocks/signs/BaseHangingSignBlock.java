package org.betterx.bclib.blocks.signs;

import org.betterx.bclib.complexmaterials.BCLWoodTypeWrapper;
import org.betterx.bclib.complexmaterials.BehaviourBuilders;
import org.betterx.bclib.interfaces.BlockModelProvider;
import org.betterx.bclib.interfaces.CustomItemProvider;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;

public class BaseHangingSignBlock extends CeilingHangingSignBlock implements BlockModelProvider, CustomItemProvider {
    public final BaseWallHangingSignBlock wallSign;

    public BaseHangingSignBlock(WoodType type) {
        this(type, MapColor.WOOD);
    }

    public BaseHangingSignBlock(BCLWoodTypeWrapper type) {
        this(type.type, type.color);
    }

    public BaseHangingSignBlock(WoodType type, MapColor color) {
        super(BehaviourBuilders.createSign(color), type);
        this.wallSign = new BaseWallHangingSignBlock(BehaviourBuilders.createWallSign(color, this), type);
    }


    @Override
    public float getYRotationDegrees(BlockState blockState) {
        return RotationSegment.convertToDegrees(blockState.getValue(StandingSignBlock.ROTATION));
    }

    @Override
    public BlockItem getCustomItem(ResourceLocation blockID, Item.Properties settings) {
        return new HangingSignItem(this, wallSign, settings.stacksTo(16));
    }
}
