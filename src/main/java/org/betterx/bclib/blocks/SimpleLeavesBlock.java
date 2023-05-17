package org.betterx.bclib.blocks;

import org.betterx.bclib.client.render.BCLRenderLayer;
import org.betterx.bclib.complexmaterials.BehaviourBuilders;
import org.betterx.bclib.interfaces.RenderLayerProvider;
import org.betterx.bclib.interfaces.TagProvider;
import org.betterx.bclib.interfaces.tools.AddMineableHoe;
import org.betterx.bclib.interfaces.tools.AddMineableShears;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.List;

public class SimpleLeavesBlock extends BaseBlockNotFull implements RenderLayerProvider, TagProvider, AddMineableShears, AddMineableHoe {
    public SimpleLeavesBlock(MapColor color) {
        this(
                BehaviourBuilders
                        .createLeaves(color)
                        .sound(SoundType.GRASS)
        );
    }

    public SimpleLeavesBlock(MapColor color, int light) {
        this(
                BehaviourBuilders
                        .createLeaves(color)
                        .lightLevel(ignored -> light)
                        .sound(SoundType.GRASS)
        );
    }

    public SimpleLeavesBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BCLRenderLayer getRenderLayer() {
        return BCLRenderLayer.CUTOUT;
    }

    @Override
    public void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
        blockTags.add(BlockTags.LEAVES);
        itemTags.add(ItemTags.LEAVES);
    }
}