package org.betterx.bclib.complexmaterials.entry;

import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.registry.BlockRegistry;
import org.betterx.worlds.together.tag.v3.TagManager;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.BiFunction;

public class BlockEntry extends ComplexMaterialEntry {
    final BiFunction<ComplexMaterial, BlockBehaviour.Properties, Block> initFunction;
    final boolean hasItem;

    TagKey<Block>[] blockTags;
    TagKey<Item>[] itemTags;

    public BlockEntry(String suffix, BiFunction<ComplexMaterial, BlockBehaviour.Properties, Block> initFunction) {
        this(suffix, true, initFunction);
    }

    public BlockEntry(
            String suffix,
            boolean hasItem,
            BiFunction<ComplexMaterial, BlockBehaviour.Properties, Block> initFunction
    ) {
        super(suffix);
        this.initFunction = initFunction;
        this.hasItem = hasItem;
    }

    @SafeVarargs
    public final BlockEntry setBlockTags(TagKey<Block>... blockTags) {
        this.blockTags = blockTags;
        return this;
    }

    @SafeVarargs
    public final BlockEntry setItemTags(TagKey<Item>... itemTags) {
        this.itemTags = itemTags;
        return this;
    }

    public Block init(ComplexMaterial material, BlockBehaviour.Properties blockSettings, BlockRegistry registry) {
        ResourceLocation location = getLocation(material.getModID(), material.getBaseName());
        Block block = initFunction.apply(material, blockSettings);
        if (hasItem) {
            registry.register(location, block);
            if (itemTags != null) {
                TagManager.ITEMS.add(block.asItem(), itemTags);
            }
        } else {
            registry.registerBlockOnly(location, block);
        }
        if (blockTags != null) {
            TagManager.BLOCKS.add(block, blockTags);
        }
        return block;
    }
}
