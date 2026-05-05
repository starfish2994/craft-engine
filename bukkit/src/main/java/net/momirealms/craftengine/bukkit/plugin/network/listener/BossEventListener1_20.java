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
import java.util.UUID;

public class BossEventListener1_20 implements ByteBufferPacketListener {
    public static final BossEventListener1_20 INSTANCE = new BossEventListener1_20();

    private BossEventListener1_20() {}

    @Override
    public void onPacketSend(NetWorkUser user, ByteBufPacketEvent event) {
        if (!Config.interceptBossBar()) return;
        FriendlyByteBuf buf = event.getBuffer();
        UUID uuid = buf.readUUID();
        int actionType = buf.readVarInt();
        if (actionType == 0) {
            String json = buf.readUtf();
            Map<String, ComponentProvider> tokens = BukkitNetworkManager.instance().matchNetworkTags(json);
            if (tokens.isEmpty()) return;
            float health = buf.readFloat();
            int color = buf.readVarInt();
            int division = buf.readVarInt();
            byte flag = buf.readByte();
            event.setChanged(true);
            buf.clear();
            buf.writeVarInt(event.packetID());
            buf.writeUUID(uuid);
            buf.writeVarInt(actionType);
            buf.writeUtf(AdventureHelper.componentToJson(AdventureHelper.replaceText(AdventureHelper.jsonToComponent(json), tokens, NetworkTextReplaceContext.of((BukkitServerPlayer) user))));
            buf.writeFloat(health);
            buf.writeVarInt(color);
            buf.writeVarInt(division);
            buf.writeByte(flag);
        } else if (actionType == 3) {
            String json = buf.readUtf();
            Map<String, ComponentProvider> tokens = BukkitNetworkManager.instance().matchNetworkTags(json);
            if (tokens.isEmpty()) return;
            event.setChanged(true);
            buf.clear();
            buf.writeVarInt(event.packetID());
            buf.writeUUID(uuid);
            buf.writeVarInt(actionType);
            buf.writeUtf(AdventureHelper.componentToJson(AdventureHelper.replaceText(AdventureHelper.jsonToComponent(json), tokens, NetworkTextReplaceContext.of((BukkitServerPlayer) user))));
        }
    }
}
