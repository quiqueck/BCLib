package org.betterx.bclib.api.v2.dataexchange.handler.autosync;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.dataexchange.DataExchangeAPI;
import org.betterx.bclib.api.v2.dataexchange.DataHandler;
import org.betterx.bclib.api.v2.dataexchange.DataHandlerDescriptor;
import org.betterx.bclib.config.Configs;
import org.betterx.worlds.together.util.ModUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;

import java.io.File;

/**
 * This message is sent once a player enters the world. It initiates a sequence of Messages that will sync files between both
 * client and server.
 * <table>
 * 	 <caption>Description</caption>
 * 	<tr>
 * 		<th>Server</th>
 * 		<th></th>
 * 		<th>Client</th>
 * 		<th></th>
 * 	</tr>
 * 	<tr>
 * 		<td colspan="4">Player enters World</td>
 * 	</tr>
 * 	<tr>
 * 		<td></td>
 * 		<td>&lt;--</td>
 * 		<td>{@link HelloServer}</td>
 * 		<td>Sends the current BLib-Version installed on the Client</td>
 * 	</tr>
 * 	<tr>
 * 		<td>{@link HelloClient}</td>
 * 		<td>--&gt;</td>
 * 		<td></td>
 * 		<td>Sends the current BClIb-Version, the Version of all Plugins and data for all AutpoSync-Files
 * 		({@link DataExchangeAPI#addAutoSyncFile(String, File)} on the Server</td>
 * 	</tr>
 * 	<tr>
 * 		<td></td>
 * 		<td>&lt;--</td>
 * 		<td>{@link RequestFiles}</td>
 * 		<td>Request missing or out of sync Files from the Server</td>
 * 	</tr>
 * 	<tr>
 * 		<td>{@link SendFiles}</td>
 * 		<td>--&gt;</td>
 * 		<td></td>
 * 		<td>Send Files from the Server to the Client</td>
 * 	</tr>
 * </table>
 */
public class HelloServer extends DataHandler.FromClient {
    public static final DataHandlerDescriptor DESCRIPTOR = new DataHandlerDescriptor(
            new ResourceLocation(
                    BCLib.MOD_ID,
                    "hello_server"
            ),
            HelloServer::new,
            true,
            false
    );

    protected String bclibVersion = "0.0.0";

    public HelloServer() {
        super(DESCRIPTOR.IDENTIFIER);
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
        BCLib.LOGGER.info("Sending hello to server.");
        buf.writeInt(ModUtil.convertModVersion(HelloClient.getBCLibVersion()));
    }

    @Override
    protected void deserializeIncomingDataOnServer(FriendlyByteBuf buf, Player player, PacketSender responseSender) {
        bclibVersion = ModUtil.convertModVersion(buf.readInt());
    }

    @Override
    protected void runOnServerGameThread(MinecraftServer server, Player player) {
        if (!Configs.SERVER_CONFIG.isAllowingAutoSync()) {
            BCLib.LOGGER.info("Auto-Sync was disabled on the server.");
            return;
        }

        String localBclibVersion = HelloClient.getBCLibVersion();
        BCLib.LOGGER.info("Received Hello from Client. (server=" + localBclibVersion + ", client=" + bclibVersion + ")");

        if (!server.isPublished()) {
            BCLib.LOGGER.info("Auto-Sync is disabled for Singleplayer worlds.");
            return;
        }

        reply(new HelloClient(), server);
    }
}
