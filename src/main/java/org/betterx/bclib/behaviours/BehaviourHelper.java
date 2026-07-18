package org.betterx.bclib.behaviours;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

import java.util.function.BiFunction;
import java.util.function.Function;

public class BehaviourHelper {
    public static boolean isStone(Block source) {
        return source.defaultBlockState().instrument().equals(NoteBlockInstrument.BASEDRUM);
    }

    public static boolean isStone(BlockSetType type) {
        return type.soundType() == SoundType.STONE;
    }

    /**
     * Always {@code false}: this used to check {@code instanceof BehaviourMetal}, a marker nothing outside
     * bclib's own dead nested classes ever implemented, so the check was already unreachable for every real
     * "source" block passed to {@link #from}. It is intentionally NOT wired to
     * {@code BlockTrait.hasRuntimeTrait(source, BlockTraits.METAL_BLOCK.key())}: unlike the {@code instanceof}
     * check, that trait lookup is NOT always-false today - e.g. BetterNether's {@code CINCINNASITE_SLAB}/
     * {@code CINCINNASITE_FORGED} (both {@code NetherMaterial.cincinnasite()}, which includes
     * {@code BlockTraits.METAL_BLOCK}) are passed as the {@code source} to {@code BaseChair.from}/
     * {@code BaseBarStool.from}/{@code BaseTaburet.from}, which currently resolve to the {@code Wood} variant
     * (confirmed against the committed {@code block_properties.txt}: {@code chair_cincinnasite}/
     * {@code bar_stool_cincinnasite}/{@code taburet_cincinnasite} all show {@code class=Wood}). Wiring the
     * trait check here would silently flip them to {@code Metal} (and add an extra
     * {@code minecraft:mineable/pickaxe} tag on top of the axe/pickaxe tag each already gets explicitly at
     * its registration site) - exactly the kind of silent material-classification change this method must
     * not cause. Revisit deliberately (as its own change, with its own golden-file review) if this
     * classification is ever desired.
     */
    public static boolean isMetal(Block source) {
        return false;
    }

    public static boolean isMetal(BlockSetType type) {
        return type.soundType() == SoundType.METAL;
    }

    public static boolean isWood(BlockSetType type) {
        return type.soundType() == SoundType.WOOD;
    }

    public static <T> T from(
            Block source,
            Function<Block, T> woodSupplier,
            Function<Block, T> stoneSupplier
    ) {
        return from(source, woodSupplier, stoneSupplier, null);
    }

    public static <T> T from(
            Block source,
            Function<Block, T> woodSupplier,
            Function<Block, T> stoneSupplier,
            Function<Block, T> metalSupplier
    ) {
        if (metalSupplier != null && BehaviourHelper.isMetal(source))
            return metalSupplier.apply(source);
        if (stoneSupplier != null && BehaviourHelper.isStone(source))
            return stoneSupplier.apply(source);

        if (woodSupplier != null)
            return woodSupplier.apply(source);
        //fallback if no wood supplier is present
        if (stoneSupplier != null)
            return stoneSupplier.apply(source);
        //fallback if neither wood or stone suppliers are present
        if (metalSupplier != null)
            return metalSupplier.apply(source);
        return null;
    }

    public static <T> T from(
            Block source,
            BlockSetType type,
            BiFunction<Block, BlockSetType, T> woodSupplier,
            BiFunction<Block, BlockSetType, T> stoneSupplier
    ) {
        return from(source, type, woodSupplier, stoneSupplier, null);
    }

    public static <T> T from(
            Block source,
            BlockSetType type,
            BiFunction<Block, BlockSetType, T> woodSupplier,
            BiFunction<Block, BlockSetType, T> stoneSupplier,
            BiFunction<Block, BlockSetType, T> metalSupplier
    ) {
        if (metalSupplier != null && BehaviourHelper.isMetal(type))
            return metalSupplier.apply(source, type);
        if (stoneSupplier != null && BehaviourHelper.isStone(type))
            return stoneSupplier.apply(source, type);

        if (woodSupplier != null)
            return woodSupplier.apply(source, type);
        //fallback if no wood supplier is present
        if (stoneSupplier != null)
            return stoneSupplier.apply(source, type);
        //fallback if neither wood or stone suppliers are present
        if (metalSupplier != null)
            return metalSupplier.apply(source, type);
        return null;
    }
}
