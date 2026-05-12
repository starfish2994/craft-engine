package net.momirealms.craftengine.proxy.velocity.network.inject;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.ReferenceCountUtil;
import net.momirealms.craftengine.proxy.common.network.ChannelConnection;
import net.momirealms.craftengine.proxy.common.network.packet.PacketSink;
import net.momirealms.craftengine.proxy.common.network.protocol.PacketSide;
import net.momirealms.craftengine.proxy.velocity.VelocityCraftEngine;

import java.util.List;

@ChannelHandler.Sharable
final class PacketDecoder extends MessageToMessageDecoder<ByteBuf> {
    private final PacketSink packetSink;
    private final ChannelConnection connection;
    private boolean relocated; // compression 启用后只重排一次

    PacketDecoder(PacketSink packetSink, ChannelConnection connection) {
        this.packetSink = packetSink;
        this.connection = connection;
        // 如果没有启用数据包压缩, 则直接标记, 无需触发重排.
        this.relocated = VelocityCraftEngine.instance().server.getConfiguration().getCompressionThreshold() == -1;
    }

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf message, List<Object> output) {
        // packetSink 可能返回原始 buffer、替换 buffer 或空 buffer 来取消 packet
        ByteBuf result = this.packetSink.handle(this.connection, this.connection.player(), PacketSide.CLIENT, message);
        if (!result.isReadable()) {
            if (result != message) {
                ReferenceCountUtil.release(result);
            }
            return;
        }
        output.add(result == message ? message.retain() : result);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext context, Object event) throws Exception {
        // Velocity 启用压缩会重建 codec 顺序, 需要把捕获 handler 放回 codec 前
        if (!this.relocated && PacketPipelineInjector.isCompressionEnabledEvent(event)) {
            this.relocated = true;
            PacketPipelineInjector.relocate(context.pipeline());
        }
        super.userEventTriggered(context, event);
    }
}
