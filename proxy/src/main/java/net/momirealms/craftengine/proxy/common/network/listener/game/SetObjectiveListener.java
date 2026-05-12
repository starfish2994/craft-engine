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

public final class SetObjectiveListener {
    private SetObjectiveListener() {}

    public static void register(PacketHandlerRegistry registry, ProxyCraftEngine plugin) {
        PacketRoute route = PacketRoute.typed(ConnectionState.PLAY, PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
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
            NetworkTextReplaceContext context = new NetworkTextReplaceContext(player, netWorkTagData);
            String objective = buf.readUtf();
            byte mode = buf.readByte();
            if (mode != 0 && mode != 2) return;
            String displayName = buf.readUtf();
            int renderType = buf.readVarInt();
            Map<String, ComponentProvider> tokens = netWorkTagData.matchNetworkTags(displayName);
            if (tokens.isEmpty()) return;
            packet.rewritePayload(replaceBuf -> {
                replaceBuf.writeVarInt(packet.packetID());
                replaceBuf.writeUtf(objective);
                replaceBuf.writeByte(mode);
                replaceBuf.writeUtf(AdventureHelper.componentToJson(clientVersion, AdventureHelper.replaceText(AdventureHelper.jsonToComponent(clientVersion, displayName), tokens, context)));
                replaceBuf.writeVarInt(renderType);
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
            NetworkTextReplaceContext context = new NetworkTextReplaceContext(player, netWorkTagData);
            String objective = buf.readUtf();
            byte mode = buf.readByte();
            if (mode != 0 && mode != 2) return;
            Tag displayName = buf.readNbt(false);
            if (displayName == null) return;
            int renderType = buf.readVarInt();
            boolean optionalNumberFormat = buf.readBoolean();

            if (optionalNumberFormat) {
                int format = buf.readVarInt();
                if (format == 0) {
                    Map<String, ComponentProvider> tokens = netWorkTagData.matchNetworkTags(displayName);
                    if (tokens.isEmpty()) return;
                    packet.rewritePayload(replaceBuf -> {
                        replaceBuf.writeVarInt(packet.packetID());
                        replaceBuf.writeUtf(objective);
                        replaceBuf.writeByte(mode);
                        replaceBuf.writeNbt(AdventureHelper.componentToTag(clientVersion, AdventureHelper.replaceText(AdventureHelper.tagToComponent(clientVersion, displayName), tokens, context)), false);
                        replaceBuf.writeVarInt(renderType);
                        replaceBuf.writeBoolean(true);
                        replaceBuf.writeVarInt(0);
                    });
                } else if (format == 1) {
                    Map<String, ComponentProvider> tokens = netWorkTagData.matchNetworkTags(displayName);
                    if (tokens.isEmpty()) return;
                    Tag style = buf.readNbt(false);
                    packet.rewritePayload(replaceBuf -> {
                        replaceBuf.writeVarInt(packet.packetID());
                        replaceBuf.writeUtf(objective);
                        replaceBuf.writeByte(mode);
                        replaceBuf.writeNbt(AdventureHelper.componentToTag(clientVersion, AdventureHelper.replaceText(AdventureHelper.tagToComponent(clientVersion, displayName), tokens, context)), false);
                        replaceBuf.writeVarInt(renderType);
                        replaceBuf.writeBoolean(true);
                        replaceBuf.writeVarInt(1);
                        replaceBuf.writeNbt(style, false);
                    });
                } else if (format == 2) {
                    Tag fixed = buf.readNbt(false);
                    if (fixed == null) return;
                    Map<String, ComponentProvider> tokens1 = netWorkTagData.matchNetworkTags(displayName);
                    Map<String, ComponentProvider> tokens2 = netWorkTagData.matchNetworkTags(fixed);
                    if (tokens1.isEmpty() && tokens2.isEmpty()) return;
                    packet.rewritePayload(replaceBuf -> {
                        replaceBuf.writeVarInt(packet.packetID());
                        replaceBuf.writeUtf(objective);
                        replaceBuf.writeByte(mode);
                        replaceBuf.writeNbt(tokens1.isEmpty() ? displayName : AdventureHelper.componentToTag(clientVersion, AdventureHelper.replaceText(AdventureHelper.tagToComponent(clientVersion, displayName), tokens1, context)), false);
                        replaceBuf.writeVarInt(renderType);
                        replaceBuf.writeBoolean(true);
                        replaceBuf.writeVarInt(2);
                        replaceBuf.writeNbt(tokens2.isEmpty() ? fixed : AdventureHelper.componentToTag(clientVersion, AdventureHelper.replaceText(AdventureHelper.tagToComponent(clientVersion, fixed), tokens2, context)), false);
                    });
                }
            } else {
                Map<String, ComponentProvider> tokens = netWorkTagData.matchNetworkTags(displayName);
                if (tokens.isEmpty()) return;
                packet.rewritePayload(replaceBuf -> {
                    replaceBuf.writeVarInt(packet.packetID());
                    replaceBuf.writeUtf(objective);
                    replaceBuf.writeByte(mode);
                    replaceBuf.writeNbt(AdventureHelper.componentToTag(clientVersion, AdventureHelper.replaceText(AdventureHelper.tagToComponent(clientVersion, displayName), tokens, context)), false);
                    replaceBuf.writeVarInt(renderType);
                    replaceBuf.writeBoolean(false);
                });
            }
        }
    }
}
