package org.betterx.bclib.complexmaterials;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.behaviours.BehaviourBuilders;
import org.betterx.bclib.complexmaterials.entry.SlotMap;
import org.betterx.bclib.complexmaterials.set.stone.StoneSlots;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class StoneComplexMaterial extends ComplexMaterialSet<StoneComplexMaterial> {
    public static final ResourceLocation MATERIAL_ID = BCLib.makeID("stone_material");
    public final MapColor color;
    public final Block sourceBlock;

    protected StoneComplexMaterial(
            String modID,
            String baseName,
            String receipGroupPrefix,
            Block sourceBlock,
            MapColor color
    ) {
        super(modID, baseName, receipGroupPrefix);
        this.color = color;
        this.sourceBlock = sourceBlock;
    }

    @Override
    protected BlockBehaviour.Properties getBlockSettings() {
        return BehaviourBuilders.createStone(color);
    }

    @Override
    public ResourceLocation getMaterialID() {
        return MATERIAL_ID;
    }

    @Override
    protected SlotMap<StoneComplexMaterial> createMaterialSlots() {
        return SlotMap.of(
                StoneSlots.SOURCE,
                StoneSlots.SLAB,
                StoneSlots.STAIRS,
                StoneSlots.WALL
        );
    }

}
