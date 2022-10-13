package org.betterx.bclib.api.v2.dataexchange.handler.autosync;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.dataexchange.DataHandler;
import org.betterx.bclib.api.v2.dataexchange.FileHash;
import org.betterx.bclib.api.v2.dataexchange.handler.autosync.AutoSyncID.ForDirectFileRequest;
import org.betterx.bclib.config.Configs;
import org.betterx.worlds.together.util.PathUtil;

import net.minecraft.network.FriendlyByteBuf;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public class SyncFolderDescriptor {
    static class SubFile {
        public final String relPath;
        public final FileHash hash;


        SubFile(String relPath, FileHash hash) {
            this.relPath = relPath;
            this.hash = hash;
        }

        @Override
        public String toString() {
            return relPath;
        }

        public void serialize(FriendlyByteBuf buf) {
            DataHandler.writeString(buf, relPath);
            hash.serialize(buf);
        }

        public static SubFile deserialize(FriendlyByteBuf buf) {
            final String relPath = DataHandler.readString(buf);
            FileHash hash = FileHash.deserialize(buf);
            return new SubFile(relPath, hash);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o instanceof String) return relPath.equals(o);
            if (!(o instanceof SubFile)) return false;
            SubFile subFile = (SubFile) o;
            return relPath.equals(subFile.relPath);
        }

        @Override
        public int hashCode() {
            return relPath.hashCode();
        }
    }

    @NotNull
    public final String folderID;
    public final boolean removeAdditionalFiles;
    @NotNull
    public final Path localFolder;

    private List<SubFile> fileCache;

    public SyncFolderDescriptor(String folderID, Path localFolder, boolean removeAdditionalFiles) {
        this.removeAdditionalFiles = removeAdditionalFiles;
        this.folderID = folderID;
        this.localFolder = localFolder;
        fileCache = null;
    }

    @Override
    public String toString() {
        return "SyncFolderDescriptor{" + "folderID='" + folderID + '\'' + ", removeAdditionalFiles=" + removeAdditionalFiles + ", localFolder=" + localFolder + ", files=" + (
                fileCache == null
                        ? "?"
                        : fileCache.size()) + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof String) {
            return folderID.equals(o);
        }
        if (o instanceof ForDirectFileRequest) {
            return folderID.equals(((ForDirectFileRequest) o).uniqueID);
        }
        if (!(o instanceof SyncFolderDescriptor)) return false;
        SyncFolderDescriptor that = (SyncFolderDescriptor) o;
        return folderID.equals(that.folderID);
    }

    @Override
    public int hashCode() {
        return folderID.hashCode();
    }

    public int fileCount() {
        return fileCache == null ? 0 : fileCache.size();
    }

    public void invalidateCache() {
        fileCache = null;
    }

    public void loadCache() {
        if (fileCache == null) {
            fileCache = new ArrayList<>(8);
            PathUtil.fileWalker(localFolder.toFile(), p -> fileCache.add(new SubFile(
                    localFolder.relativize(p)
                               .toString(),
                    FileHash.create(p.toFile())
            )));
			
			/*//this tests if we can trick the system to load files that are not beneath the base-folder
			if (!BCLib.isClient()) {
				fileCache.add(new SubFile("../breakout.json", FileHash.create(mapAbsolute("../breakout.json").toFile())));
			}*/
        }
    }

    public void serialize(FriendlyByteBuf buf) {
        final boolean debugHashes = Configs.CLIENT_CONFIG.getBoolean(AutoSync.SYNC_CATEGORY, "debugHashes", false);
        loadCache();

        DataHandler.writeString(buf, folderID);
        buf.writeBoolean(removeAdditionalFiles);
        buf.writeInt(fileCache.size());
        fileCache.forEach(fl -> {
            if (Configs.MAIN_CONFIG.verboseLogging()) {
                BCLib.LOGGER.info("	  - " + fl.relPath);
                if (debugHashes) {
                    BCLib.LOGGER.info("		" + fl.hash);
                }
            }
            fl.serialize(buf);
        });
    }

    public static SyncFolderDescriptor deserialize(FriendlyByteBuf buf) {
        final String folderID = DataHandler.readString(buf);
        final boolean remAddFiles = buf.readBoolean();
        final int count = buf.readInt();
        SyncFolderDescriptor localDescriptor = AutoSync.getSyncFolderDescriptor(folderID);

        final SyncFolderDescriptor desc;
        if (localDescriptor != null) {
            desc = new SyncFolderDescriptor(
                    folderID,
                    localDescriptor.localFolder,
                    localDescriptor.removeAdditionalFiles && remAddFiles
            );
            desc.fileCache = new ArrayList<>(count);
        } else {
            BCLib.LOGGER.warning(BCLib.isClient()
                    ? "Client"
                    : "Server" + " does not know Sync-Folder ID '" + folderID + "'");
            desc = null;
        }

        for (int i = 0; i < count; i++) {
            SubFile relPath = SubFile.deserialize(buf);
            if (desc != null) desc.fileCache.add(relPath);
        }

        return desc;
    }

    //Note: make sure loadCache was called before using this
    boolean hasRelativeFile(String relFile) {
        return fileCache.stream()
                        .filter(sf -> sf.equals(relFile))
                        .findFirst()
                        .isPresent();
    }

    //Note: make sure loadCache was called before using this
    boolean hasRelativeFile(SubFile subFile) {
        return hasRelativeFile(subFile.relPath);
    }

    //Note: make sure loadCache was called before using this
    SubFile getLocalSubFile(String relPath) {
        return fileCache.stream()
                        .filter(sf -> sf.relPath.equals(relPath))
                        .findFirst()
                        .orElse(null);
    }

    Stream<SubFile> relativeFilesStream() {
        loadCache();
        return fileCache.stream();
    }

    public Path mapAbsolute(String relPath) {
        return this.localFolder.resolve(relPath)
                               .normalize();
    }

    public Path mapAbsolute(SubFile subFile) {
        return this.localFolder.resolve(subFile.relPath)
                               .normalize();
    }

    public boolean acceptChildElements(Path absPath) {
        return PathUtil.isChildOf(this.localFolder, absPath);
    }

    public boolean acceptChildElements(SubFile subFile) {
        return acceptChildElements(mapAbsolute(subFile));
    }

    public boolean discardChildElements(SubFile subFile) {
        return !acceptChildElements(subFile);
    }
}
