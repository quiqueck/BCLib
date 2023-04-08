package org.betterx.bclib.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class StructureHelper {
    public static StructureTemplate readStructure(ResourceLocation resource) {
        String ns = resource.getNamespace();
        String nm = resource.getPath();
        return readStructure("/data/" + ns + "/structures/" + nm + ".nbt");
    }

    public static StructureTemplate readStructure(File datapack, String path) {
        if (datapack.isDirectory()) {
            return readStructure(datapack + "/" + path);
        } else if (datapack.isFile() && datapack.getName().endsWith(".zip")) {
            try {
                ZipFile zipFile = new ZipFile(datapack);
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String name = entry.getName();
                    long compressedSize = entry.getCompressedSize();
                    long normalSize = entry.getSize();
                    String type = entry.isDirectory() ? "DIR" : "FILE";

                    System.out.println(name);
                    System.out.format("\t %s - %d - %d\n", type, compressedSize, normalSize);
                }
                zipFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static StructureTemplate readStructure(String path) {
        try {
            InputStream inputstream = StructureHelper.class.getResourceAsStream(path);
            return readStructureFromStream(inputstream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static StructureTemplate readStructureFromStream(InputStream stream) throws IOException {
        CompoundTag nbttagcompound = NbtIo.readCompressed(stream);

        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), nbttagcompound);

        return template;
    }

    public static BlockPos offsetPos(BlockPos pos, StructureTemplate structure, Rotation rotation, Mirror mirror) {
        Vec3 offset = StructureTemplate.transform(
                Vec3.atCenterOf(structure.getSize()),
                mirror,
                rotation,
                BlockPos.ZERO
        );
        return pos.offset((int) (-offset.x * 0.5), 0, (int) (-offset.z * 0.5));
    }

    public static void placeCenteredBottom(
            WorldGenLevel world,
            BlockPos pos,
            StructureTemplate structure,
            Rotation rotation,
            Mirror mirror,
            RandomSource random
    ) {
        placeCenteredBottom(world, pos, structure, rotation, mirror, makeBox(pos), random);
    }

    public static void placeCenteredBottom(
            WorldGenLevel world,
            BlockPos pos,
            StructureTemplate structure,
            Rotation rotation,
            Mirror mirror,
            BoundingBox bounds,
            RandomSource random
    ) {
        BlockPos offset = offsetPos(pos, structure, rotation, mirror);
        StructurePlaceSettings placementData = new StructurePlaceSettings().setRotation(rotation)
                                                                           .setMirror(mirror)
                                                                           .setBoundingBox(bounds);
        structure.placeInWorld(world, offset, offset, placementData, random, 4);
    }

    private static BoundingBox makeBox(BlockPos pos) {
        int sx = ((pos.getX() >> 4) << 4) - 16;
        int sz = ((pos.getZ() >> 4) << 4) - 16;
        int ex = sx + 47;
        int ez = sz + 47;
        return BoundingBox.fromCorners(new Vec3i(sx, 0, sz), new Vec3i(ex, 255, ez));
    }

    public static BoundingBox getStructureBounds(
            BlockPos pos,
            StructureTemplate structure,
            Rotation rotation,
            Mirror mirror
    ) {
        Vec3i max = structure.getSize();
        Vec3 min = StructureTemplate.transform(Vec3.atCenterOf(structure.getSize()), mirror, rotation, BlockPos.ZERO);
        max = max.offset((int) -min.x, (int) -min.y, (int) -min.z);
        return BoundingBox.fromCorners(pos.offset((int) min.x, (int) min.y, (int) min.z), max.offset(pos));
    }

    public static BoundingBox intersectBoxes(BoundingBox box1, BoundingBox box2) {
        int x1 = MHelper.max(box1.minX(), box2.minX());
        int y1 = MHelper.max(box1.minY(), box2.minY());
        int z1 = MHelper.max(box1.minZ(), box2.minZ());

        int x2 = MHelper.min(box1.maxX(), box2.maxX());
        int y2 = MHelper.min(box1.maxY(), box2.maxY());
        int z2 = MHelper.min(box1.maxZ(), box2.maxZ());

        return BoundingBox.fromCorners(new Vec3i(x1, y1, z1), new Vec3i(x2, y2, z2));
    }
}
