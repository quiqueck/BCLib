package org.betterx.bclib.blocks;

import org.betterx.bclib.complexmaterials.BCLWoodTypeWrapper;
import org.betterx.bclib.complexmaterials.BehaviourBuilders;
import org.betterx.bclib.interfaces.BlockModelProvider;
import org.betterx.bclib.interfaces.CustomItemProvider;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MaterialColor;

@SuppressWarnings("deprecation")
public class BaseSignBlock extends StandingSignBlock implements BlockModelProvider, CustomItemProvider {
    public final WallSignBlock wallSign;

    public BaseSignBlock(WoodType type) {
        this(type, MaterialColor.WOOD);
    }

    public BaseSignBlock(BCLWoodTypeWrapper type) {
        this(type.type, type.color);
    }

    public BaseSignBlock(WoodType type, MaterialColor color) {
        super(BehaviourBuilders.createSign(color), type);
        this.wallSign = new BaseWallSignBlock(BehaviourBuilders.createWallSign(color, this), type);
    }

    @Override
    public BlockItem getCustomItem(ResourceLocation blockID, Item.Properties settings) {
        return new SignItem(settings.stacksTo(16), this, wallSign);
    }
}