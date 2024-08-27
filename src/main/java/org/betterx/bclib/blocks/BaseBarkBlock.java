package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.wover.block.api.BlockTagProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;
import org.betterx.wover.item.api.ItemTagProvider;
import org.betterx.wover.tag.api.event.context.ItemTagBootstrapContext;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;

import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public abstract class BaseBarkBlock extends BaseRotatedPillarBlock {
    protected BaseBarkBlock(Properties settings) {
        super(settings);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void provideBlockModels(WoverBlockModelGenerators generator) {
        var res = TextureMapping.getBlockTexture(this);
        var log = ResourceLocation.fromNamespaceAndPath(res.getNamespace(), res
                .getPath()
                .replace("_bark", "_log"));
        generator.createRotatedPillar(this, new TextureMapping()
                .put(TextureSlot.SIDE, log.withSuffix("_side"))
                .put(TextureSlot.END, log.withSuffix("_side")));

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
