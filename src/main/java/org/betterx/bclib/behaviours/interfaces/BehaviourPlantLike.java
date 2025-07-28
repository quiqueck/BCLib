package org.betterx.bclib.behaviours.interfaces;

import org.betterx.bclib.trait.block.PlantBlockTrait;
import org.betterx.bclib.trait.block.WaterPlantBlockTrait;
import org.betterx.wover.block.api.trait.BlockTrait;
import org.betterx.wover.tabs.api.interfaces.CreativeTabPredicate;

import net.minecraft.world.item.BlockItem;

public interface BehaviourPlantLike extends BlockBehaviour {
    CreativeTabPredicate TAB_PREDICATE = item -> item instanceof BlockItem bi
            && (
            bi.getBlock() instanceof BehaviourPlantLike
                    || bi.getBlock() instanceof BehaviourLeaves
                    || BlockTrait.hasRuntimeTrait(bi.getBlock(), PlantBlockTrait.KEY)
                    || BlockTrait.hasRuntimeTrait(bi.getBlock(), WaterPlantBlockTrait.KEY)
    );
}
