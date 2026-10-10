package org.betterx.bclib.api.v2.levelgen.structures;

import org.betterx.bclib.util.NbtHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import com.google.common.collect.Maps;

import java.util.Map;

public class StructureWorld {
    private final Map<ChunkPos, Part> parts = Maps.newHashMap();
    private ChunkPos lastPos;
    private Part lastPart;
    private int minX = Integer.MAX_VALUE;
    private int minY = Integer.MAX_VALUE;
    private int minZ = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int maxY = Integer.MIN_VALUE;
    private int maxZ = Integer.MIN_VALUE;

    public StructureWorld() {
    }

    public StructureWorld(CompoundTag tag) {
        minX = tag.getInt("minX").orElse(Integer.MAX_VALUE);
        maxX = tag.getInt("maxX").orElse(Integer.MIN_VALUE);
        minY = tag.getInt("minY").orElse(Integer.MAX_VALUE);
        maxY = tag.getInt("maxY").orElse(Integer.MIN_VALUE);
        minZ = tag.getInt("minZ").orElse(Integer.MAX_VALUE);
        maxZ = tag.getInt("maxZ").orElse(Integer.MIN_VALUE);

        tag.getList("parts").ifPresent(map -> {
            map.forEach((element) -> {
                CompoundTag compound = (CompoundTag) element;
                Part part = new Part(compound);
                int x = compound.getInt("x").orElse(0);
                int z = compound.getInt("z").orElse(0);
                parts.put(new ChunkPos(x, z), part);
            });
        });
    }

    public void setBlock(BlockPos pos, BlockState state) {
        ChunkPos cPos = new ChunkPos(pos);

        if (cPos.equals(lastPos)) {
            lastPart.addBlock(pos, state);
            return;
        }

        Part part = parts.get(cPos);
        if (part == null) {
            part = new Part();
            parts.put(cPos, part);

            if (cPos.x < minX) minX = cPos.x;
            if (cPos.x > maxX) maxX = cPos.x;
            if (cPos.z < minZ) minZ = cPos.z;
            if (cPos.z > maxZ) maxZ = cPos.z;
        }
        if (pos.getY() < minY) minY = pos.getY();
        if (pos.getY() > maxY) maxY = pos.getY();
        part.addBlock(pos, state);

        lastPos = cPos;
        lastPart = part;
    }

    public boolean placeChunk(WorldGenLevel world, ChunkPos chunkPos) {
        Part part = parts.get(chunkPos);
        if (part != null) {
            ChunkAccess chunk = world.getChunk(chunkPos.x, chunkPos.z);
            part.placeChunk(chunk);
            return true;
        }
        return false;
    }

