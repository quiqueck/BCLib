package org.betterx.worlds.together.util;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

public class PathUtil {
    public final static Path GAME_FOLDER = FabricLoader.getInstance()
                                                       .getGameDir()
                                                       .normalize();

    public final static Path MOD_FOLDER = FabricLoader.getInstance()
                                                      .getGameDir()
                                                      .resolve("mods")
                                                      .normalize();

    public final static Path MOD_BAK_FOLDER = MOD_FOLDER.resolve("_bclib_deactivated")
                                                        .normalize();

    /**
     * Tests if a path is a child-path.
     * <p>
     * A path is a child of another if it is located in the parent or any of the parents subdirectories
     *
     * @param parent The folder we search for the {@code child}
     * @param child  The folder you want to test
     * @return {@code true} if {@code child} is in {@code parent} or any of its sub directories
     */
    public static boolean isChildOf(Path parent, Path child) {
        if (child == null || parent == null) return false;
        parent = parent.toAbsolutePath().normalize();
        child = child.toAbsolutePath().normalize();

        final int pCount = parent.getNameCount();
        final int cCount = child.getNameCount();

        if (cCount > pCount) return isChildOf(parent, child.getParent());
        if (cCount < pCount) return false;

        return child.equals(parent);
    }

    /**
     * A simple directory walker that ignores dot-files
     *
     * @param path         The path where you want to start
     * @param pathConsumer The consumer called for each valid file. The consumer will get an absolute {@link Path}-Object
     *                     for each visited file
     */
    public static void fileWalker(File path, Consumer<Path> pathConsumer) {
        fileWalker(path, true, pathConsumer);
    }

    /**
     * A simple directory walker that ignores dot-files
     *
     * @param path         The path where you want to start
     * @param recursive    if {@code false}, only the {@code path} is traversed
     * @param pathConsumer The consumer called for each valid file. The consumer will get an absolute {@link Path}-Object
     *                     for each visited file
     */
    public static void fileWalker(File path, boolean recursive, Consumer<Path> pathConsumer) {
        if (!path.exists()) return;
        for (final File f : path.listFiles()) {
            if (f.getName()
                 .startsWith(".")) continue;
            if (f.isDirectory()) {
                if (recursive) fileWalker(f, pathConsumer);
            } else if (f.isFile()) {
                pathConsumer.accept(f.toPath());
            }
        }
    }

    /**
     * Creates a human readable File-Size
     *
     * @param size Filesize in bytes
     * @return A Human readable String
     */
    public static String humanReadableFileSize(long size) {
        final int threshold = 2;
        final int factor = 1024;
        if (size < 0) return "? Byte";
        if (size < factor * threshold) {
            return size + " Byte";
        }
        char[] units = {'K', 'M', 'G', 'T', 'P'};
        int unitIndex = 0;
        double fSize = size;
        do {
            unitIndex++;
            fSize /= 1024;
        } while (fSize > factor * threshold && unitIndex < units.length);

        return String.format("%.1f %ciB", fSize, units[unitIndex - 1]);
    }
}
