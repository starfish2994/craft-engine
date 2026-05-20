package net.momirealms.craftengine.core.plugin.network.mod;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslationArgument;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.codec.NetworkCodec;
import net.momirealms.craftengine.core.plugin.network.mod.protocol.*;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class CustomPackets {
    public static final int PROTOCOL_VERSION = 1;
    public static final NetworkCodec<FriendlyByteBuf, ClientboundLightPacket> LIGHT = registerClientbound(ClientboundLightPacket.ID, ClientboundLightPacket.CODEC);
    public static final NetworkCodec<FriendlyByteBuf, ClientboundVisualBlockStateBatchStartPacket> VISUAL_BLOCK_STATE_BATCH_START = registerClientbound(ClientboundVisualBlockStateBatchStartPacket.ID, ClientboundVisualBlockStateBatchStartPacket.CODEC);
    public static final NetworkCodec<FriendlyByteBuf, ClientboundVisualBlockStateBatchFinishedPacket> VISUAL_BLOCK_STATE_BATCH_FINISHED = registerClientbound(ClientboundVisualBlockStateBatchFinishedPacket.ID, ClientboundVisualBlockStateBatchFinishedPacket.CODEC);
    public static final NetworkCodec<FriendlyByteBuf, ClientboundVisualBlockStatesPacket> VISUAL_BLOCK_STATES = registerClientbound(ClientboundVisualBlockStatesPacket.ID, ClientboundVisualBlockStatesPacket.CODEC);
    public static final NetworkCodec<FriendlyByteBuf, ClientboundCancelBlockUpdateResponsePacket> CANCEL_BLOCK_UPDATE_RESPONSE = registerClientbound(ClientboundCancelBlockUpdateResponsePacket.ID, ClientboundCancelBlockUpdateResponsePacket.CODEC);
    public static final NetworkCodec<FriendlyByteBuf, ClientboundCreativeModeTabItemsPacket> CREATIVE_MODE_TAB_ITEMS = registerClientbound(ClientboundCreativeModeTabItemsPacket.ID, ClientboundCreativeModeTabItemsPacket.CODEC);
    public static final NetworkCodec<FriendlyByteBuf, ServerboundHandshakePacket> HANDSHAKE = registerServerbound(ServerboundHandshakePacket.ID, ServerboundHandshakePacket.CODEC);
    public static final NetworkCodec<FriendlyByteBuf, ServerboundEnableClientCustomBlockPacket> ENABLE_CLIENT_CUSTOM_BLOCK = registerServerbound(ServerboundEnableClientCustomBlockPacket.ID, ServerboundEnableClientCustomBlockPacket.CODEC);
    public static final NetworkCodec<FriendlyByteBuf, ServerboundCancelBlockUpdateRequestPacket> CANCEL_BLOCK_UPDATE_REQUEST = registerServerbound(ServerboundCancelBlockUpdateRequestPacket.ID, ServerboundCancelBlockUpdateRequestPacket.CODEC);
    public static final NetworkCodec<FriendlyByteBuf, ServerboundLegacyPacket> LEGACY_PACKET = registerServerbound(ServerboundLegacyPacket.ID, ServerboundLegacyPacket.CODEC);
    private static final Map<Key, Boolean> TRUSTED_PACKETS = new HashMap<>();

    private CustomPackets() {
    }

    public static void init() {
        registerTrustedPacket(ClientboundLightPacket.ID, ClientboundLightPacket.class);
    }

    public static <T extends ClientCustomPacket> NetworkCodec<FriendlyByteBuf, T> registerClientbound(Key id, NetworkCodec<FriendlyByteBuf, T> codec) {
        ((WritableRegistry<NetworkCodec<FriendlyByteBuf, ? extends ClientCustomPacket>>) BuiltInRegistries.CLIENT_MOD_PACKET)
                .register(ResourceKey.create(Registries.CLIENT_MOD_PACKET.location(), id), codec);
        return codec;
    }

    public static <T extends ServerCustomPacket> NetworkCodec<FriendlyByteBuf, T> registerServerbound(Key id, NetworkCodec<FriendlyByteBuf, T> codec) {
        ((WritableRegistry<NetworkCodec<FriendlyByteBuf, ? extends ServerCustomPacket>>) BuiltInRegistries.SERVER_MOD_PACKET)
                .register(ResourceKey.create(Registries.SERVER_MOD_PACKET.location(), id), codec);
        return codec;
    }

    public static boolean checkPermission(NetWorkUser user, Key id, boolean toClient) {
        if (!Config.modChannelRequiresPermission()) return true;
        Boolean isClient = TRUSTED_PACKETS.get(id);
        if (isClient != null && isClient == toClient) return true;
        String permission = "ce.mod." + (toClient ? "clientbound" : "serverbound") + "." + (id.namespace.equals("craftengine") ? id.value : id);
        boolean hasPermission = CraftEngine.instance().compatibilityManager().hasPermission(user, permission);
        if (!hasPermission && Config.modChannelLoggingPermissionDenied()) {
            CraftEngine.instance().logger().warn(TranslationManager.instance().plainTranslation(
                    toClient ? "mod.clientbound.no_permission" : "mod.serverbound.no_permission",
                    user.name(), permission, id.asString()
            ));
        }
        return hasPermission;
    }

    public static void registerTrustedPacket(@NotNull Key id, @NotNull Class<?> clazz) {
        TRUSTED_PACKETS.put(id, ClientCustomPacket.class.isAssignableFrom(clazz));
    }

    public static void checkProtocolVersion(NetWorkUser user) {
        if (user.clientModProtocol() == PROTOCOL_VERSION) return;
        user.kick(Component.translatable(
                "disconnect.craftengine.client_outdated",
                "Please update your CraftEngine Client Mod \n client protocol version " + user.clientModProtocol() + ", server protocol version " + PROTOCOL_VERSION,
                TranslationArgument.numeric(user.clientModProtocol()),
                TranslationArgument.numeric(PROTOCOL_VERSION)
        ));
    }
}
