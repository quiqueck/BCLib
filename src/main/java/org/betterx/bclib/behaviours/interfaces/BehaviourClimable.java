package org.betterx.bclib.behaviours.interfaces;

/**
 * Interface for blocks that can be climbed.
 * <p>
 * {@link org.betterx.bclib.api.v2.PostInitAPI} will add the {@link net.minecraft.tags.BlockTags#CLIMBABLE} tag to all blocks that
 * implement this interface.
 */
public interface BehaviourClimable extends BlockBehaviour{
}
