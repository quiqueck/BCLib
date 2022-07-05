package org.betterx.bclib.api.v2.dataexchange;

import org.betterx.bclib.api.v2.dataexchange.handler.autosync.AutoSync;
import org.betterx.bclib.api.v2.dataexchange.handler.autosync.AutoSyncID;

import net.minecraft.network.FriendlyByteBuf;

import java.io.File;
import java.util.Objects;

/**
 * Calculates a hash based on the contents of a File.
 * <p>
 * A File-Hash contains the md5-sum of the File, as well as its size and byte-values from defined positions
 * <p>
 * You can compare instances using {@link #equals(Object)} to determine if two files are
 * identical.
 */
public class SyncFileHash extends AutoSyncID {
    public final FileHash hash;

    SyncFileHash(String modID, File file, byte[] md5, int size, int value) {
        this(modID, file.getName(), md5, size, value);
    }

    SyncFileHash(String modID, String uniqueID, byte[] md5, int size, int value) {
        this(modID, uniqueID, new FileHash(md5, size, value));
    }

    SyncFileHash(String modID, File file, FileHash hash) {
        this(modID, file.getName(), hash);
    }

    SyncFileHash(String modID, String uniqueID, FileHash hash) {
        super(modID, uniqueID);
        this.hash = hash;
    }


    final static AutoSync.NeedTransferPredicate NEED_TRANSFER = (clientHash, serverHash, content) -> !clientHash.equals(
            serverHash);

    @Override
    public String toString() {
        return super.toString() + ": " + hash.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SyncFileHash)) return false;
        if (!super.equals(o)) return false;
        SyncFileHash that = (SyncFileHash) o;
        return hash.equals(that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), hash);
    }

    /**
     * Serializes the Object to a buffer
     *
     * @param buf The buffer to write to
     */
    public void serialize(FriendlyByteBuf buf) {
        hash.serialize(buf);
        DataHandler.writeString(buf, modID);
        DataHandler.writeString(buf, uniqueID);
    }

    /**
     * Deserialize a Buffer to a new {@link SyncFileHash}-Object
     *
     * @param buf Thea buffer to read from
     * @return The received String
     */
    public static SyncFileHash deserialize(FriendlyByteBuf buf) {
        final FileHash hash = FileHash.deserialize(buf);
        final String modID = DataHandler.readString(buf);
        final String uniqueID = DataHandler.readString(buf);

        return new SyncFileHash(modID, uniqueID, hash);
    }

    /**
     * Create a new {@link SyncFileHash}.
     * <p>
     * Will call {@link #create(String, File, String)} using the name of the File as {@code uniqueID}.
     *
     * @param modID ID of the calling Mod
     * @param file  The input file
     * @return A new Instance. You can compare instances using {@link #equals(Object)} to determine if two files are
     * identical. Will return {@code null} when an error occurs or the File does not exist
     */
    public static SyncFileHash create(String modID, File file) {
        return create(modID, file, file.getName());
    }

    /**
     * Create a new {@link SyncFileHash}.
     *
     * @param modID    ID of the calling Mod
     * @param file     The input file
     * @param uniqueID The unique ID that is used for this File (see {@link SyncFileHash#uniqueID} for Details.
     * @return A new Instance. You can compare instances using {@link #equals(Object)} to determine if two files are
     * identical. Will return {@code null} when an error occurs or the File does not exist
     */
    public static SyncFileHash create(String modID, File file, String uniqueID) {
        return new SyncFileHash(modID, uniqueID, FileHash.create(file));
    }
}
