package org.betterx.bclib.api.v2.dataexchange.handler.autosync;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.dataexchange.DataHandler;
import org.betterx.bclib.api.v2.dataexchange.DataHandlerDescriptor;
import org.betterx.bclib.config.Configs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RequestFiles extends DataHandler.FromClient {
    public static final DataHandlerDescriptor DESCRIPTOR = new DataHandlerDescriptor(
            new ResourceLocation(
                    BCLib.MOD_ID,
                    "request_files"
            ),
            RequestFiles::new,
            false,
            false
    );
    static String currentToken = "";

    protected List<AutoSyncID> files;

    private RequestFiles() {
        this(null);
    }

    public RequestFiles(List<AutoSyncID> files) {
        super(DESCRIPTOR.IDENTIFIER);
        this.files = files;
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected boolean prepareDataOnClient() {
        if (!Configs.CLIENT_CONFIG.isAllowingAutoSync()) {
            BCLib.LOGGER.info("Auto-Sync was disabled on the client.");
            return false;
        }
        return true;
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void serializeDataOnClient(FriendlyByteBuf buf) {
        newToken();
        writeString(buf, currentToken);

        buf.writeInt(files.size());

        for (AutoSyncID a : files) {
            a.serializeData(buf);
        }
    }

    String receivedToken = "";

    @Override
    protected void deserializeIncomingDataOnServer(FriendlyByteBuf buf, Player player, PacketSender responseSender) {
        receivedToken = readString(buf);
        int size = buf.readInt();
        files = new ArrayList<>(size);

        if (Configs.MAIN_CONFIG.verboseLogging())
            BCLib.LOGGER.info("Client requested " + size + " Files:");
        for (int i = 0; i < size; i++) {
            AutoSyncID asid = AutoSyncID.deserializeData(buf);
            files.add(asid);
            if (Configs.MAIN_CONFIG.verboseLogging())
                BCLib.LOGGER.info("	- " + asid);
        }


    }

    @Override
    protected void runOnServerGameThread(MinecraftServer server, Player player) {
        if (!Configs.SERVER_CONFIG.isAllowingAutoSync()) {
            BCLib.LOGGER.info("Auto-Sync was disabled on the server.");
            return;
        }

        List<AutoFileSyncEntry> syncEntries = files.stream()
                                                   .map(asid -> AutoFileSyncEntry.findMatching(asid))
                                                   .filter(e -> e != null)
                                                   .collect(Collectors.toList());

        reply(new SendFiles(syncEntries, receivedToken), server);
    }

    public static void newToken() {
        currentToken = UUID.randomUUID()
                           .toString();
    }

    static {
        newToken();
    }
}
