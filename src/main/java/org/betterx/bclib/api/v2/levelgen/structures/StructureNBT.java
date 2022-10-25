package org.betterx.bclib.api.v2.levelgen.structures;

import org.betterx.bclib.BCLib;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import com.google.common.collect.Maps;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class StructureNBT {
    public final ResourceLocation location;
    protected StructureTemplate structure;


    protected StructureNBT(ResourceLocation location) {
        this.location = location;
        this.structure = readStructureFromJar(location);
    }

    protected StructureNBT(ResourceLocation location, StructureTemplate structure) {
        this.location = location;
        this.structure = structure;
    }

    public static Rotation getRandomRotation(RandomSource random) {
        return Rotation.getRandom(random);
    }

    public static Mirror getRandomMirror(RandomSource random) {
        return Mirror.values()[random.nextInt(3)];
    }

    private static final Map<ResourceLocation, StructureNBT> STRUCTURE_CACHE = Maps.newHashMap();

    public static StructureNBT create(ResourceLocation location) {
        return STRUCTURE_CACHE.computeIfAbsent(location, r -> new StructureNBT(r));
    }

    public boolean generateCentered(ServerLevelAccessor world, BlockPos pos, Rotation rotation, Mirror mirror) {
        if (structure == null) {
            BCLib.LOGGER.error("No structure: " + location.toString());
            return false;
        }

        MutableBlockPos blockpos2 = new MutableBlockPos().set(structure.getSize());
        if (mirror == Mirror.FRONT_BACK)
            blockpos2.setX(-blockpos2.getX());
        if (mirror == Mirror.LEFT_RIGHT)
            blockpos2.setZ(-blockpos2.getZ());
        blockpos2.set(blockpos2.rotate(rotation));
        StructurePlaceSettings data = new StructurePlaceSettings().setRotation(rotation).setMirror(mirror);
        BlockPos newPos = pos.offset(-blockpos2.getX() >> 1, 0, -blockpos2.getZ() >> 1);
        structure.placeInWorld(
                world,
                newPos,
                newPos,
                data,
                world.getRandom(),
                Block.UPDATE_CLIENTS
        );
        return true;
    }

    private static final Map<ResourceLocation, StructureTemplate> READER_CACHE = Maps.newHashMap();

    private static StructureTemplate readStructureFromJar(ResourceLocation resource) {
        return READER_CACHE.computeIfAbsent(resource, r -> _readStructureFromJar(r));
    }

    private static StructureTemplate _readStructureFromJar(ResourceLocation resource) {
        String ns = resource.getNamespace();
        String nm = resource.getPath();

        try {
            InputStream inputstream = MinecraftServer.class.getResourceAsStream("/data/" + ns + "/structures/" + nm + ".nbt");
            return readStructureFromStream(inputstream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static StructureTemplate readStructureFromStream(InputStream stream) throws IOException {
        CompoundTag nbttagcompound = NbtIo.readCompressed(stream);

        StructureTemplate template = new StructureTemplate();
        template.load(HolderLookup.forRegistry(Registry.BLOCK), nbttagcompound);

        return template;
    }

    public BlockPos getSize(Rotation rotation) {
        if (rotation == Rotation.NONE || rotation == Rotation.CLOCKWISE_180)
            return new BlockPos(structure.getSize());
        else {
            Vec3i size = structure.getSize();
            int x = size.getX();
            int z = size.getZ();
            return new BlockPos(z, size.getY(), x);
        }
    }

    public String getName() {
        return location.getPath();
    }

    public BoundingBox getBoundingBox(BlockPos pos, Rotation rotation, Mirror mirror) {
        return structure.getBoundingBox(new StructurePlaceSettings().setRotation(rotation).setMirror(mirror), pos);
    }
}
