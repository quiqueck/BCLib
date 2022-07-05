package org.betterx.bclib.api.v2.dataexchange.handler.autosync;

import org.betterx.bclib.api.v2.dataexchange.DataHandler;
import org.betterx.bclib.config.Config;
import org.betterx.worlds.together.util.ModUtil;

import net.minecraft.network.FriendlyByteBuf;

import java.io.File;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class AutoSyncID {
    static class WithContentOverride extends AutoSyncID {
        final FileContentWrapper contentWrapper;
        final File localFile;

        WithContentOverride(String modID, String uniqueID, FileContentWrapper contentWrapper, File localFile) {
            super(modID, uniqueID);
            this.contentWrapper = contentWrapper;
            this.localFile = localFile;
        }

        @Override
        public String toString() {
            return super.toString() + " (Content override)";
        }
    }

    static class ForDirectFileRequest extends AutoSyncID {
        public final static String MOD_ID = "bclib::FILE";
        final File relFile;

        ForDirectFileRequest(String syncID, File relFile) {
            super(ForDirectFileRequest.MOD_ID, syncID);
            this.relFile = relFile;
        }

        @Override
        void serializeData(FriendlyByteBuf buf) {
            super.serializeData(buf);
            DataHandler.writeString(buf, relFile.toString());
        }

        static ForDirectFileRequest finishDeserialize(String modID, String uniqueID, FriendlyByteBuf buf) {
            final File fl = new File(DataHandler.readString(buf));
            return new ForDirectFileRequest(uniqueID, fl);
        }

        @Override
        public String toString() {
            return super.uniqueID + " (" + this.relFile + ")";
        }
    }

    static class ForModFileRequest extends AutoSyncID {
        public final static String UNIQUE_ID = "bclib::MOD";
        private final String version;

        ForModFileRequest(String modID, String version) {
            super(modID, ForModFileRequest.UNIQUE_ID);
            this.version = version;
        }

        @Override
        void serializeData(FriendlyByteBuf buf) {
            super.serializeData(buf);
            buf.writeInt(ModUtil.convertModVersion(version));
        }

        static ForModFileRequest finishDeserialize(String modID, String uniqueID, FriendlyByteBuf buf) {
            final String version = ModUtil.convertModVersion(buf.readInt());
            return new ForModFileRequest(modID, version);
        }

        @Override
        public String toString() {
            return super.modID + " (v" + this.version + ")";
        }
    }

    /**
     * A Unique ID for the referenced File.
     * <p>
     * Files with the same {@link #modID} need to have a unique IDs. Normally the filename from FileHash(String, File, byte[], int, int)
     * is used to generated that ID, but you can directly specify one using FileHash(String, String, byte[], int, int).
     */
    @NotNull
    public final String uniqueID;

    /**
     * The ID of the Mod that is registering the File
     */
    @NotNull
    public final String modID;

    public AutoSyncID(String modID, String uniqueID) {
        Objects.nonNull(modID);
        Objects.nonNull(uniqueID);

        this.modID = modID;
        this.uniqueID = uniqueID;
    }

    @Override
    public String toString() {
        return modID + "." + uniqueID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AutoSyncID)) return false;
        AutoSyncID that = (AutoSyncID) o;
        return uniqueID.equals(that.uniqueID) && modID.equals(that.modID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueID, modID);
    }

    void serializeData(FriendlyByteBuf buf) {
        DataHandler.writeString(buf, modID);
        DataHandler.writeString(buf, uniqueID);
    }

    static AutoSyncID deserializeData(FriendlyByteBuf buf) {
        String modID = DataHandler.readString(buf);
        String uID = DataHandler.readString(buf);

        if (ForDirectFileRequest.MOD_ID.equals(modID)) {
            return ForDirectFileRequest.finishDeserialize(modID, uID, buf);
        } else if (ForModFileRequest.UNIQUE_ID.equals(uID)) {
            return ForModFileRequest.finishDeserialize(modID, uID, buf);
        } else {
            return new AutoSyncID(modID, uID);
        }
    }

    public boolean isConfigFile() {
        return this.uniqueID.startsWith(Config.CONFIG_SYNC_PREFIX);
    }
}
