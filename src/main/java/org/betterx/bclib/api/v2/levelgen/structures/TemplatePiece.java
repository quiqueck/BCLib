package org.betterx.bclib.api.v2.levelgen.structures;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.util.BlocksHelper;
import org.betterx.bclib.util.MHelper;
import org.betterx.bclib.util.StructureErode;
import de.ambertation.wover.tag.api.predefined.CommonBlockTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class TemplatePiece extends TemplateStructurePiece {
    private final int erosion;
    private final boolean cover;
    private final Block broken;
    private final int brokenIndex;
    public static final StructurePieceType INSTANCE = setTemplatePieceId(
            TemplatePiece::new,
            "template_piece"
    );


    private static StructurePieceType setFullContextPieceId(StructurePieceType structurePieceType, String id) {
        return Registry.register(BuiltInRegistries.STRUCTURE_PIECE, BCLib.makeID(id), structurePieceType);
    }

    private static StructurePieceType setTemplatePieceId(
            StructurePieceType.StructureTemplateType structureTemplateType,
            String string
    ) {
        return setFullContextPieceId(structureTemplateType, string);
    }


    public static void ensureStaticInitialization() {
    }

    public TemplatePiece(
            StructureTemplateManager structureTemplateManager,
            Identifier resourceLocation,
            BlockPos centerPos,
            Rotation rotation,
            Mirror mirror,
            BlockPos halfSize
    ) {
        this(structureTemplateManager, resourceLocation, centerPos, rotation, mirror, halfSize, 0, false);
    }

    public TemplatePiece(
            StructureTemplateManager structureTemplateManager,
            Identifier resourceLocation,
            BlockPos centerPos,
            Rotation rotation,
            Mirror mirror,
            BlockPos halfSize,
            int erosion,
            boolean cover
    ) {
        this(structureTemplateManager, resourceLocation, centerPos, rotation, mirror, halfSize, erosion, cover, null, 0);
    }

    /**
     * @param broken      a block of the template of which one is always replaced by air, or {@code null}
     * @param brokenIndex which of those blocks it is (taken modulo their number)
     */
    public TemplatePiece(
            StructureTemplateManager structureTemplateManager,
            Identifier resourceLocation,
            BlockPos centerPos,
            Rotation rotation,
            Mirror mirror,
            BlockPos halfSize,
            int erosion,
            boolean cover,
            Block broken,
            int brokenIndex
    ) {
        super(
                INSTANCE,
                0,
                structureTemplateManager,
                resourceLocation,
                resourceLocation.toString(),
                makeSettings(rotation, mirror, halfSize),
                shiftPos(rotation, mirror, halfSize, centerPos)
        );
        this.erosion = erosion;
        this.cover = cover;
        this.broken = broken;
        this.brokenIndex = brokenIndex;
    }

    public TemplatePiece(StructureTemplateManager structureTemplateManager, CompoundTag compoundTag) {
        super(
                INSTANCE,
                compoundTag,
                structureTemplateManager,
                (Identifier resourceLocation) -> makeSettings(compoundTag)
        );
        this.erosion = compoundTag.getInt("E").orElse(0);
        this.cover = compoundTag.getBoolean("C").orElse(true);
        this.broken = compoundTag.getString("B")
                                 .map(Identifier::tryParse)
                                 .flatMap(BuiltInRegistries.BLOCK::getOptional)
                                 .orElse(null);
        this.brokenIndex = compoundTag.getInt("BI").orElse(0);

    }

    private static BlockPos shiftPos(
            Rotation rotation,
            Mirror mirror,
            BlockPos halfSize,
            BlockPos pos
    ) {
        halfSize = StructureTemplate.transform(halfSize, mirror, rotation, halfSize);
        return pos.offset(-halfSize.getX(), 0, -halfSize.getZ());
    }

    private static StructurePlaceSettings makeSettings(CompoundTag compoundTag) {
        return makeSettings(
                Rotation.valueOf(compoundTag.getString("R").orElse(Rotation.NONE.name())),
                Mirror.valueOf(compoundTag.getString("M").orElse(Mirror.NONE.name())),
                new BlockPos(
                        compoundTag.getInt("RX").orElse(0),
                        compoundTag.getInt("RY").orElse(0),
                        compoundTag.getInt("RZ").orElse(0)
                )
        );

    }

    private static StructurePlaceSettings makeSettings(Rotation rotation, Mirror mirror, BlockPos halfSize) {
        return new StructurePlaceSettings().setRotation(rotation)
                                           .setMirror(mirror)
                                           .setRotationPivot(halfSize)
                                           .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
    }

    @Override
    protected void addAdditionalSaveData(
            StructurePieceSerializationContext structurePieceSerializationContext,
            CompoundTag tag
    ) {
        super.addAdditionalSaveData(structurePieceSerializationContext, tag);
        tag.putString("R", this.placeSettings.getRotation().name());
        tag.putString("M", this.placeSettings.getMirror().name());
        tag.putInt("RX", this.placeSettings.getRotationPivot().getX());
        tag.putInt("RY", this.placeSettings.getRotationPivot().getY());
        tag.putInt("RZ", this.placeSettings.getRotationPivot().getZ());
        tag.putInt("E", this.erosion);
        tag.putBoolean("C", this.cover);
        if (this.broken != null) {
            tag.putString("B", BuiltInRegistries.BLOCK.getKey(this.broken).toString());
            tag.putInt("BI", this.brokenIndex);
        }
    }

    @Override
    protected void handleDataMarker(
            String string,
            BlockPos blockPos,
            ServerLevelAccessor serverLevelAccessor,
            RandomSource randomSource,
            BoundingBox boundingBox
    ) {

    }

    /**
     * Replaces the chosen one of the {@link #broken} blocks by air when it lies in the chunk being generated. The
     * choice is made from all of them, not only the ones in this chunk, so exactly one breaks across all chunks.
     */
    private void breakOne(WorldGenLevel world, BoundingBox chunkBox) {
        if (broken == null) return;
        final var all = this.template.filterBlocks(
                this.templatePosition,
                this.placeSettings.copy().setBoundingBox(null),
                broken
        );
        if (all.isEmpty()) return;
        final BlockPos pos = all.get(Math.floorMod(brokenIndex, all.size())).pos();
        if (chunkBox.isInside(pos) && world.getBlockState(pos).is(broken)) {
            BlocksHelper.setWithoutUpdate(world, pos, Blocks.AIR);
        }
    }

    @Override
    public void postProcess(
            WorldGenLevel world,
            StructureManager structureManager,
            ChunkGenerator chunkGenerator,
            RandomSource random,
            BoundingBox boundingBox,
            ChunkPos chunkPos,
            BlockPos blockPos
    ) {
        BlockState coverState = null;
        if (cover) {
            BlockPos.MutableBlockPos mPos = new BlockPos(
                    this.boundingBox.minX() - 1,
                    blockPos.getY(),
                    this.boundingBox.minZ() - 1
            ).mutable();
            if (BlocksHelper.findOnSurroundingSurface(
                    world,
                    mPos,
                    Direction.DOWN,
                    8,
                    s -> s.is(CommonBlockTags.TERRAIN)
            )) {
                mPos.move(Direction.DOWN);
                coverState = world.getBlockState(mPos);
            }
        }
        super.postProcess(world, structureManager, chunkGenerator, random, boundingBox, chunkPos, blockPos);
        breakOne(world, boundingBox);
        BoundingBox bounds = BoundingBox.fromCorners(
                new Vec3i(
                        boundingBox.minX(),
                        this.boundingBox.minY(),
                        boundingBox.minZ()
                ), new Vec3i(boundingBox.maxX(), this.boundingBox.maxY(), boundingBox.maxZ())
        );

        if (erosion > 0) {
            int x1 = MHelper.min(bounds.maxX(), this.boundingBox.maxX());
            int x0 = MHelper.max(bounds.minX(), this.boundingBox.minX());
            int z1 = MHelper.min(bounds.maxZ(), this.boundingBox.maxZ());
            int z0 = MHelper.max(bounds.minZ(), this.boundingBox.minZ());
            bounds = BoundingBox.fromCorners(new Vec3i(x0, bounds.minY(), z0), new Vec3i(x1, bounds.maxY(), z1));
            StructureErode.erode(world, bounds, erosion, random);
        }

        if (cover) {
            //System.out.println("CoverState:" + coverState + ", " + blockPos + " " + boundingBox.getCenter());
            StructureErode.cover(world, bounds, random, coverState);
        }
    }
}
