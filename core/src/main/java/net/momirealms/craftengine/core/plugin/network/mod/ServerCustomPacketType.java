package net.momirealms.craftengine.core.plugin.network.mod;

import net.momirealms.craftengine.core.plugin.network.codec.NetworkCodec;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;

public record ServerCustomPacketType<T extends ServerCustomPacket>(Key id, NetworkCodec<FriendlyByteBuf, T> codec) {
}
