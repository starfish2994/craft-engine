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
import java.util.UUID;

public final class SetBossBarListener {
    private SetBossBarListener() {}

    public static void register(PacketHandlerRegistry registry, ProxyCraftEngine plugin) {
        PacketRoute route = PacketRoute.typed(ConnectionState.PLAY, PacketType.Play.Server.BOSS_BAR);
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
            UUID uuid = buf.readUUID();
            int actionType = buf.readVarInt();
            if (actionType == 0) {
                String json = buf.readUtf();
                Map<String, ComponentProvider> tokens = netWorkTagData.matchNetworkTags(json);
                if (tokens.isEmpty()) return;

                float health = buf.readFloat();
                int color = buf.readVarInt();
                int division = buf.readVarInt();
                byte flag = buf.readByte();

                packet.rewritePayload(replaceBuf -> {
                    replaceBuf.writeVarInt(packet.packetID());
                    replaceBuf.writeUUID(uuid);
                    replaceBuf.writeVarInt(actionType);
                    replaceBuf.writeUtf(
                            AdventureHelper.componentToJson(
                                    clientVersion, AdventureHelper.replaceText(
                                            AdventureHelper.jsonToComponent(clientVersion, json), tokens, new NetworkTextReplaceContext(player, netWorkTagData)
                                    )
                            )
                    );
                    replaceBuf.writeFloat(health);
                    replaceBuf.writeVarInt(color);
                    replaceBuf.writeVarInt(division);
                    replaceBuf.writeByte(flag);
                });
            } else if (actionType == 3) {
                String json = buf.readUtf();
                Map<String, ComponentProvider> tokens = netWorkTagData.matchNetworkTags(json);
                if (tokens.isEmpty()) return;

                packet.rewritePayload(replaceBuf -> {
                    replaceBuf.writeVarInt(packet.packetID());
                    replaceBuf.writeUUID(uuid);
                    replaceBuf.writeVarInt(actionType);
                    replaceBuf.writeUtf(
                            AdventureHelper.componentToJson(
                                    clientVersion, AdventureHelper.replaceText(
                                            AdventureHelper.jsonToComponent(clientVersion, json), tokens, new NetworkTextReplaceContext(player, netWorkTagData)
                                    )
                            )
                    );
                });
            }
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
            UUID uuid = buf.readUUID();
            int actionType = buf.readVarInt();
            if (actionType == 0) {
                Tag nbt = buf.readNbt(false);
                if (nbt == null) return;
                Map<String, ComponentProvider> tokens = netWorkTagData.matchNetworkTags(nbt);
                if (tokens.isEmpty()) return;

                float health = buf.readFloat();
                int color = buf.readVarInt();
                int division = buf.readVarInt();
                byte flag = buf.readByte();

                packet.rewritePayload(replaceBuf -> {
                    replaceBuf.writeVarInt(packet.packetID());
                    replaceBuf.writeUUID(uuid);
                    replaceBuf.writeVarInt(actionType);
                    replaceBuf.writeNbt(
                            AdventureHelper.componentToTag(
                                    clientVersion, AdventureHelper.replaceText(
                                            AdventureHelper.tagToComponent(clientVersion, nbt), tokens, new NetworkTextReplaceContext(player, netWorkTagData)
                                    )
                            ),
                            false
                    );
                    replaceBuf.writeFloat(health);
                    replaceBuf.writeVarInt(color);
                    replaceBuf.writeVarInt(division);
                    replaceBuf.writeByte(flag);
                });
            } else if (actionType == 3) {
                Tag nbt = buf.readNbt(false);
                if (nbt == null) return;
                Map<String, ComponentProvider> tokens = netWorkTagData.matchNetworkTags(nbt);
                if (tokens.isEmpty()) return;

                packet.rewritePayload(replaceBuf -> {
                    replaceBuf.writeVarInt(packet.packetID());
                    replaceBuf.writeUUID(uuid);
                    replaceBuf.writeVarInt(actionType);
                    replaceBuf.writeNbt(
                            AdventureHelper.componentToTag(
                                    clientVersion, AdventureHelper.replaceText(
                                            AdventureHelper.tagToComponent(clientVersion, nbt), tokens, new NetworkTextReplaceContext(player, netWorkTagData)
                                    )
                            ),
                            false
                    );
                });
            }
        }
    }
}
