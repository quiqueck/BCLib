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
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class RequestFiles extends DataHandler.FromClient<RequestFiles.RequestFilesPayload> {
    public static class RequestFilesPayload extends DataHandlerDescriptor.PacketPayload<RequestFilesPayload> {
        public final String token;
        List<AutoSyncID> files;

        protected RequestFilesPayload(String token, List<AutoSyncID> files) {
            super(DESCRIPTOR);
            this.token = token;
            this.files = files;
        }

        RequestFilesPayload(FriendlyByteBuf buf) {
            super(DESCRIPTOR);

            this.token = readString(buf);
            int count = buf.readInt();
            this.files = new ArrayList<>(count);

            if (Configs.MAIN_CONFIG.verboseLogging())
                BCLib.LOGGER.info("Client requested " + count + " Files:");
            for (int i = 0; i < count; i++) {
                AutoSyncID asid = AutoSyncID.deserializeData(buf);
                files.add(asid);
                if (Configs.MAIN_CONFIG.verboseLogging())
                    BCLib.LOGGER.info("	- " + asid);
            }
        }

        protected void write(FriendlyByteBuf buf) {
            writeString(buf, this.token);
            buf.writeInt(files.size());
            for (AutoSyncID a : files) {
                a.serializeData(buf);
            }
        }
    }

    public static final DataHandlerDescriptor<RequestFilesPayload> DESCRIPTOR = new DataHandlerDescriptor<>(
            DataHandlerDescriptor.Direction.CLIENT_TO_SERVER,
            ResourceLocation.fromNamespaceAndPath(
                    BCLib.MOD_ID,
                    "request_files"
            ),
            RequestFilesPayload::new,
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
        super(DESCRIPTOR.IDENTIFIER.id());
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
    protected RequestFilesPayload serializeDataOnClient() {
        newToken();
        return new RequestFilesPayload(currentToken, files);
    }

    String receivedToken = "";

    @Override
    protected void deserializeIncomingDataOnServer(
            RequestFilesPayload payload,
            Player player,
            PacketSender responseSender
    ) {
        receivedToken = payload.token;
        files = payload.files;
    }

    @Override
    protected void runOnServerGameThread(MinecraftServer server, Player player) {
        if (!Configs.SERVER_CONFIG.isAllowingAutoSync()) {
            BCLib.LOGGER.info("Auto-Sync was disabled on the server.");
            return;
        }

        List<AutoFileSyncEntry> syncEntries = files.stream()
                                                   .map(AutoFileSyncEntry::findMatching)
                                                   .filter(Objects::nonNull)
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
