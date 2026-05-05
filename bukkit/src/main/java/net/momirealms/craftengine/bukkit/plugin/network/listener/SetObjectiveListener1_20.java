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

public class SetObjectiveListener1_20 implements ByteBufferPacketListener {
    public static final SetObjectiveListener1_20 INSTANCE = new SetObjectiveListener1_20();

    private SetObjectiveListener1_20() {}

    @Override
    public void onPacketSend(NetWorkUser user, ByteBufPacketEvent event) {
        if (!Config.interceptScoreboard()) return;
        FriendlyByteBuf buf = event.getBuffer();
        String objective = buf.readUtf();
        byte mode = buf.readByte();
        if (mode != 0 && mode != 2) return;
        String displayName = buf.readUtf();
        int renderType = buf.readVarInt();
        Map<String, ComponentProvider> tokens = BukkitNetworkManager.instance().matchNetworkTags(displayName);
        if (tokens.isEmpty()) return;
        event.setChanged(true);
        buf.clear();
        buf.writeVarInt(event.packetID());
        buf.writeUtf(objective);
        buf.writeByte(mode);
        buf.writeUtf(AdventureHelper.componentToJson(AdventureHelper.replaceText(AdventureHelper.jsonToComponent(displayName), tokens, NetworkTextReplaceContext.of((BukkitServerPlayer) user))));
        buf.writeVarInt(renderType);
    }
}
