package org.betterx.bclib.blocks;

import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.PatternsHelper;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class BaseBarkBlock extends BaseRotatedPillarBlock {
    public BaseBarkBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected Optional<String> createBlockPattern(ResourceLocation blockId) {
        blockId = Registry.BLOCK.getKey(this);
        return PatternsHelper.createJson(BasePatterns.BLOCK_BASE, replacePath(blockId));
    }

    private ResourceLocation replacePath(ResourceLocation blockId) {
        String newPath = blockId.getPath().replace("_bark", "_log_side");
        return new ResourceLocation(blockId.getNamespace(), newPath);
    }
}
