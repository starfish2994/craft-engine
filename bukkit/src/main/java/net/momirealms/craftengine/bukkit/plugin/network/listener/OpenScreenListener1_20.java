package net.momirealms.craftengine.bukkit.plugin.network.listener;

import net.momirealms.craftengine.bukkit.plugin.network.BukkitNetworkManager;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.context.NetworkTextReplaceContext;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.event.ByteBufPacketEvent;
import net.momirealms.craftengine.core.plugin.network.listener.ByteBufferPacketListener;
import net.momirealms.craftengine.core.plugin.text.component.ComponentProvider;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;

import java.util.Map;

public class OpenScreenListener1_20 implements ByteBufferPacketListener {
    public static final OpenScreenListener1_20 INSTANCE = new OpenScreenListener1_20();

    private OpenScreenListener1_20() {}

    @Override
    public void onPacketSend(NetWorkUser user, ByteBufPacketEvent event) {
        if (!Config.interceptContainer()) return;
        FriendlyByteBuf buf = event.getBuffer();
        int containerId = buf.readVarInt();
        int type = buf.readVarInt();
        String json = buf.readUtf();
        Map<String, ComponentProvider> tokens = BukkitNetworkManager.instance().matchNetworkTags(json);
        if (tokens.isEmpty()) return;
        event.setChanged(true);
        buf.clear();
        buf.writeVarInt(event.packetID());
        buf.writeVarInt(containerId);
        buf.writeVarInt(type);
        buf.writeUtf(AdventureHelper.componentToJson(AdventureHelper.replaceText(AdventureHelper.jsonToComponent(json), tokens, NetworkTextReplaceContext.of((BukkitServerPlayer) user))));
    }
}
