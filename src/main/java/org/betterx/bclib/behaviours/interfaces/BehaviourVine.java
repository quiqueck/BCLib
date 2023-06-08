package org.betterx.bclib.behaviours.interfaces;

import org.betterx.bclib.interfaces.tools.AddMineableHoe;
import org.betterx.bclib.interfaces.tools.AddMineableShears;

/**
 * Interface for blocks that are vines.
 * <p>
 * This will add the {@link AddMineableShears}, {@link AddMineableHoe} and {@link BehaviourCompostable} behaviours.
 */
public interface BehaviourVine extends AddMineableShears, AddMineableHoe, BehaviourPlantLike, BehaviourCompostable, BehaviourClimable {
}
