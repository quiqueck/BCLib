package org.betterx.bclib.behaviours.interfaces;

import org.betterx.bclib.trait.block.PlantBlockTrait;
import org.betterx.bclib.trait.block.PlantLikeBlockTrait;
import org.betterx.bclib.trait.block.WaterPlantBlockTrait;
import org.betterx.wover.block.api.trait.BlockTrait;
import org.betterx.wover.tabs.api.interfaces.CreativeTabPredicate;

import net.minecraft.world.item.BlockItem;

public interface BehaviourPlantLike extends BlockBehaviour {
    /**
     * Matches everything that belongs in the creative "Plants"/nature tab of BetterNether and BetterEnd.
     * <p>
     * The {@code instanceof} branches are kept alongside the trait lookups on purpose: both mods still carry
     * the {@code Behaviour*} marker interfaces, so the predicate has to keep matching them until every
     * consumer has migrated to {@link PlantLikeBlockTrait}. {@code BehaviourLeaves} is listed explicitly
     * because it is not itself a {@code BehaviourPlantLike}.
     */
    CreativeTabPredicate TAB_PREDICATE = item -> item instanceof BlockItem bi
            && (
            bi.getBlock() instanceof BehaviourPlantLike
                    || bi.getBlock() instanceof BehaviourLeaves
                    || BlockTrait.hasRuntimeTrait(bi.getBlock(), PlantLikeBlockTrait.KEY)
                    || BlockTrait.hasRuntimeTrait(bi.getBlock(), PlantBlockTrait.KEY)
                    || BlockTrait.hasRuntimeTrait(bi.getBlock(), WaterPlantBlockTrait.KEY)
    );
}
