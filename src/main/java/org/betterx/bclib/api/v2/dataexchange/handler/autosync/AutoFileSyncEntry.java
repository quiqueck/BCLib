package org.betterx.bclib.api.v2.dataexchange.handler.autosync;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.dataexchange.DataHandler;
import org.betterx.bclib.api.v2.dataexchange.SyncFileHash;
import org.betterx.bclib.util.Pair;
import org.betterx.bclib.util.Triple;
import org.betterx.worlds.together.util.ModUtil;
import org.betterx.worlds.together.util.ModUtil.ModInfo;
import org.betterx.worlds.together.util.PathUtil;

import net.minecraft.network.FriendlyByteBuf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class AutoFileSyncEntry extends AutoSyncID {
    static class ForDirectFileRequest extends AutoFileSyncEntry {
        final File relFile;

        ForDirectFileRequest(String syncID, File relFile, File absFile) {
            super(AutoSyncID.ForDirectFileRequest.MOD_ID, syncID, absFile, false, (a, b, c) -> false);
            this.relFile = relFile;
        }

        @Override
        public int serializeContent(FriendlyByteBuf buf) {
            int res = super.serializeContent(buf);
            DataHandler.writeString(buf, relFile.toString());

            return res;
        }

        static AutoFileSyncEntry.ForDirectFileRequest finishDeserializeContent(String syncID, FriendlyByteBuf buf) {
            final String relFile = DataHandler.readString(buf);
            SyncFolderDescriptor desc = AutoSync.getSyncFolderDescriptor(syncID);
            if (desc != null) {
                //ensures that the file is not above the base-folder
                if (desc.acceptChildElements(desc.mapAbsolute(relFile))) {
                    return new AutoFileSyncEntry.ForDirectFileRequest(
                            syncID,
                            new File(relFile),
                            desc.localFolder.resolve(relFile)
                                            .normalize()
                                            .toFile()
                    );
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return uniqueID + " - " + relFile;
        }
    }

    static class ForModFileRequest extends AutoFileSyncEntry {
        public static File getLocalPathForID(String modID, boolean matchLocalVersion) {
            ModInfo mi = ModUtil.getModInfo(modID, matchLocalVersion);
            if (mi != null) {
                return mi.jarPath.toFile();
            }
            return null;
        }

        public final String version;

        ForModFileRequest(String modID, boolean matchLocalVersion, String version) {
            super(
                    modID,
                    AutoSyncID.ForModFileRequest.UNIQUE_ID,
                    getLocalPathForID(modID, matchLocalVersion),
                    false,
                    (a, b, c) -> false
            );
            if (this.fileName == null && matchLocalVersion) {
                BCLib.LOGGER.error("Unknown mod '" + modID + "'.");
            }
            if (version == null)
                this.version = ModUtil.getModVersion(modID);
            else
                this.version = version;
        }

        @Override
        public int serializeContent(FriendlyByteBuf buf) {
            final int res = super.serializeContent(buf);
            buf.writeInt(ModUtil.convertModVersion(version));
            return res;
        }

        static AutoFileSyncEntry.ForModFileRequest finishDeserializeContent(String modID, FriendlyByteBuf buf) {
            final String version = ModUtil.convertModVersion(buf.readInt());
            return new AutoFileSyncEntry.ForModFileRequest(modID, false, version);
        }

        @Override
        public String toString() {
            return "Mod " + modID + " (v" + version + ")";
        }
    }

    public final AutoSync.NeedTransferPredicate needTransfer;
    public final File fileName;
    public final boolean requestContent;
    private SyncFileHash hash;

    AutoFileSyncEntry(
            String modID,
            File fileName,
            boolean requestContent,
            AutoSync.NeedTransferPredicate needTransfer
    ) {
        this(modID, fileName.getName(), fileName, requestContent, needTransfer);
    }

    AutoFileSyncEntry(
            String modID,
            String uniqueID,
            File fileName,
            boolean requestContent,
            AutoSync.NeedTransferPredicate needTransfer
    ) {
        super(modID, uniqueID);
        this.needTransfer = needTransfer;
        this.fileName = fileName;
        this.requestContent = requestContent;
    }


    public SyncFileHash getFileHash() {
        if (hash == null) {
            hash = SyncFileHash.create(modID, fileName, uniqueID);
        }
        return hash;
    }

    public byte[] getContent() {
        if (!fileName.exists()) return new byte[0];
        final Path path = fileName.toPath();

        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {

        }
        return new byte[0];
    }

    public int serializeContent(FriendlyByteBuf buf) {
        DataHandler.writeString(buf, modID);
        DataHandler.writeString(buf, uniqueID);
        return serializeFileContent(buf);
    }

    public static Triple<AutoFileSyncEntry, byte[], AutoSyncID> deserializeContent(FriendlyByteBuf buf) {
        final String modID = DataHandler.readString(buf);
        final String uniqueID = DataHandler.readString(buf);
        byte[] data = deserializeFileContent(buf);

        AutoFileSyncEntry entry;
        if (AutoSyncID.ForDirectFileRequest.MOD_ID.equals(modID)) {
            entry = AutoFileSyncEntry.ForDirectFileRequest.finishDeserializeContent(uniqueID, buf);
        } else if (AutoSyncID.ForModFileRequest.UNIQUE_ID.equals(uniqueID)) {
            entry = AutoFileSyncEntry.ForModFileRequest.finishDeserializeContent(modID, buf);
        } else {
            entry = AutoFileSyncEntry.findMatching(modID, uniqueID);
        }
        return new Triple<>(entry, data, new AutoSyncID(modID, uniqueID));
    }


    public void serialize(FriendlyByteBuf buf) {
        getFileHash().serialize(buf);
        buf.writeBoolean(requestContent);

        if (requestContent) {
            serializeFileContent(buf);
        }
    }

    public static AutoSync.AutoSyncTriple deserializeAndMatch(FriendlyByteBuf buf) {
        Pair<SyncFileHash, byte[]> e = deserialize(buf);
        AutoFileSyncEntry match = findMatching(e.first);
        return new AutoSync.AutoSyncTriple(e.first, e.second, match);
    }

    public static Pair<SyncFileHash, byte[]> deserialize(FriendlyByteBuf buf) {
        SyncFileHash hash = SyncFileHash.deserialize(buf);
        boolean withContent = buf.readBoolean();
        byte[] data = null;
        if (withContent) {
            data = deserializeFileContent(buf);
        }

        return new Pair(hash, data);
    }

    private int serializeFileContent(FriendlyByteBuf buf) {
        if (!org.betterx.worlds.together.util.PathUtil.isChildOf(
                org.betterx.worlds.together.util.PathUtil.GAME_FOLDER,
                fileName.toPath()
        )) {
            BCLib.LOGGER.error(fileName + " is not within game folder " + PathUtil.GAME_FOLDER + ". Pretending it does not exist.");
            buf.writeInt(0);
            return 0;
        }

        byte[] content = getContent();
        buf.writeInt(content.length);
        buf.writeByteArray(content);
        return content.length;
    }

    private static byte[] deserializeFileContent(FriendlyByteBuf buf) {
        byte[] data;
        int size = buf.readInt();
        data = buf.readByteArray(size);
        return data;
    }


    public static AutoFileSyncEntry findMatching(SyncFileHash hash) {
        return findMatching(hash.modID, hash.uniqueID);
    }

    public static AutoFileSyncEntry findMatching(AutoSyncID aid) {
        if (aid instanceof AutoSyncID.ForDirectFileRequest) {
            AutoSyncID.ForDirectFileRequest freq = (AutoSyncID.ForDirectFileRequest) aid;
            SyncFolderDescriptor desc = AutoSync.getSyncFolderDescriptor(freq.uniqueID);
            if (desc != null) {
                SyncFolderDescriptor.SubFile subFile = desc.getLocalSubFile(freq.relFile.toString());
                if (subFile != null) {
                    final File absPath = desc.localFolder.resolve(subFile.relPath)
                                                         .normalize()
                                                         .toFile();
                    return new AutoFileSyncEntry.ForDirectFileRequest(
                            freq.uniqueID,
                            new File(subFile.relPath),
                            absPath
                    );
                }
            }
            return null;
        } else if (aid instanceof AutoSyncID.ForModFileRequest) {
            AutoSyncID.ForModFileRequest mreq = (AutoSyncID.ForModFileRequest) aid;
            return new AutoFileSyncEntry.ForModFileRequest(mreq.modID, true, null);
        }
        return findMatching(aid.modID, aid.uniqueID);
    }

    public static AutoFileSyncEntry findMatching(String modID, String uniqueID) {
        return AutoSync.getAutoSyncFiles()
                       .stream()
                       .filter(asf -> asf.modID.equals(modID) && asf.uniqueID.equals(uniqueID))
                       .findFirst()
                       .orElse(null);
    }
}
