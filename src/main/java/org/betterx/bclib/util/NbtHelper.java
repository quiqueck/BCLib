package org.betterx.bclib.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;

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

    /**
     * Reads a block state that was written by {@link NbtUtils#writeBlockState(BlockState)} of any Minecraft
     * version.
     * <p>
     * 26.3 renamed the fields from {@code {Name, Properties}} to {@code {id, properties}}, and
     * {@link NbtUtils#readBlockState(HolderGetter, CompoundTag)} only knows the new names: a state saved by
     * 26.2 or older comes back as air. Vanilla's datafixer renames the fields in chunk palettes and other
     * vanilla data, but not in custom data that mods keep in their own tags (structure pieces, for example),
     * so everything a mod saved itself has to be read through this method.
     */
    public static BlockState readBlockState(HolderGetter<Block> blocks, CompoundTag tag) {
        if (!tag.contains(StateHolder.ID_TAG) && tag.contains(NbtUtils.LEGACY_BLOCK_STATE_ID_TAG)) {
            final CompoundTag modern = new CompoundTag();
            modern.put(StateHolder.ID_TAG, tag.get(NbtUtils.LEGACY_BLOCK_STATE_ID_TAG));
            tag.getCompound(NbtUtils.LEGACY_BLOCKSTATE_PROPERTY_TAG)
               .ifPresent(properties -> modern.put(StateHolder.PROPERTIES_TAG, properties));
            tag = modern;
        }
        return NbtUtils.readBlockState(blocks, tag);
    }
}
