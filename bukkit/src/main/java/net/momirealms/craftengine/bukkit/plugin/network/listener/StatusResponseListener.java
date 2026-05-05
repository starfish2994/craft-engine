package net.momirealms.craftengine.bukkit.plugin.network.listener;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.event.ByteBufPacketEvent;
import net.momirealms.craftengine.core.plugin.network.listener.ByteBufferPacketListener;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;

public class StatusResponseListener implements ByteBufferPacketListener {
    public static final StatusResponseListener INSTANCE = new StatusResponseListener();

    private StatusResponseListener() {}

    @Override
    public void onPacketSend(NetWorkUser user, ByteBufPacketEvent event) {
        if (!Config.disableChatReport()) {
            return;
        }
        FriendlyByteBuf buf = event.getBuffer();
        JsonObject jsonObject = JsonParser.parseString(buf.readUtf()).getAsJsonObject();
        jsonObject.addProperty("preventsChatReports", true);
        event.setChanged(true);
        buf.clear();
        buf.writeVarInt(event.packetID());
        buf.writeUtf(jsonObject.toString());
    }
}
