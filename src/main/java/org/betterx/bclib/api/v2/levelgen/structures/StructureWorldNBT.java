package org.betterx.bclib.api.v2.levelgen.structures;

import org.betterx.bclib.util.BlocksHelper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import com.google.common.collect.Maps;

import java.util.Map;

public class StructureWorldNBT extends StructureNBT {
    public static final Codec<StructureWorldNBT> CODEC =
            RecordCodecBuilder.create((instance) ->
                    instance.group(
                                    ResourceLocation.CODEC
                                            .fieldOf("location")
                                            .forGetter((cfg) -> cfg.location),

                                    Codec
                                            .INT
                                            .fieldOf("offset_y")
                                            .orElse(0)
                                            .forGetter((cfg) -> cfg.offsetY),

                                    StructurePlacementType.CODEC
                                            .fieldOf("placement")
                                            .orElse(StructurePlacementType.FLOOR)
                                            .forGetter((cfg) -> cfg.type),
                                    Codec
                                            .FLOAT
                                            .fieldOf("chance")
                                            .orElse(1.0f)
                                            .forGetter((cfg) -> cfg.chance)
                            )
                            .apply(instance, StructureWorldNBT::new)
            );

    public final StructurePlacementType type;
    public final int offsetY;
    public final float chance;

    protected StructureWorldNBT(ResourceLocation location, int offsetY, StructurePlacementType type, float chance) {
        super(location);
        this.offsetY = offsetY;
        this.type = type;
        this.chance = chance;
    }

    private static final Map<String, StructureWorldNBT> READER_CACHE = Maps.newHashMap();

    public static StructureWorldNBT create(ResourceLocation location, int offsetY, StructurePlacementType type) {
        return create(location, offsetY, type, 1.0f);
    }

    public static StructureWorldNBT create(
            ResourceLocation location,
            int offsetY,
            StructurePlacementType type,
            float chance
    ) {
        String key = location.toString() + "::" + offsetY + "::" + type.getSerializedName();
        return READER_CACHE.computeIfAbsent(key, r -> new StructureWorldNBT(location, offsetY, type, chance));
    }

    public boolean generateIfPlaceable(
            ServerLevelAccessor level,
            BlockPos pos,
            RandomSource random
    ) {
        return generateIfPlaceable(
                level,
                pos,
                getRandomRotation(random),
                getRandomMirror(random)
        );
    }

    public boolean generateIfPlaceable(
            ServerLevelAccessor level,
            BlockPos pos,
            Rotation r,
            Mirror m
    ) {
        if (canGenerate(level, pos, r)) {
            return generate(level, pos, r, m);
        }
        return false;
    }

    public boolean generate(ServerLevelAccessor level, BlockPos pos, Rotation r, Mirror m) {
        return generateCentered(level, pos.above(offsetY), r, m);
    }

    protected boolean canGenerate(LevelAccessor level, BlockPos pos, Rotation rotation) {
        if (type == StructurePlacementType.FLOOR)
            return canGenerateFloor(level, pos, rotation);
        else if (type == StructurePlacementType.LAVA)
            return canGenerateLava(level, pos, rotation);
        else if (type == StructurePlacementType.UNDER)
            return canGenerateUnder(level, pos, rotation);
        else if (type == StructurePlacementType.CEIL)
            return canGenerateCeil(level, pos, rotation);
        else
            return false;
    }

    private boolean containsBedrock(LevelAccessor level, BlockPos startPos) {
        for (int i = 0; i < this.structure.getSize().getY(); i += 2) {
            if (level.getBlockState(startPos.above(i)).is(Blocks.BEDROCK)) {
                return true;
            }
        }
        return false;
    }

    protected boolean canGenerateFloorFreeAbove(LevelAccessor world, BlockPos pos, Rotation rotation) {
        if (containsBedrock(world, pos)) return false;

        return getAirFractionFoundation(world, pos, rotation) < 0.5
                && world.getBlockState(pos.above(2)).is(Blocks.AIR)
                && world.getBlockState(pos.above(4)).is(Blocks.AIR);
    }

    protected boolean canGenerateFloor(LevelAccessor world, BlockPos pos, Rotation rotation) {
        if (containsBedrock(world, pos)) return false;

        return getAirFraction(world, pos, rotation) > 0.6 && getAirFractionFoundation(world, pos, rotation) < 0.5;
    }

    protected boolean canGenerateLava(LevelAccessor world, BlockPos pos, Rotation rotation) {
        if (containsBedrock(world, pos)) return false;

        return getLavaFractionFoundation(world, pos, rotation) > 0.9 && getAirFraction(world, pos, rotation) > 0.9;
    }

    protected boolean canGenerateUnder(LevelAccessor world, BlockPos pos, Rotation rotation) {
        if (containsBedrock(world, pos)) return false;

        return getAirFraction(world, pos, rotation) < 0.2;
    }

