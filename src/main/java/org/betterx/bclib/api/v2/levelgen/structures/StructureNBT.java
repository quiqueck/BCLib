package org.betterx.bclib.api.v2.levelgen.structures;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.util.BlocksHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import com.google.common.collect.Maps;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

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
        BlockPos newPos = getCenteredPos(pos, rotation, mirror);
        if (newPos == null) return false;
        StructurePlaceSettings data = new StructurePlaceSettings().setRotation(rotation).setMirror(mirror);
        structure.placeInWorld(
                world,
                newPos,
                newPos,
                data,
                world.getRandom(),
                BlocksHelper.SET_SILENT
        );
        return true;
    }

    public boolean generateAt(ServerLevelAccessor world, BlockPos pos, Rotation rotation, Mirror mirror) {
        StructurePlaceSettings data = new StructurePlaceSettings().setRotation(rotation).setMirror(mirror);
        structure.placeInWorld(
                world,
                pos,
                pos,
                data,
                world.getRandom(),
                BlocksHelper.SET_SILENT
        );
        return true;
    }

    @Nullable
    private BlockPos getCenteredPos(BlockPos pos, Rotation rotation, Mirror mirror) {
        if (structure == null) {
            BCLib.LOGGER.error("No structure: " + location.toString());
            return null;
        }

        MutableBlockPos blockpos2 = new MutableBlockPos().set(structure.getSize());
        if (mirror == Mirror.FRONT_BACK)
            blockpos2.setX(-blockpos2.getX());
        if (mirror == Mirror.LEFT_RIGHT)
            blockpos2.setZ(-blockpos2.getZ());
        blockpos2.set(blockpos2.rotate(rotation));
        return pos.offset(-blockpos2.getX() >> 1, 0, -blockpos2.getZ() >> 1);
    }

    private static final Map<ResourceLocation, StructureTemplate> READER_CACHE = Maps.newHashMap();

    private static StructureTemplate readStructureFromJar(ResourceLocation resource) {
        return READER_CACHE.computeIfAbsent(resource, r -> _readStructureFromJar(r));
    }

    private static StructureTemplate _readStructureFromJar(ResourceLocation resource) {
        String ns = resource.getNamespace();
        String nm = resource.getPath();

        allResourcesFrom(resource);
        try {
            InputStream inputstream = MinecraftServer.class.getResourceAsStream("/data/" + ns + "/structures/" + nm + ".nbt");
            return readStructureFromStream(inputstream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<StructureNBT> allResourcesFrom(ResourceLocation resource) {
        String ns = resource.getNamespace();
        String nm = resource.getPath();

        final URL url = MinecraftServer.class.getClassLoader().getResource("data/" + ns + "/structures/" + nm);
        if (url != null) {
            final URI uri;
            try {
                uri = url.toURI();
            } catch (URISyntaxException e) {
                BCLib.LOGGER.error("Unable to load Resources: ", e);
                return null;
            }
            Path myPath;
            if (uri.getScheme().equals("jar")) {
                FileSystem fileSystem = null;
                try {
                    fileSystem = FileSystems.newFileSystem(uri, new HashMap<>());
                } catch (IOException e) {
                    BCLib.LOGGER.error("Unable to load Resources: ", e);
                    return null;
                }
                myPath = fileSystem.getPath("/resources");
            } else {
                myPath = Paths.get(uri);
            }
            if (myPath.toFile().isDirectory()) {
                try {
                    return Files.walk(myPath, 1)
                                .filter(p -> p.toFile().isFile())
                                .map(p -> p.getFileName().toFile())
                                .filter(f -> f.toString().endsWith(".nbt"))
                                .map(f -> f.toString())
                                .map(s -> new ResourceLocation(
                                        ns,
                                        (nm.isEmpty() ? "" : (nm + "/")) + s.substring(0, s.length() - 4)
                                ))
                                .map(r -> {
                                    BCLib.LOGGER.info("Loading Structure: " + r);
                                    try {
                                        return StructureNBT.create(r);
                                    } catch (Exception e) {
                                        BCLib.LOGGER.error("Unable to load Structure " + r, e);
                                    }
                                    return null;
                                }).toList();
                } catch (IOException e) {
                    BCLib.LOGGER.error("Unable to load Resources: ", e);
                    return null;
                }
            }
        }
        return null;
    }

    private static StructureTemplate readStructureFromStream(InputStream stream) throws IOException {
        CompoundTag nbttagcompound = NbtIo.readCompressed(stream);

        StructureTemplate template = new StructureTemplate();

        template.load(BuiltInRegistries.BLOCK.asLookup(), nbttagcompound);

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

    public BoundingBox getCenteredBoundingBox(BlockPos pos, Rotation rotation, Mirror mirror) {
        return structure.getBoundingBox(
                new StructurePlaceSettings().setRotation(rotation).setMirror(mirror),
                getCenteredPos(pos, rotation, mirror)
        );
    }
}
