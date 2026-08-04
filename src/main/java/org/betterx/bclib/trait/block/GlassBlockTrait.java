package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import de.ambertation.wover.block.api.BlockDefinition;
import de.ambertation.wover.block.api.trait.*;
import de.ambertation.wover.block.impl.trait.BlockTraitImpl;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Vanilla's glass property recipe, applied at the block's definition site (R1: no class may alter the
 * {@code BlockBehaviour.Properties} handed to its constructor).
 * <p>
 * {@code Blocks.GLASS} configures <em>five</em> things, and a see-through full-cube block needs all five:
 * <pre>noOcclusion().isValidSpawn(never).isRedstoneConductor(never).isSuffocating(never).isViewBlocking(never)</pre>
 * The retired {@code BaseGlassBlock} constructors only ever set three of them, so every glass block that
 * copied its properties from an opaque parent (e.g. {@code quartz_glass_framed} off
 * {@code cincinnasite_block}) silently inherited that parent's <em>defaults</em> for the missing two and
 * went on conducting redstone and hosting mob spawns. The blocks that happened to look correct only did so
 * because they copied {@code Blocks.GLASS}, which sets them - not because anything here did.
 * <p>
 * Both missing predicates default to a shape test - {@code isCollisionShapeFullBlock} for
 * {@code isRedstoneConductor}, a sturdy up-face for {@code isValidSpawn} - which a full-cube glass block
 * passes. That is why the glass <em>panes</em> were never affected: their shape fails the test on its own.
 * Only full-cube glass needs this trait.
 * <p>
 * {@code isValidSpawn} is the reason this is a trait rather than a helper method. The other three take
 * {@link Blocks#never}, but {@code isValidSpawn} is a 4-arg {@code StateArgumentPredicate} and vanilla has
 * no {@code never} equivalent for it - it inlines a lambda at each site. Writing that lambda once, here, is
 * what keeps it from being copy-pasted to every glass definition site.
 * <p>
 * Note that {@link PlantBlockTrait} and {@link PathBlockTrait} deliberately keep their own
 * {@code isValidSpawn} lambdas: the plants opt <em>in</em> to spawning ({@code -> true}), which is the
 * opposite intent.
 *
 * @see LeavesBlockTrait the same {@code configure()}-overriding pattern for leaves
 */
public class GlassBlockTrait extends BlockTraitImpl<Block, GenericBlockTrait> {
    private static final BlockTraitKey KEY = BlockTraitKey.ofUnique(BCLib.C, "glass");

    /**
     * Sentinel for "do not touch {@code explosionResistance}", so a definition site that already sets its
     * own strength keeps it. A trait's {@code configure()} runs <em>after</em> every chained setter, so an
     * unconditional {@code explosionResistance()} here would silently clobber a chained
     * {@code .strength(destroyTime, resistance)}.
     */
    private static final float KEEP_RESISTANCE = Float.NaN;

    private final float resistance;
    private final boolean conductsRedstone;

    /**
     * The full vanilla glass recipe, leaving {@code explosionResistance} to the definition site.
     */
    public static GlassBlockTrait glass() {
        return new GlassBlockTrait(KEEP_RESISTANCE, false);
    }

    /**
     * The full vanilla glass recipe, plus the explosion resistance the retired {@code BaseGlassBlock}
     * constructors used to apply (0.3 for every caller in BetterNether).
     */
    public static GlassBlockTrait glass(float resistance) {
        return new GlassBlockTrait(resistance, false);
    }

    /**
     * A see-through full-cube block that stays a <em>redstone conductor</em>: no mob spawning, no
     * suffocation, no view blocking, but wiring still runs through it. Vanilla has no block like this
     * (its see-through full cubes are all glass, which insulates), so this is deliberately a separate
     * factory rather than a flag on {@link #glass()} - a caller has to opt into the odd combination.
     * <p>
     * Used by BetterNether's {@code cincinnasite_frame}: a metal lattice with real holes in its texture,
     * so the glass treatment fits everywhere except conduction, which it keeps by design.
     */
    public static GlassBlockTrait seeThroughConductor() {
        return new GlassBlockTrait(KEEP_RESISTANCE, true);
    }

    private GlassBlockTrait(float resistance, boolean conductsRedstone) {
        this.resistance = resistance;
        this.conductsRedstone = conductsRedstone;
    }

    @Override
    public BlockTraitKey key() {
        return KEY;
    }

    @Override
    public void configure(BlockDefinition<Block, ? extends BlockDefinition<Block, ?>> definition) {
        super.configure(definition);

        definition
                .noOcclusion()
                .isValidSpawn((state, level, pos, type) -> false)
                .isSuffocating(Blocks::never)
                .isViewBlocking(Blocks::never);

        if (!conductsRedstone) definition.isRedstoneConductor(Blocks::never);
        if (!Float.isNaN(resistance)) definition.explosionResistance(resistance);
    }
}
