package net.momirealms.craftengine.proxy.common.network.listener.game;

import net.kyori.adventure.text.Component;
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

public final class SetScoreListener {
    private SetScoreListener() {}

    public static void register(PacketHandlerRegistry registry, ProxyCraftEngine plugin) {
        PacketRoute route = PacketRoute.typed(ConnectionState.PLAY, PacketType.Play.Server.UPDATE_SCORE);
        registry.registerSince(route, ClientVersion.V_1_20_3, new Handler(plugin));
    }

    private static final class Handler implements PacketHandler {
        private final ProxyCraftEngine plugin;

        private Handler(ProxyCraftEngine plugin) {
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

            boolean isChanged = false;
            String owner = buf.readUtf();
            String objectiveName = buf.readUtf();
            int score = buf.readVarInt();
            boolean hasDisplay = buf.readBoolean();
            Tag displayName = null;
            if (hasDisplay) {
                displayName = buf.readNbt(false);
            }
            outside:
            if (displayName != null) {
                Map<String, ComponentProvider> tokens = netWorkTagData.matchNetworkTags(displayName);
                if (tokens.isEmpty()) break outside;
                Component component = AdventureHelper.tagToComponent(clientVersion, displayName);
                component = AdventureHelper.replaceText(component, tokens, context);
                displayName = AdventureHelper.componentToTag(clientVersion, component);
                isChanged = true;
            }
            boolean hasNumberFormat = buf.readBoolean();
            int format = -1;
            Tag style = null;
            Tag fixed = null;
            if (hasNumberFormat) {
                format = buf.readVarInt();
                if (format == 0) {
                    if (displayName == null) return;
                } else if (format == 1) {
                    if (displayName == null) return;
                    style = buf.readNbt(false);
                } else if (format == 2) {
                    fixed = buf.readNbt(false);
                    if (fixed == null) return;
                    Map<String, ComponentProvider> tokens = netWorkTagData.matchNetworkTags(fixed);
                    if (tokens.isEmpty() && !isChanged) return;
                    if (!tokens.isEmpty()) {
                        Component component = AdventureHelper.tagToComponent(clientVersion, fixed);
                        component = AdventureHelper.replaceText(component, tokens, context);
                        fixed = AdventureHelper.componentToTag(clientVersion, component);
                        isChanged = true;
                    }
                }
            }
            if (isChanged) {
                final Tag displayNameF = displayName;
                final int formatF = format;
                final Tag styleF = style;
                final Tag fixedF = fixed;
                packet.rewritePayload(replaceBuf -> {
                    replaceBuf.writeVarInt(packet.packetID());
                    replaceBuf.writeUtf(owner);
                    replaceBuf.writeUtf(objectiveName);
                    replaceBuf.writeVarInt(score);
                    if (hasDisplay) {
                        replaceBuf.writeBoolean(true);
                        replaceBuf.writeNbt(displayNameF, false);
                    } else {
                        replaceBuf.writeBoolean(false);
                    }
                    if (hasNumberFormat) {
                        replaceBuf.writeBoolean(true);
                        replaceBuf.writeVarInt(formatF);
                        if (formatF == 1) {
                            replaceBuf.writeNbt(styleF, false);
                        } else if (formatF == 2) {
                            replaceBuf.writeNbt(fixedF, false);
                        }
                    } else {
                        replaceBuf.writeBoolean(false);
                    }
                });
            }
        }
    }
}
