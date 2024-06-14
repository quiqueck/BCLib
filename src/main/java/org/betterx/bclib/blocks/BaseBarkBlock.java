package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.wover.block.api.BlockTagProvider;
import org.betterx.wover.item.api.ItemTagProvider;
import org.betterx.wover.tag.api.event.context.ItemTagBootstrapContext;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;

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
        return ResourceLocation.fromNamespaceAndPath(blockId.getNamespace(), newPath);
    }

    public static class Wood extends BaseBarkBlock implements BehaviourWood, BlockTagProvider, ItemTagProvider {
        private final boolean flammable;

        public Wood(Properties settings, boolean flammable) {
            super(flammable ? settings.ignitedByLava() : settings);
            this.flammable = flammable;
        }

        @Override
        public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
            context.add(BlockTags.LOGS, this);
            if (flammable) {
                context.add(BlockTags.LOGS_THAT_BURN, this);
            }
        }

        @Override
        public void registerItemTags(ResourceLocation location, ItemTagBootstrapContext context) {
            context.add(ItemTags.LOGS, this);
            if (flammable) {
                context.add(ItemTags.LOGS_THAT_BURN, this);
            }
        }
    }
}
