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
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MaterialColor;

public class BaseHangingSignBlock extends CeilingHangingSignBlock implements BlockModelProvider, CustomItemProvider {
    public final BaseWallHangingSignBlock wallSign;

    public BaseHangingSignBlock(WoodType type) {
        this(type, MaterialColor.WOOD);
    }

    public BaseHangingSignBlock(BCLWoodTypeWrapper type) {
        this(type.type, type.color);
    }

    public BaseHangingSignBlock(WoodType type, MaterialColor color) {
        super(BehaviourBuilders.createSign(color), type);
        this.wallSign = new BaseWallHangingSignBlock(BehaviourBuilders.createWallSign(color, this), type);
    }

    @Override
    public BlockItem getCustomItem(ResourceLocation blockID, Item.Properties settings) {
        return new HangingSignItem(this, wallSign, settings.stacksTo(16));
    }
}
