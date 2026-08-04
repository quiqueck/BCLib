package org.betterx.bclib.blocks;


import net.minecraft.world.level.block.TransparentBlock;

/**
 * A translucent, non-occluding full-cube glass block.
 * <p>
 * Extends vanilla {@link TransparentBlock} (the same class {@code GlassBlock}/{@code StainedGlassBlock}
 * derive from), which supplies all of the glass rendering behaviour that used to be hand-written here:
 * full shade brightness, skylight propagation, same-block face culling ({@code skipRendering}) and an
 * empty visual shape. The translucent render layer and silk-touch loot are supplied by traits at the
 * registration site.
 * <p>
 * The constructors used to mutate the incoming {@code Properties} (an R1 violation) and applied only three
 * of vanilla glass's five property calls, which is what let {@code isRedstoneConductor}/{@code isValidSpawn}
 * leak in from whatever parent block the properties were copied from. The whole recipe - resistance
 * included - now lives in {@link org.betterx.bclib.trait.block.GlassBlockTrait}, applied at the definition
 * site. What is left here adds nothing to {@link TransparentBlock} beyond a distinct class name in the
 * block-properties report, and is a candidate for deletion in its own right.
 */
public class BaseGlassBlock extends TransparentBlock {
    public BaseGlassBlock(Properties settings) {
        super(settings);
    }
}
