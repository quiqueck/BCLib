package org.betterx.bclib.api.v2.levelgen.structures;

import org.betterx.bclib.BCLib;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class TemplatePiece extends TemplateStructurePiece {
    public static final StructurePieceType INSTANCE = setTemplatePieceId(
            TemplatePiece::new,
            "template_piece"
    );


    private static StructurePieceType setFullContextPieceId(StructurePieceType structurePieceType, String id) {
        return Registry.register(Registry.STRUCTURE_PIECE, BCLib.makeID(id), structurePieceType);
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
        super(
                INSTANCE,
                0,
                structureTemplateManager,
                resourceLocation,
                resourceLocation.toString(),
                makeSettings(rotation, mirror, halfSize),
                shiftPos(rotation, mirror, halfSize, centerPos)
        );
    }

    public TemplatePiece(StructureTemplateManager structureTemplateManager, CompoundTag compoundTag) {
        super(
                INSTANCE,
                compoundTag,
                structureTemplateManager,
                (ResourceLocation resourceLocation) -> makeSettings(compoundTag)
        );
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
}
