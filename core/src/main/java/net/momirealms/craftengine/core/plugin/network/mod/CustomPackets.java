package net.momirealms.craftengine.core.plugin.network.mod;

import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
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
    public static final NetworkCodec<FriendlyByteBuf, ClientboundLightPacket> LIGHT = registerClientbound(ClientboundLightPacket.ID, ClientboundLightPacket.CODEC);
    public static final NetworkCodec<FriendlyByteBuf, ClientboundVisualBlockStatePacket> VISUAL_BLOCK_STATE = registerClientbound(ClientboundVisualBlockStatePacket.ID, ClientboundVisualBlockStatePacket.CODEC);
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
        return CraftEngine.instance().compatibilityManager().hasPermission(user, permission);
    }

    public static void registerTrustedPacket(@NotNull Key id, @NotNull Class<?> clazz) {
        TRUSTED_PACKETS.put(id, ClientCustomPacket.class.isAssignableFrom(clazz));
    }
}
