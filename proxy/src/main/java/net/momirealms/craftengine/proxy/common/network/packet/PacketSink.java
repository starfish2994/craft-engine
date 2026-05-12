package net.momirealms.craftengine.proxy.common.network.packet;

import io.netty.buffer.ByteBuf;
import net.momirealms.craftengine.proxy.common.network.ChannelConnection;
import net.momirealms.craftengine.proxy.common.network.protocol.PacketSide;
import net.momirealms.craftengine.proxy.common.platform.ProxyPlayer;
import org.jetbrains.annotations.Nullable;

// 处理尚未转换为通用 ProxyPacketContext 的原始数据包
@FunctionalInterface
public interface PacketSink {
    ByteBuf handle(ChannelConnection connection, @Nullable ProxyPlayer player, PacketSide side, ByteBuf buffer);
}
