package org.betterx.bclib.api.v2.dataexchange;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.dataexchange.handler.DataExchange;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;

/**
 * This is an internal class that handles a Clienetside players Connection to a Server
 */
@Environment(EnvType.CLIENT)
public class ConnectorClientside extends Connector {
    private Minecraft client;

    ConnectorClientside(DataExchange api) {
        super(api);
        this.client = null;
    }


    @Override
    public boolean onClient() {
        return true;
    }

    public void onPlayInit(ClientPacketListener handler, Minecraft client) {
        if (this.client != null && this.client != client) {
            BCLib.LOGGER.warning("Client changed!");
        }
        this.client = client;
        for (DataHandlerDescriptor desc : getDescriptors()) {
            ClientPlayNetworking.registerReceiver(desc.IDENTIFIER, (_client, _handler, _buf, _responseSender) -> {
                receiveFromServer(desc, _client, _handler, _buf, _responseSender);
            });
        }
    }

    public void onPlayReady(ClientPacketListener handler, PacketSender sender, Minecraft client) {
        for (DataHandlerDescriptor desc : getDescriptors()) {
            if (desc.sendOnJoin) {
                BaseDataHandler h = desc.JOIN_INSTANCE.get();
                if (!h.getOriginatesOnServer()) {
                    h.sendToServer(client);
                }
            }
        }
    }

    public void onPlayDisconnect(ClientPacketListener handler, Minecraft client) {
        for (DataHandlerDescriptor desc : getDescriptors()) {
            ClientPlayNetworking.unregisterReceiver(desc.IDENTIFIER);
        }
    }

    void receiveFromServer(
            DataHandlerDescriptor desc,
            Minecraft client,
            ClientPacketListener handler,
            FriendlyByteBuf buf,
            PacketSender responseSender
    ) {
        BaseDataHandler h = desc.INSTANCE.get();
        h.receiveFromServer(client, handler, buf, responseSender);
    }

    public void sendToServer(BaseDataHandler h) {
        if (client == null) {
            throw new RuntimeException("[internal error] Client not initialized yet!");
        }
        h.sendToServer(this.client);
    }
}
