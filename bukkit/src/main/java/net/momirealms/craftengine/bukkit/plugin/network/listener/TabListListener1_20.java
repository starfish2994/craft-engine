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

public class TabListListener1_20 implements ByteBufferPacketListener {
    public static final TabListListener1_20 INSTANCE = new TabListListener1_20();

    private TabListListener1_20() {}

    @Override
    public void onPacketSend(NetWorkUser user, ByteBufPacketEvent event) {
        if (!Config.interceptTabList()) return;
        FriendlyByteBuf buf = event.getBuffer();
        String json1 = buf.readUtf();
        String json2 = buf.readUtf();
        BukkitNetworkManager networkManager = BukkitNetworkManager.instance();
        Map<String, ComponentProvider> tokens1 = networkManager.matchNetworkTags(json1);
        Map<String, ComponentProvider> tokens2 = networkManager.matchNetworkTags(json2);
        if (tokens1.isEmpty() && tokens2.isEmpty()) return;
        event.setChanged(true);
        buf.clear();
        buf.writeVarInt(event.packetID());
        NetworkTextReplaceContext context = NetworkTextReplaceContext.of((BukkitServerPlayer) user);
        buf.writeUtf(tokens1.isEmpty() ? json1 : AdventureHelper.componentToJson(AdventureHelper.replaceText(AdventureHelper.jsonToComponent(json1), tokens1, context)));
        buf.writeUtf(tokens2.isEmpty() ? json2 : AdventureHelper.componentToJson(AdventureHelper.replaceText(AdventureHelper.jsonToComponent(json2), tokens2, context)));
    }
}
