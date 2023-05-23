package org.betterx.bclib.behaviours;

import org.betterx.bclib.behaviours.interfaces.*;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

import java.util.function.BiFunction;
import java.util.function.Function;

public class BehaviourHelper {
    public static boolean isStone(Block source) {
        return source instanceof BehaviourStone || source.defaultBlockState()
                                                         .instrument()
                                                         .equals(NoteBlockInstrument.BASEDRUM);
    }

    public static boolean isStone(BlockSetType type) {
        return type.soundType() == SoundType.STONE;
    }

    public static boolean isMetal(Block source) {
        return source instanceof BehaviourMetal;
    }

    public static boolean isMetal(BlockSetType type) {
        return type.soundType() == SoundType.METAL;
    }

    public static boolean isWood(Block source) {
        return source instanceof BehaviourWood;
    }

    public static boolean isWood(BlockSetType type) {
        return type.soundType() == SoundType.WOOD;
    }

    public static boolean isObsidian(Block source) {
        return source instanceof BehaviourObsidian;
    }

    public static boolean isGlass(Block source) {
        return source instanceof BehaviourGlass;
    }

    public static <T> T from(
            Block source,
            Function<Block, T> woodSupplier,
            Function<Block, T> stoneSupplier
    ) {
        return from(source, woodSupplier, stoneSupplier, null, null, null);
    }

    public static <T> T from(
            Block source,
            Function<Block, T> woodSupplier,
            Function<Block, T> stoneSupplier,
            Function<Block, T> metalSupplier
    ) {
        return from(source, woodSupplier, stoneSupplier, metalSupplier, null, null);
    }

    public static <T> T from(
            Block source,
            Function<Block, T> woodSupplier,
            Function<Block, T> stoneSupplier,
            Function<Block, T> metalSupplier,
            Function<Block, T> obsidianSupplier,
            Function<Block, T> glassSupplier
    ) {
        if (metalSupplier != null && BehaviourHelper.isMetal(source))
            return metalSupplier.apply(source);
        if (stoneSupplier != null && BehaviourHelper.isStone(source))
            return stoneSupplier.apply(source);
        if (glassSupplier != null && BehaviourHelper.isGlass(source))
            return glassSupplier.apply(source);
        if (obsidianSupplier != null && BehaviourHelper.isObsidian(source))
            return obsidianSupplier.apply(source);


        if (woodSupplier != null)
            return woodSupplier.apply(source);
        //fallback if no wood supplier is present
        if (stoneSupplier != null)
            return stoneSupplier.apply(source);
        //fallback if neither wood or stone suppliers are present
        if (metalSupplier != null)
            return metalSupplier.apply(source);
        if (glassSupplier != null)
            return glassSupplier.apply(source);
        if (obsidianSupplier != null)
            return obsidianSupplier.apply(source);
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
