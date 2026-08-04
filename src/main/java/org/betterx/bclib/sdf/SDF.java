package org.betterx.bclib.sdf;

import org.betterx.bclib.api.v2.levelgen.structures.StructureWorld;
import org.betterx.bclib.util.BlocksHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.function.Function;

public abstract class SDF {
    private final List<Function<PosInfo, BlockState>> postProcesses = Lists.newArrayList();
    private Function<BlockState, Boolean> canReplace = (state) -> {
        return state.canBeReplaced();
    };

    public abstract float getDistance(float x, float y, float z);

    public abstract BlockState getBlockState(BlockPos pos);

    public SDF addPostProcess(Function<PosInfo, BlockState> postProcess) {
        this.postProcesses.add(postProcess);
        return this;
    }

    public SDF setReplaceFunction(Function<BlockState, Boolean> canReplace) {
        this.canReplace = canReplace;
        return this;
    }

    public void fillRecursive(ServerLevelAccessor world, BlockPos start) {
        fillRecursive(world, start, null);
    }

    /**
     * @param writeBounds when non-null, the flood-fill neither reads nor propagates through world positions
     *                    outside these bounds - an unbounded flood only ever explores through
     *                    {@code canReplace} positions inside the SDF's own shape, but during worldgen that
     *                    shape can still reach past the chunks a feature may touch (a tilted/tall SDF, a
     *                    generous {@code canReplace}), which is exactly what triggers
     *                    {@code WorldGenRegion}'s "unsafe terrain read" warning. Passing {@code null}
     *                    restores the original unbounded behavior.
     */
    public void fillRecursive(ServerLevelAccessor world, BlockPos start, BoundingBox writeBounds) {
        Map<BlockPos, PosInfo> mapWorld = Maps.newHashMap();
        Map<BlockPos, PosInfo> addInfo = Maps.newHashMap();
        Set<BlockPos> blocks = Sets.newHashSet();
        Set<BlockPos> ends = Sets.newHashSet();
        Set<BlockPos> add = Sets.newHashSet();
        ends.add(new BlockPos(0, 0, 0));
        boolean run = true;

        MutableBlockPos bPos = new MutableBlockPos();

        while (run) {
            for (BlockPos center : ends) {
                for (Direction dir : Direction.values()) {
                    bPos.set(center).move(dir);
                    BlockPos wpos = bPos.offset(start);
                    if (writeBounds != null && !writeBounds.isInside(wpos)) {
                        continue;
                    }

                    if (!blocks.contains(bPos) && canReplace.apply(world.getBlockState(wpos))) {
                        if (this.getDistance(bPos.getX(), bPos.getY(), bPos.getZ()) < 0) {
                            BlockState state = getBlockState(wpos);
                            PosInfo.create(mapWorld, addInfo, wpos).setState(state);
                            add.add(bPos.immutable());
                        }
                    }
                }
            }

            blocks.addAll(ends);
            ends.clear();
            ends.addAll(add);
            add.clear();

            run &= !ends.isEmpty();
        }

        List<PosInfo> infos = new ArrayList<PosInfo>(mapWorld.values());
        if (infos.size() > 0) {
            Collections.sort(infos);
            postProcesses.forEach((postProcess) -> {
                infos.forEach((info) -> {
                    info.setState(postProcess.apply(info));
                });
            });
            infos.forEach((info) -> {
                BlocksHelper.setWithoutUpdate(world, info.getPos(), info.getState());
            });

            infos.clear();
            infos.addAll(addInfo.values());
            Collections.sort(infos);
            postProcesses.forEach((postProcess) -> {
                infos.forEach((info) -> {
                    info.setState(postProcess.apply(info));
                });
            });
            infos.forEach((info) -> {
                if (canReplace.apply(world.getBlockState(info.getPos()))) {
                    BlocksHelper.setWithoutUpdate(world, info.getPos(), info.getState());
                }
            });
        }
    }

