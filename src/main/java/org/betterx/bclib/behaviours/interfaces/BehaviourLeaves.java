package org.betterx.bclib.behaviours.interfaces;

import org.betterx.bclib.interfaces.tools.AddMineableHoe;
import org.betterx.bclib.interfaces.tools.AddMineableShears;

/**
 * Interface for leaves blocks.
 * <p>
 * Adds composting chance, mineable with shears and hoe.
 */
public interface BehaviourLeaves extends AddMineableShears, AddMineableHoe, BehaviourCompostable {
    @Override
    default float compostingChance() {
        return 0.3f;
    }
}
