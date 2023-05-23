package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.bclib.interfaces.TagProvider;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Optional;

public abstract class BaseBarkBlock extends BaseRotatedPillarBlock {
    protected BaseBarkBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected Optional<String> createBlockPattern(ResourceLocation blockId) {
        blockId = BuiltInRegistries.BLOCK.getKey(this);
        return PatternsHelper.createJson(BasePatterns.BLOCK_BASE, replacePath(blockId));
    }

    private ResourceLocation replacePath(ResourceLocation blockId) {
        String newPath = blockId.getPath().replace("_bark", "_log_side");
        return new ResourceLocation(blockId.getNamespace(), newPath);
    }

    public static class Wood extends BaseBarkBlock implements BehaviourWood, TagProvider {
        private final boolean flammable;

        public Wood(Properties settings, boolean flammable) {
            super(flammable ? settings.ignitedByLava() : settings);
            this.flammable = flammable;
        }

        @Override
        public void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
            blockTags.add(BlockTags.LOGS);
            itemTags.add(ItemTags.LOGS);

            if (flammable) {
                blockTags.add(BlockTags.LOGS_THAT_BURN);
                itemTags.add(ItemTags.LOGS_THAT_BURN);
            }
        }
    }
}
