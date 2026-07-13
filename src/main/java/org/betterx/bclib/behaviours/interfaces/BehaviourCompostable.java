package org.betterx.bclib.behaviours.interfaces;

/**
 * Marker interface for blocks that can be composted.
 * <p>
 * {@code BCLAutoItemTagProvider} adds the {@link org.betterx.wover.tag.api.predefined.CommonItemTags#COMPOSTABLE}
 * tag to the items of all blocks that implement this interface, at datagen time.
 * <p>
 * The actual composting chance registered with vanilla's composter is configured separately, via
 * {@link org.betterx.bclib.trait.block.CompostableBlockTrait} on the block's own registration -
 * this interface's {@link #compostingChance()} is not consulted for that.
 */
public interface BehaviourCompostable extends BlockBehaviour {

    /**
     * The chance that this block will be composted.
     * <p>
     * The default value is 0.1f.
     *
     * @return The chance that this block will be composted.
     */
    default float compostingChance() {
        return 0.1f;
    }
}
