package net.momirealms.craftengine.proxy.common.network.listener.game;

import net.momirealms.craftengine.proxy.common.ProxyCraftEngine;
import net.momirealms.craftengine.proxy.common.context.NetworkTextReplaceContext;
import net.momirealms.craftengine.proxy.common.network.ChannelConnection;
import net.momirealms.craftengine.proxy.common.network.packet.PacketContext;
import net.momirealms.craftengine.proxy.common.network.packet.PacketHandler;
import net.momirealms.craftengine.proxy.common.network.packet.PacketHandlerRegistry;
import net.momirealms.craftengine.proxy.common.network.packet.PacketRoute;
import net.momirealms.craftengine.proxy.common.network.protocol.ConnectionState;
import net.momirealms.craftengine.proxy.common.network.protocol.packettype.PacketType;
import net.momirealms.craftengine.proxy.common.network.protocol.player.ClientVersion;
import net.momirealms.craftengine.proxy.common.platform.ProxyPlayer;
import net.momirealms.craftengine.proxy.common.tag.NetworkTagData;
import net.momirealms.craftengine.proxy.common.text.component.ComponentProvider;
import net.momirealms.craftengine.proxy.common.util.AdventureHelper;
import net.momirealms.craftengine.proxy.common.util.ProxyByteBuf;
import net.momirealms.sparrow.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class SetTitleTextListener {
    private SetTitleTextListener() {}

    public static void register(PacketHandlerRegistry registry, ProxyCraftEngine plugin) {
        PacketRoute route = PacketRoute.typed(ConnectionState.PLAY, PacketType.Play.Server.SET_TITLE_TEXT);
        registry.registerBetween(route, ClientVersion.V_1_20, ClientVersion.V_1_20_2, new V1_20(plugin));
        registry.registerSince(route, ClientVersion.V_1_20_3, new V1_20_3(plugin));
    }

    private static final class V1_20 implements PacketHandler {
        private final ProxyCraftEngine plugin;

        private V1_20(ProxyCraftEngine plugin) {
            this.plugin = plugin;
        }

        @Override
        public void handle(ChannelConnection connection, @Nullable ProxyPlayer player, PacketContext packet) {
            if (player == null) return;
            NetworkTagData netWorkTagData = this.plugin.networkTagDataSyncService().getTagData(player);
            if (netWorkTagData == null) return;

            ClientVersion clientVersion = packet.clientVersion();
            ProxyByteBuf buf = packet.payload();
            String json = buf.readUtf();
            Map<String, ComponentProvider> tokens = netWorkTagData.matchNetworkTags(json);
            if (tokens.isEmpty()) return;

            NetworkTextReplaceContext context = new NetworkTextReplaceContext(player, netWorkTagData);
            String replacedJson = AdventureHelper.componentToJson(clientVersion, AdventureHelper.replaceText(AdventureHelper.jsonToComponent(clientVersion, json), tokens, context));
            packet.rewritePayload(replaceBuf -> {
                replaceBuf.writeVarInt(packet.packetID());
                replaceBuf.writeUtf(replacedJson);
            });
        }
    }

    private static final class V1_20_3 implements PacketHandler {
        private final ProxyCraftEngine plugin;

        private V1_20_3(ProxyCraftEngine plugin) {
            this.plugin = plugin;
        }

        @Override
        public void handle(ChannelConnection connection, @Nullable ProxyPlayer player, PacketContext packet) {
            if (player == null) return;
            NetworkTagData netWorkTagData = this.plugin.networkTagDataSyncService().getTagData(player);
            if (netWorkTagData == null) return;

            ClientVersion clientVersion = packet.clientVersion();
            ProxyByteBuf buf = packet.payload();
            Tag nbt = buf.readNbt(false);
            if (nbt == null) return;
            Map<String, ComponentProvider> tokens = netWorkTagData.matchNetworkTags(nbt);
            if (tokens.isEmpty()) return;

            NetworkTextReplaceContext context = new NetworkTextReplaceContext(player, netWorkTagData);
            Tag tag = AdventureHelper.componentToTag(clientVersion, AdventureHelper.replaceText(AdventureHelper.tagToComponent(clientVersion, nbt), tokens, context));
            packet.rewritePayload(replaceBuf -> {
                replaceBuf.writeVarInt(packet.packetID());
                replaceBuf.writeNbt(tag, false);
            });
        }
    }
}
