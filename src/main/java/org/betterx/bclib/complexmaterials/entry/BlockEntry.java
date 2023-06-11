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
    final boolean isPseudoEntry;

    TagKey<Block>[] blockTags;
    TagKey<Item>[] itemTags;

    public BlockEntry(String suffix, BiFunction<ComplexMaterial, BlockBehaviour.Properties, Block> initFunction) {
        this(suffix, true, false, initFunction);
    }

    public BlockEntry(
            String suffix,
            boolean hasItem,
            BiFunction<ComplexMaterial, BlockBehaviour.Properties, Block> initFunction
    ) {
        this(suffix, hasItem, false, initFunction);
    }

    public BlockEntry(
            String suffix,
            boolean hasItem,
            boolean isPseudoEntry,
            BiFunction<ComplexMaterial, BlockBehaviour.Properties, Block> initFunction
    ) {
        super(suffix);
        this.initFunction = initFunction;
        this.hasItem = hasItem;
        this.isPseudoEntry = isPseudoEntry;
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
        if (block == null) return null;

        if (!isPseudoEntry) {
            if (hasItem) {
                registry.register(location, block);

            } else {
                registry.registerBlockOnly(location, block);
            }
        }
        if (hasItem && itemTags != null) {
            TagManager.ITEMS.add(block.asItem(), itemTags);
        }
        if (blockTags != null) {
            TagManager.BLOCKS.add(block, blockTags);
        }

        return block;
    }
}
