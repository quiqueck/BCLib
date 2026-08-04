package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import de.ambertation.wover.block.api.BlockDefinition;
import de.ambertation.wover.block.api.trait.BlockTraitKey;
import de.ambertation.wover.block.api.trait.GenericBlockTrait;
import de.ambertation.wover.block.impl.trait.BlockTraitImpl;

import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.registry.FuelRegistryEvents;

/**
 * A behaviour trait that registers the block's item as a furnace fuel with a fixed burn time.
 * <p>
 * This mirrors the (now-removed) {@code NetherBlocks.addFuel(source, result)} helper, which did
 * <pre>{@code FuelRegistryEvents.BUILD.register((builder, ctx) -> builder.add(result, 40));}</pre>
 * The trait performs exactly that registration (same registry: Fabric's {@link FuelRegistryEvents#BUILD},
 * same timing: at block-registration time via {@link #afterBlockRegistration}), so a block that carries
 * this trait becomes a valid furnace fuel identically to the old code path.
 * <p>
 * <b>Burn-time source:</b> BetterNether registered every slab/stairs/roof/button/plate/etc. fuel with a
 * hard-coded {@code 40} ticks (the {@code addFuel} body), which is what {@link #DEFAULT_TICKS} / {@link #withDefault()}
 * emit. Use {@link #withTicks(int)} for any other value.
 * <p>
 * <b>Not verifiable by datagen:</b> furnace fuel is a runtime-only Fabric registration and is <em>not</em>
 * reflected in any golden datapack file (the {@code fuel=} column in the registrations golden reads {@code ?}).
 * Equivalence with the old {@code addFuel} path therefore rests on the call being byte-identical, not on a
 * golden diff. Per user decision 6, the caller (Phase 5) is responsible for <em>which</em> blocks get this
 * trait: the old {@code addFuel} guarded on {@code source.ignitedByLava()}; the trait itself is
 * unconditional, so the guard becomes an explicit per-site {@code .addTrait(...)} decision.
 */
public class FuelBlockTrait extends BlockTraitImpl<Block, GenericBlockTrait> implements GenericBlockTrait {
    public static final BlockTraitKey KEY = BlockTraitKey.ofUnique(BCLib.C, "fuel");

    /**
     * The burn time (in ticks) BetterNether's {@code addFuel} used for every generated fuel block.
     */
    public static final int DEFAULT_TICKS = 40;

    private static final FuelBlockTrait DEFAULT = new FuelBlockTrait(DEFAULT_TICKS);

    /**
     * The fuel trait with the standard {@link #DEFAULT_TICKS} (40 tick) burn time used by all of
     * BetterNether's slab/stairs/roof/button/plate fuel registrations.
     *
     * @return the default fuel trait instance
     */
    public static FuelBlockTrait withDefault() {
        return DEFAULT;
    }

    /**
     * A fuel trait with a custom burn time.
     *
     * @param ticks the burn time in ticks (as passed to {@code FuelRegistryBuilder.add})
     * @return the fuel trait instance
     */
    public static FuelBlockTrait withTicks(int ticks) {
        return ticks == DEFAULT_TICKS ? DEFAULT : new FuelBlockTrait(ticks);
    }

    public final int ticks;

    private FuelBlockTrait(int ticks) {
        this.ticks = ticks;
    }

    @Override
    public BlockTraitKey key() {
        return KEY;
    }

    @Override
    public GenericBlockTrait forRuntime() {
        return this;
    }

    @Override
    public void afterBlockRegistration(
            Block block,
            BlockDefinition<Block, ? extends BlockDefinition<Block, ?>> definition
    ) {
        // Byte-identical to the old NetherBlocks.addFuel body (the block/item is resolved lazily
        // when the BUILD event fires at world load, so registering the callback here - at the same
        // registration phase addFuel ran in - is safe and matches the previous timing exactly).
        FuelRegistryEvents.BUILD.register((builder, fuelContext) -> builder.add(block, ticks));
    }
}