    public void fillArea(ServerLevelAccessor world, BlockPos center, AABB box) {
        Map<BlockPos, PosInfo> mapWorld = Maps.newHashMap();
        Map<BlockPos, PosInfo> addInfo = Maps.newHashMap();

        MutableBlockPos mut = new MutableBlockPos();
        for (int y = (int) box.minY; y <= box.maxY; y++) {
            mut.setY(y);
            for (int x = (int) box.minX; x <= box.maxX; x++) {
                mut.setX(x);
                for (int z = (int) box.minZ; z <= box.maxZ; z++) {
                    mut.setZ(z);
                    if (canReplace.apply(world.getBlockState(mut))) {
                        BlockPos fpos = mut.subtract(center);
                        if (this.getDistance(fpos.getX(), fpos.getY(), fpos.getZ()) < 0) {
                            PosInfo.create(mapWorld, addInfo, mut.immutable()).setState(getBlockState(mut));
                        }
                    }
                }
            }
        }

        List<PosInfo> infos = new ArrayList<PosInfo>(mapWorld.values());
        if (infos.size() > 0) {
            Collections.sort(infos);
            postProcesses.forEach((postProcess) -> {
                infos.forEach((info) -> {
                    info.setState(postProcess.apply(info));
                });
            });
            infos.forEach((info) -> {
                BlocksHelper.setWithoutUpdate(world, info.getPos(), info.getState());
            });

            infos.clear();
            infos.addAll(addInfo.values());
            Collections.sort(infos);
            postProcesses.forEach((postProcess) -> {
                infos.forEach((info) -> {
                    info.setState(postProcess.apply(info));
                });
            });
            infos.forEach((info) -> {
                if (canReplace.apply(world.getBlockState(info.getPos()))) {
                    BlocksHelper.setWithoutUpdate(world, info.getPos(), info.getState());
                }
            });
        }
    }

    public void fillRecursiveIgnore(ServerLevelAccessor world, BlockPos start, Function<BlockState, Boolean> ignore) {
        fillRecursiveIgnore(world, start, null, ignore);
    }

    /**
     * @param writeBounds when non-null, the flood-fill neither reads nor propagates through world positions
     *                    outside these bounds - see {@link #fillRecursive(ServerLevelAccessor, BlockPos,
     *                    BoundingBox)} for why an unbounded flood is unsafe during worldgen. Passing
     *                    {@code null} restores the original unbounded behavior.
     */
    public void fillRecursiveIgnore(
            ServerLevelAccessor world,
            BlockPos start,
            BoundingBox writeBounds,
            Function<BlockState, Boolean> ignore
    ) {
        Map<BlockPos, PosInfo> mapWorld = Maps.newHashMap();
        Map<BlockPos, PosInfo> addInfo = Maps.newHashMap();
        Set<BlockPos> blocks = Sets.newHashSet();
        Set<BlockPos> ends = Sets.newHashSet();
        Set<BlockPos> add = Sets.newHashSet();
        ends.add(new BlockPos(0, 0, 0));
        boolean run = true;

        MutableBlockPos bPos = new MutableBlockPos();

        while (run) {
            for (BlockPos center : ends) {
                for (Direction dir : Direction.values()) {
                    bPos.set(center).move(dir);
                    BlockPos wpos = bPos.offset(start);
                    if (writeBounds != null && !writeBounds.isInside(wpos)) {
                        continue;
                    }
                    BlockState state = world.getBlockState(wpos);
                    boolean ign = ignore.apply(state);
                    if (!blocks.contains(bPos) && (ign || canReplace.apply(state))) {
                        if (this.getDistance(bPos.getX(), bPos.getY(), bPos.getZ()) < 0) {
                            PosInfo.create(mapWorld, addInfo, wpos).setState(ign ? state : getBlockState(bPos));
                            add.add(bPos.immutable());
                        }
                    }
                }
            }

            blocks.addAll(ends);
            ends.clear();
            ends.addAll(add);
            add.clear();

            run &= !ends.isEmpty();
        }

        List<PosInfo> infos = new ArrayList<PosInfo>(mapWorld.values());
        if (infos.size() > 0) {
            Collections.sort(infos);
            postProcesses.forEach((postProcess) -> {
                infos.forEach((info) -> {
                    info.setState(postProcess.apply(info));
                });
            });
            infos.forEach((info) -> {
                BlocksHelper.setWithoutUpdate(world, info.getPos(), info.getState());
            });

            infos.clear();
            infos.addAll(addInfo.values());
            Collections.sort(infos);
            postProcesses.forEach((postProcess) -> {
                infos.forEach((info) -> {
                    info.setState(postProcess.apply(info));
                });
            });
            infos.forEach((info) -> {
                if (canReplace.apply(world.getBlockState(info.getPos()))) {
                    BlocksHelper.setWithoutUpdate(world, info.getPos(), info.getState());
                }
            });
        }
    }

