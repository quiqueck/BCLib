package org.betterx.bclib.api.v2.levelgen.structures;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.util.BlocksHelper;
import org.betterx.bclib.util.MHelper;
import org.betterx.bclib.util.StructureErode;
import org.betterx.worlds.together.tag.v3.CommonBlockTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
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
            ResourceLocation resourceLocation,
            BlockPos centerPos,
            Rotation rotation,
            Mirror mirror,
            BlockPos halfSize
    ) {
        this(structureTemplateManager, resourceLocation, centerPos, rotation, mirror, halfSize, 0, false);
    }

    public TemplatePiece(
            StructureTemplateManager structureTemplateManager,
            ResourceLocation resourceLocation,
            BlockPos centerPos,
            Rotation rotation,
            Mirror mirror,
            BlockPos halfSize,
            int erosion,
            boolean cover
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
    }

    public TemplatePiece(StructureTemplateManager structureTemplateManager, CompoundTag compoundTag) {
        super(
                INSTANCE,
                compoundTag,
                structureTemplateManager,
                (ResourceLocation resourceLocation) -> makeSettings(compoundTag)
        );
        if (compoundTag.contains("E"))
            this.erosion = compoundTag.getInt("E");
        else
            this.erosion = 0;

        if (compoundTag.contains("C"))
            this.cover = compoundTag.getBoolean("C");
        else
            this.cover = true;
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
                Rotation.valueOf(compoundTag.getString("R")),
                Mirror.valueOf(compoundTag.getString("M")),
                new BlockPos(compoundTag.getInt("RX"), compoundTag.getInt("RY"), compoundTag.getInt("RZ"))
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
        BoundingBox bounds = BoundingBox.fromCorners(new Vec3i(
                boundingBox.minX(),
                this.boundingBox.minY(),
                boundingBox.minZ()
        ), new Vec3i(boundingBox.maxX(), this.boundingBox.maxY(), boundingBox.maxZ()));

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
