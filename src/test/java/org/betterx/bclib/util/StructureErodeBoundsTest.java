package org.betterx.bclib.util;

import de.ambertation.wover.tag.api.predefined.CommonBlockTags;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A feature must not read blocks outside the 3x3 chunks it may write to (26.3 logs "Detected unsafe terrain
 * read during worldgen", 1.21.x reads silently). BetterEnd's crashed ship hands {@link StructureErode#erodeIntense} a box that
 * is already clipped to those chunks, but the erosion scattered debris up to a few blocks sideways and
 * looked for neighbouring terrain one block outside the box, so a ship at the edge of the zone read
 * outside of it.
 * <p>
 * A real {@code WorldGenRegion} cannot be built in a unit test, so this runs the erosion against an
 * in-memory {@link WorldGenLevel} that records every position it is asked about. The terrain is an endless
 * sheet of stone bricks (eroded, never ignored) on top of end stone, reaching across every side of the box.
 * Blocks may fall below the box, but no column next to it may be read.
 */
class StructureErodeBoundsTest {
    private static final int FLOOR_Y = 10;
    private static final int HULL_TOP_Y = 13;
    private static final BoundingBox BOUNDS = new BoundingBox(0, FLOOR_Y + 1, 0, 15, FLOOR_Y + 6, 15);

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        // no datapacks are loaded here, so bind the one tag erosion relies on by hand (the rest stay empty)
        final MappedRegistry<Block> blocks = (MappedRegistry<Block>) BuiltInRegistries.BLOCK;
        blocks.bindAllTagsToEmpty();
        blocks.bindTag(CommonBlockTags.END_STONES, List.of(Blocks.END_STONE.builtInRegistryHolder()));
        blocks.freeze();
    }

    @Test
    void erodeIntenseOnlyReadsColumnsInsideTheBox() {
        for (long seed = 0; seed < 16; seed++) {
            final Set<BlockPos> reads = new HashSet<>();
            final Map<BlockPos, BlockState> written = new HashMap<>();
            StructureErode.erodeIntense(fakeLevel(reads, written), BOUNDS, RandomSource.create(seed));

            assertFalse(written.isEmpty(), "the fixture should erode something (seed " + seed + ")");
            for (BlockPos pos : reads) {
                assertTrue(
                        pos.getX() >= BOUNDS.minX() && pos.getX() <= BOUNDS.maxX()
                                && pos.getZ() >= BOUNDS.minZ() && pos.getZ() <= BOUNDS.maxZ(),
                        "erodeIntense read " + pos + ", outside the columns of " + BOUNDS + " (seed " + seed + ")"
                );
            }
        }
    }

    /**
     * {@link StructureErode#erode} lets a loose block slide off to a free side and fall down. The fall never
     * moved the block (it checked the same position over and over), so a block that did not land right away
     * vanished, and a block that was moved was looked at again from its new position for the remaining
     * directions.
     * <p>
     * A single stone brick floats in the middle of a one block high box, the four columns next to it hold
     * pillars of end stone that end well below it. Wherever the brick slides to, it has to come to rest on top
     * of that pillar, and it must not slide off the pillar a second time.
     */
    @Test
    void erodeDropsALooseBlockOntoTheFirstSolidBlockBelowOnce() {
        final BoundingBox bounds = new BoundingBox(7, 20, 7, 9, 20, 9);
        final BlockPos brick = new BlockPos(8, 20, 8);
        final int pillarTopY = 15;
        final Function<BlockPos, BlockState> terrain = pos -> {
            if (pos.equals(brick)) return Blocks.STONE_BRICKS.defaultBlockState();
            if (pos.getY() <= FLOOR_Y) return Blocks.END_STONE.defaultBlockState();
            final int dist = Math.abs(pos.getX() - brick.getX()) + Math.abs(pos.getZ() - brick.getZ());
            if (dist == 1 && pos.getY() <= pillarTopY) return Blocks.END_STONE.defaultBlockState();
            return Blocks.AIR.defaultBlockState();
        };

        int moved = 0;
        for (long seed = 0; seed < 64; seed++) {
            final Set<BlockPos> reads = new HashSet<>();
            final Map<BlockPos, BlockState> written = new HashMap<>();
            StructureErode.erode(fakeLevel(reads, written, terrain), bounds, 1, RandomSource.create(seed));

            final List<BlockPos> bricks = written.entrySet()
                                                 .stream()
                                                 .filter(e -> e.getValue().is(Blocks.STONE_BRICKS))
                                                 .map(Map.Entry::getKey)
                                                 .toList();
            assertTrue(bricks.size() <= 1, "erode duplicated the brick to " + bricks + " (seed " + seed + ")");
            if (written.containsKey(brick) && written.get(brick).isAir()) {
                //the brick was either moved or eroded away, if it was moved it rests on a pillar
                for (BlockPos pos : bricks) {
                    final int dist = Math.abs(pos.getX() - brick.getX()) + Math.abs(pos.getZ() - brick.getZ());
                    assertEquals(1, dist, "the brick was moved more than once to " + pos + " (seed " + seed + ")");
                    assertEquals(pillarTopY + 1, pos.getY(), "the brick did not land on the pillar " + pos + " (seed " + seed + ")");
                    moved++;
                }
            }
            for (BlockPos pos : reads) {
                assertTrue(
                        pos.getX() >= bounds.minX() - 1 && pos.getX() <= bounds.maxX() + 1
                                && pos.getZ() >= bounds.minZ() - 1 && pos.getZ() <= bounds.maxZ() + 1,
                        "erode read " + pos + ", more than one column away from " + bounds + " (seed " + seed + ")"
                );
            }
        }
        assertTrue(moved > 0, "the brick never landed on a pillar, a dislodged block must not vanish");
    }

    /**
     * End stone up to {@link #FLOOR_Y}, stone bricks up to {@link #HULL_TOP_Y} and air above, everywhere.
     * Writes land in {@code written}; every position read is added to {@code reads}.
     */
    private static WorldGenLevel fakeLevel(Set<BlockPos> reads, Map<BlockPos, BlockState> written) {
        return fakeLevel(reads, written, pos -> {
            if (pos.getY() <= FLOOR_Y) return Blocks.END_STONE.defaultBlockState();
            if (pos.getY() <= HULL_TOP_Y) return Blocks.STONE_BRICKS.defaultBlockState();
            return Blocks.AIR.defaultBlockState();
        });
    }

    /**
     * A level made of {@code terrain}. Writes land in {@code written}; every position read is added to
     * {@code reads}.
     */
    private static WorldGenLevel fakeLevel(
            Set<BlockPos> reads,
            Map<BlockPos, BlockState> written,
            Function<BlockPos, BlockState> terrain
    ) {
        final InvocationHandler handler = (proxy, method, args) -> {
            final int argc = args == null ? 0 : args.length;
            switch (method.getName()) {
                case "getBlockState", "getFluidState", "isEmptyBlock" -> {
                    final BlockPos pos = ((BlockPos) args[0]).immutable();
                    reads.add(pos);
                    BlockState state = written.get(pos);
                    if (state == null) state = terrain.apply(pos);
                    return switch (method.getName()) {
                        case "getFluidState" -> state.getFluidState();
                        case "isEmptyBlock" -> state.isAir();
                        default -> state;
                    };
                }
                case "setBlock" -> {
                    written.put(((BlockPos) args[0]).immutable(), (BlockState) args[1]);
                    return true;
                }
                case "getMinY" -> {
                    return 0;
                }
                case "getHeight" -> {
                    if (argc == 0) return 256;
                }
                case "equals" -> {
                    return proxy == (argc > 0 ? args[0] : null);
                }
                case "hashCode" -> {
                    return System.identityHashCode(proxy);
                }
                case "toString" -> {
                    return "FakeWorldGenLevel";
                }
                default -> {
                }
            }
            if (method.isDefault()) {
                return InvocationHandler.invokeDefault(proxy, method, args);
            }
            throw new UnsupportedOperationException("WorldGenLevel." + method.getName() + "() not stubbed for this test");
        };
        return (WorldGenLevel) Proxy.newProxyInstance(
                StructureErodeBoundsTest.class.getClassLoader(),
                new Class<?>[]{WorldGenLevel.class},
                handler
        );
    }
}