    public void fillRecursive(StructureWorld world, BlockPos start) {
        Map<BlockPos, PosInfo> mapWorld = Maps.newHashMap();
        Map<BlockPos, PosInfo> addInfo = Maps.newHashMap();
        Set<BlockPos> blocks = Sets.newHashSet();
        Set<BlockPos> ends = Sets.newHashSet();
        Set<BlockPos> add = Sets.newHashSet();
        ends.add(new BlockPos(0, 0, 0));
        boolean run = true;

        MutableBlockPos bPos = new MutableBlockPos();

        while (run) {
            for (BlockPos center : ends) {
                for (Direction dir : Direction.values()) {
                    bPos.set(center).move(dir);
                    BlockPos wpos = bPos.offset(start);

                    if (!blocks.contains(bPos)) {
                        if (this.getDistance(bPos.getX(), bPos.getY(), bPos.getZ()) < 0) {
                            BlockState state = getBlockState(wpos);
                            PosInfo.create(mapWorld, addInfo, wpos).setState(state);
                            add.add(bPos.immutable());
                        }
                    }
                }
            }

            blocks.addAll(ends);
            ends.clear();
            ends.addAll(add);
            add.clear();

            run &= !ends.isEmpty();
        }

        List<PosInfo> infos = new ArrayList<PosInfo>(mapWorld.values());
        Collections.sort(infos);
        postProcesses.forEach((postProcess) -> {
            infos.forEach((info) -> {
                info.setState(postProcess.apply(info));
            });
        });
        infos.forEach((info) -> {
            world.setBlock(info.getPos(), info.getState());
        });

        infos.clear();
        infos.addAll(addInfo.values());
        Collections.sort(infos);
        postProcesses.forEach((postProcess) -> {
            infos.forEach((info) -> {
                info.setState(postProcess.apply(info));
            });
        });
        infos.forEach((info) -> {
            world.setBlock(info.getPos(), info.getState());
        });
    }

    public Set<BlockPos> getPositions(ServerLevelAccessor world, BlockPos start) {
        Set<BlockPos> blocks = Sets.newHashSet();
        Set<BlockPos> ends = Sets.newHashSet();
        Set<BlockPos> add = Sets.newHashSet();
        ends.add(new BlockPos(0, 0, 0));
        boolean run = true;

        MutableBlockPos bPos = new MutableBlockPos();

        while (run) {
            for (BlockPos center : ends) {
                for (Direction dir : Direction.values()) {
                    bPos.set(center).move(dir);
                    BlockPos wpos = bPos.offset(start);
                    BlockState state = world.getBlockState(wpos);
                    if (!blocks.contains(wpos) && canReplace.apply(state)) {
                        if (this.getDistance(bPos.getX(), bPos.getY(), bPos.getZ()) < 0) {
                            add.add(bPos.immutable());
                        }
                    }
                }
            }

            ends.forEach((end) -> blocks.add(end.offset(start)));
            ends.clear();
            ends.addAll(add);
            add.clear();

            run &= !ends.isEmpty();
        }

        return blocks;
    }
}
