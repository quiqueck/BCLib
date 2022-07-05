package org.betterx.bclib.util;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;


/**
 * @deprecated replaced by {@link org.betterx.worlds.together.util.PathUtil}
 */
@Deprecated(forRemoval = true)
public class PathUtil {
    /**
     * @deprecated replaced by {@link org.betterx.worlds.together.util.PathUtil#GAME_FOLDER}
     */
    @Deprecated(forRemoval = true)
    public final static Path GAME_FOLDER = org.betterx.worlds.together.util.PathUtil.GAME_FOLDER;

    /**
     * @deprecated replaced by {@link org.betterx.worlds.together.util.PathUtil#MOD_FOLDER}
     */
    @Deprecated(forRemoval = true)
    public final static Path MOD_FOLDER = org.betterx.worlds.together.util.PathUtil.MOD_FOLDER;

    /**
     * @deprecated replaced by {@link org.betterx.worlds.together.util.PathUtil#MOD_BAK_FOLDER}
     */
    @Deprecated(forRemoval = true)
    public final static Path MOD_BAK_FOLDER = org.betterx.worlds.together.util.PathUtil.MOD_BAK_FOLDER;

    /**
     * @deprecated replaced by {@link org.betterx.worlds.together.util.PathUtil#isChildOf(Path, Path)}
     */
    @Deprecated(forRemoval = true)
    public static boolean isChildOf(Path parent, Path child) {
        return org.betterx.worlds.together.util.PathUtil.isChildOf(parent, child);
    }

    /**
     * @deprecated replaced by {@link org.betterx.worlds.together.util.PathUtil#fileWalker(File, Consumer)}
     */
    @Deprecated(forRemoval = true)
    public static void fileWalker(File path, Consumer<Path> pathConsumer) {
        org.betterx.worlds.together.util.PathUtil.fileWalker(path, pathConsumer);
    }

    /**
     * @deprecated replaced by {@link org.betterx.worlds.together.util.PathUtil#fileWalker(File, boolean, Consumer)}
     */
    @Deprecated(forRemoval = true)
    public static void fileWalker(File path, boolean recursive, Consumer<Path> pathConsumer) {
        org.betterx.worlds.together.util.PathUtil.fileWalker(path, recursive, pathConsumer);
    }

    /**
     * @deprecated replaced by {@link org.betterx.worlds.together.util.PathUtil#humanReadableFileSize(long)}
     */
    @Deprecated(forRemoval = true)
    public static String humanReadableFileSize(long size) {
        return org.betterx.worlds.together.util.PathUtil.humanReadableFileSize(size);
    }
}