    public CompoundTag toBNT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("minX", minX);
        tag.putInt("maxX", maxX);
        tag.putInt("minY", minY);
        tag.putInt("maxY", maxY);
        tag.putInt("minZ", minZ);
        tag.putInt("maxZ", maxZ);
        ListTag map = new ListTag();
        tag.put("parts", map);
        parts.forEach((pos, part) -> {
            map.add(part.toNBT(pos.x, pos.z));
        });
        return tag;
    }

    public BoundingBox getBounds() {
        if (minX == Integer.MAX_VALUE || maxX == Integer.MIN_VALUE || minZ == Integer.MAX_VALUE || maxZ == Integer.MIN_VALUE) {
            return BoundingBox.infinite();
        }
        return new BoundingBox(minX << 4, minY, minZ << 4, (maxX << 4) | 15, maxY, (maxZ << 4) | 15);
    }

    private static final class Part {
        // Compact block layout: (x & 15) << 28 | (z & 15) << 24 | (y & 0xFFF) << 12 | paletteIndex
        // y is a signed 12-bit value, which covers the whole possible build height (-2032..2031)
        private static final int PACKED_MIN_Y = -2048;
        private static final int PACKED_MAX_Y = 2047;
        private static final int PACKED_INDEX_MASK = 0xFFF;

        Map<BlockPos, BlockState> blocks = Maps.newHashMap();

        private static int pack(BlockPos pos, int stateID) {
            return (pos.getX() & 15) << 28 | (pos.getZ() & 15) << 24 | (pos.getY() & 0xFFF) << 12 | stateID;
        }

        private static BlockPos unpackPos(int value) {
            return new BlockPos((value >>> 28) & 15, (value << 8) >> 20, (value >>> 24) & 15);
        }

        public Part() {
        }

        public Part(CompoundTag tag) {
            ListTag states = tag.getList("states").orElse(new ListTag());
            final BlockState[] blockStates = new BlockState[states.size()];
            for (int i = 0; i < blockStates.length; i++) {
                blockStates[i] = NbtUtils.readBlockState(
                        BuiltInRegistries.BLOCK,
                        (CompoundTag) states.get(i)
                );
            }

            final int[] packed = tag.getIntArray("packed").orElse(null);
            if (packed != null) {
                for (int value : packed) {
                    final int stateID = value & PACKED_INDEX_MASK;
                    if (stateID < blockStates.length) {
                        this.blocks.put(unpackPos(value), blockStates[stateID]);
                    }
                }
                return;
            }

            // Legacy format: one compound per block
            tag.getList("blocks").ifPresent(blocks -> {
                blocks.forEach((element) -> {
                    CompoundTag block = (CompoundTag) element;
                    BlockPos pos = NbtHelper.readBlockPos(block, "pos").orElse(null);
                    if (pos != null) {
                        block.getInt("state").ifPresent(stateID -> {
                            BlockState state = stateID < blockStates.length
                                    ? blockStates[stateID]
                                    : Block.stateById(stateID);
                            this.blocks.put(pos, state);
                        });
                    }
                });
            });
        }

        void addBlock(BlockPos pos, BlockState state) {
            BlockPos inner = new BlockPos(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
            blocks.put(inner, state);
        }

        void placeChunk(ChunkAccess chunk) {
            blocks.forEach((pos, state) -> {
                //ChunkAccess#setBlockState does not remove block entities, so a block a feature placed
                //earlier (e.g. a chest or skull) would leave its block entity behind under the new block
                final BlockState old = chunk.setBlockState(pos, state);
                if (old != null && old.hasBlockEntity() && !old.is(state.getBlock())) {
                    //pos is chunk-local, block entities are keyed by their world position
                    chunk.removeBlockEntity(chunk.getPos().getBlockAt(pos.getX(), pos.getY(), pos.getZ()));
                }
            });
        }

        CompoundTag toNBT(int x, int z) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("x", x);
            tag.putInt("z", z);
            ListTag stateMap = new ListTag();
            tag.put("states", stateMap);

            Map<BlockState, Integer> states = Maps.newHashMap();
            boolean canPack = true;
            for (Map.Entry<BlockPos, BlockState> e : blocks.entrySet()) {
                if (!states.containsKey(e.getValue())) {
                    states.put(e.getValue(), states.size());
                    stateMap.add(NbtUtils.writeBlockState(e.getValue()));
                }
                final int y = e.getKey().getY();
                if (y < PACKED_MIN_Y || y > PACKED_MAX_Y) canPack = false;
            }
            if (states.size() > PACKED_INDEX_MASK + 1) canPack = false;

            if (canPack) {
                // Compact format: one int per block, see pack()
                final int[] packed = new int[blocks.size()];
                int i = 0;
                for (Map.Entry<BlockPos, BlockState> e : blocks.entrySet()) {
                    packed[i++] = pack(e.getKey(), states.get(e.getValue()));
                }
                tag.putIntArray("packed", packed);
            } else {
                // Does not fit the compact layout, use the (palette-indexed) legacy format
                ListTag map = new ListTag();
                tag.put("blocks", map);
                blocks.forEach((pos, state) -> {
                    CompoundTag block = new CompoundTag();
                    block.put("pos", NbtHelper.writeBlockPos(pos));
                    block.putInt("state", states.get(state));
                    map.add(block);
                });
            }

            return tag;
        }
    }
}
