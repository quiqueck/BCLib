package org.betterx.bclib.trait;

import de.ambertation.wover.block.api.model.ModelTraitLibrary;
import de.ambertation.wover.block.api.client.trait.ClientBlockTraits;
import de.ambertation.wover.block.api.trait.BlockTrait;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Generic combinators for the {@code List<BlockTrait<?, ?>>} that block {@code register}/{@code define}
 * helpers take, so a block can be handed several unrelated rules (a ground rule, a render layer, a
 * {@link ModelTraitLibrary} model) through a single list parameter.
 * <p>
 * The client-only trait builders ({@link ModelTraitLibrary}, {@link ClientBlockTraits#RENDER_LAYER}) return
 * {@code null} outside the client/datagen, so these lists must tolerate a null element - {@link List#of}
 * would throw on one. Nulls are dropped here, so the returned lists are always null-free.
 */
public class TraitLists {
    /** A null-free list of the given traits; any {@code null} (client-only, off-client) is dropped. */
    @SafeVarargs
    public static List<BlockTrait<?, ?>> of(BlockTrait<?, ?>... traits) {
        return and(List.of(), traits);
    }

    /** {@code a} followed by {@code b}, dropping any {@code null}. */
    public static List<BlockTrait<?, ?>> concat(List<BlockTrait<?, ?>> a, List<BlockTrait<?, ?>> b) {
        final List<BlockTrait<?, ?>> combined = new ArrayList<>(a.size() + b.size());
        a.stream().filter(t -> t != null).forEach(combined::add);
        b.stream().filter(t -> t != null).forEach(combined::add);
        return combined;
    }

    /** {@code base} plus the given extra traits, dropping any {@code null}. */
    @SafeVarargs
    public static List<BlockTrait<?, ?>> and(List<BlockTrait<?, ?>> base, BlockTrait<?, ?>... extra) {
        final List<BlockTrait<?, ?>> combined = new ArrayList<>(base.size() + extra.length);
        combined.addAll(base);
        Arrays.stream(extra).filter(t -> t != null).forEach(combined::add);
        return combined;
    }
}