    protected boolean canGenerateCeil(LevelAccessor world, BlockPos pos, Rotation rotation) {
        if (containsBedrock(world, pos)) return false;

        return getAirFractionBottom(world, pos, rotation) > 0.8 && getAirFraction(world, pos, rotation) < 0.6;
    }

    public BoundingBox boundingBox(Rotation r, BlockPos p) {
        return getBoundingBox(p, r, Mirror.NONE);
    }

    protected float getAirFraction(LevelAccessor world, BlockPos pos, Rotation rotation) {
        final MutableBlockPos POS = new MutableBlockPos();
        int airCount = 0;

        MutableBlockPos size = new MutableBlockPos().set(new BlockPos(structure.getSize()).rotate(rotation));
        size.setX(Math.abs(size.getX()) >> 1);
        size.setZ(Math.abs(size.getZ()) >> 1);

        BlockPos start = pos.offset(-size.getX(), 0, -size.getZ());
        BlockPos end = pos.offset(size.getX(), size.getY() + offsetY, size.getZ());
        int count = 0;

        for (int x = start.getX(); x <= end.getX(); x++) {
            POS.setX(x);
            for (int y = start.getY(); y <= end.getY(); y++) {
                POS.setY(y);
                for (int z = start.getZ(); z <= end.getZ(); z++) {
                    POS.setZ(z);
                    if (world.isEmptyBlock(POS))
                        airCount++;
                    count++;
                }
            }
        }

        return (float) airCount / count;
    }

    private float getLavaFractionFoundation(LevelAccessor world, BlockPos pos, Rotation rotation) {
        final MutableBlockPos POS = new MutableBlockPos();
        int lavaCount = 0;

        MutableBlockPos size = new MutableBlockPos().set(new BlockPos(structure.getSize()).rotate(rotation));
        size.setX(Math.abs(size.getX()) >> 1);
        size.setZ(Math.abs(size.getZ()) >> 1);

        BlockPos start = pos.offset(-(size.getX()), 0, -(size.getZ()));
        BlockPos end = pos.offset(size.getX(), 0, size.getZ());
        int count = 0;

        POS.setY(pos.getY() - 1);
        for (int x = start.getX(); x <= end.getX(); x++) {
            POS.setX(x);
            for (int z = start.getZ(); z <= end.getZ(); z++) {
                POS.setZ(z);

                if (BlocksHelper.isLava(world.getBlockState(POS)))
                    lavaCount++;
                count++;
            }
        }

        return (float) lavaCount / count;
    }

    private float getAirFractionFoundation(LevelAccessor world, BlockPos pos, Rotation rotation) {
        final MutableBlockPos POS = new MutableBlockPos();
        int airCount = 0;

        MutableBlockPos size = new MutableBlockPos().set(new BlockPos(structure.getSize()).rotate(rotation));
        size.setX(Math.abs(size.getX()) >> 1);
        size.setZ(Math.abs(size.getZ()) >> 1);

        BlockPos start = pos.offset(-(size.getX()), -1, -(size.getZ()));
        BlockPos end = pos.offset(size.getX(), 0, size.getZ());
        int count = 0;

        for (int x = start.getX(); x <= end.getX(); x++) {
            POS.setX(x);
            for (int y = start.getY(); y <= end.getY(); y++) {
                POS.setY(y);
                for (int z = start.getZ(); z <= end.getZ(); z++) {
                    POS.setZ(z);
                    if (world.getBlockState(POS).canBeReplaced())
                        airCount++;
                    count++;
                }
            }
        }

        return (float) airCount / count;
    }

    private float getAirFractionBottom(LevelAccessor world, BlockPos pos, Rotation rotation) {
        final MutableBlockPos POS = new MutableBlockPos();
        int airCount = 0;

        MutableBlockPos size = new MutableBlockPos().set(new BlockPos(structure.getSize()).rotate(rotation));
        size.setX(Math.abs(size.getX()));
        size.setZ(Math.abs(size.getZ()));

        float y1 = Math.min(offsetY, 0);
        float y2 = Math.max(offsetY, 0);
        BlockPos start = pos.offset(-(size.getX() >> 1), (int) y1, -(size.getZ() >> 1));
        BlockPos end = pos.offset(size.getX() >> 1, (int) y2, size.getZ() >> 1);
        int count = 0;

        for (int x = start.getX(); x <= end.getX(); x++) {
            POS.setX(x);
            for (int y = start.getY(); y <= end.getY(); y++) {
                POS.setY(y);
                for (int z = start.getZ(); z <= end.getZ(); z++) {
                    POS.setZ(z);
                    if (world.getBlockState(POS).canBeReplaced())
                        airCount++;
                    count++;
                }
            }
        }

        return (float) airCount / count;
    }

    public boolean loaded() {
        return structure != null;
    }
}
