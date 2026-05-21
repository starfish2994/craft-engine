package net.momirealms.craftengine.core.plugin.network.mod;

import net.momirealms.craftengine.core.plugin.network.codec.NetworkCodec;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;

public record ClientCustomPacketType<T extends ClientCustomPacket>(Key id, NetworkCodec<FriendlyByteBuf, T> codec, boolean inServerHandle) {
}
