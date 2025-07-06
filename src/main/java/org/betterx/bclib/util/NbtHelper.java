package org.betterx.bclib.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;

import java.util.Optional;

public class NbtHelper {
    public static Optional<BlockPos> readBlockPos(CompoundTag compoundTag, String string) {
        int[] is = compoundTag.getIntArray(string).orElse(null);
        if (is != null && is.length == 3) {
            return Optional.of(new BlockPos(is[0], is[1], is[2]));
        }
        return Optional.empty();
    }

    public static Tag writeBlockPos(BlockPos blockPos) {
        return new IntArrayTag(new int[]{blockPos.getX(), blockPos.getY(), blockPos.getZ()});
    }
}
