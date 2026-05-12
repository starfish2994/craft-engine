package net.momirealms.craftengine.proxy.bungeecord.network.inject;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import net.md_5.bungee.api.ProxyServer;
import net.momirealms.craftengine.proxy.bungeecord.BungeeCordCraftEngine;
import net.momirealms.craftengine.proxy.common.network.ChannelConnection;
import net.momirealms.craftengine.proxy.common.network.packet.PacketSink;
import net.momirealms.craftengine.proxy.common.util.ReflectionUtils;
import net.momirealms.craftengine.proxy.common.util.SetMonitor;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class PacketPipelineInjector {
    private static final String MINECRAFT_DECODER = "packet-decoder";
    private static final String MINECRAFT_ENCODER = "packet-encoder";
    private static final String PACKET_DECODER = "craftengine_proxy_packet_decoder";
    private static final String PACKET_ENCODER = "craftengine_proxy_packet_encoder";
    private static final Field LISTENERS_FIELD;
    private final BungeeCordCraftEngine plugin;
    private final PacketSink packetSink; // raw ByteBuf 捕获回调
    private final Consumer<ChannelConnection> connectionRegisterer; // 新 Channel 注册回调
    private final Consumer<ChannelConnection> connectionUnregister; // Channel 关闭清理回调
    private volatile boolean injected; // initializer 是否处于注入状态

    static {
        LISTENERS_FIELD = ReflectionUtils.getDeclaredField(ProxyServer.getInstance().getClass(), "listeners");
    }

    public PacketPipelineInjector(
            BungeeCordCraftEngine plugin,
            PacketSink packetSink,
            Consumer<ChannelConnection> connectionRegisterer,
            Consumer<ChannelConnection> connectionUnregister
    ) {
        this.plugin = plugin;
        this.packetSink = packetSink;
        this.connectionRegisterer = connectionRegisterer;
        this.connectionUnregister = connectionUnregister;
    }

    // 注入服务端 channel initializer
    @SuppressWarnings("unchecked")
    public void inject() {
        try {
            Set<Channel> listeners = (Set<Channel>) LISTENERS_FIELD.get(ProxyServer.getInstance());
            for (Channel channel : listeners) {
                this.injectChannel(channel);
            }

            Set<Channel> wrapper = new SetMonitor<>(listeners, this::injectChannel, channel -> {});
            LISTENERS_FIELD.set(ProxyServer.getInstance(), wrapper);
            this.injected = true;
        } catch (IllegalAccessException e) {
            this.plugin.getLogger().log(Level.SEVERE, "can't inject ", e); // todo
        }
    }

    // 撤销注入的 channel initializer.
    public void uninject() {
    }

    boolean injected() {
        return this.injected;
    }

    public void injectChannel(Channel channel) {
        Field initializerField = null;
        ChannelHandler bootstrapAcceptor = null;

        for (String channelName : channel.pipeline().names()) {
            if (channelName.contains("QueryHandler")) {
                return;
            }
            ChannelHandler handler = channel.pipeline().get(channelName);
            if (handler == null) continue;
            bootstrapAcceptor = handler;
            initializerField = ReflectionUtils.getDeclaredField(handler.getClass(), "childHandler");
        }
        if (bootstrapAcceptor == null) {
            bootstrapAcceptor = channel.pipeline().first();
            initializerField = ReflectionUtils.getDeclaredField(bootstrapAcceptor.getClass(), "childHandler");
        }

        try {
            @SuppressWarnings("unchecked")
            io.netty.channel.ChannelInitializer<Channel> newInitializer = new ChannelInitializer(
                    this,
                    (io.netty.channel.ChannelInitializer<Channel>) initializerField.get(bootstrapAcceptor),
                    this.packetSink,
                    this.connectionRegisterer,
                    this.connectionUnregister
            );
            initializerField.set(bootstrapAcceptor, newInitializer);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // 将数据包捕获 handler 添加到 BungeeCord Minecraft codec handler 之前
    public static void addTo(Channel channel, PacketSink packetSink, ChannelConnection connection) {
        ChannelPipeline pipeline = channel.pipeline();
        PacketPipelineInjector.removeHandlers(channel);

        if (pipeline.get(PacketPipelineInjector.MINECRAFT_DECODER) != null) {
            pipeline.addBefore(PacketPipelineInjector.MINECRAFT_DECODER, PacketPipelineInjector.PACKET_DECODER, new PacketDecoder(packetSink, connection));
        }
        if (pipeline.get(PacketPipelineInjector.MINECRAFT_ENCODER) != null) {
            pipeline.addBefore(PacketPipelineInjector.MINECRAFT_ENCODER, PacketPipelineInjector.PACKET_ENCODER, new PacketEncoder(packetSink, connection));
        }
    }

    // 从已注入的 channel 中移除 handler
    public static void removeHandlers(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        if (pipeline.get(PacketPipelineInjector.PACKET_DECODER) != null) {
            pipeline.remove(PacketPipelineInjector.PACKET_DECODER);
        }
        if (pipeline.get(PacketPipelineInjector.PACKET_ENCODER) != null) {
            pipeline.remove(PacketPipelineInjector.PACKET_ENCODER);
        }
    }

    // 启用压缩并改变 pipeline 后, 将数据包 handler 移回 BungeeCord codec 之前
    public static void relocate(ChannelPipeline pipeline) {
        PacketPipelineInjector.relocate(pipeline, PacketPipelineInjector.MINECRAFT_ENCODER, PacketPipelineInjector.PACKET_ENCODER);
        PacketPipelineInjector.relocate(pipeline, PacketPipelineInjector.MINECRAFT_DECODER, PacketPipelineInjector.PACKET_DECODER);
    }

    // 保留同一个 handler 实例, 并将其重新添加到目标 handler 之前
    private static void relocate(ChannelPipeline pipeline, String target, String handlerName) {
        ChannelHandler handler = pipeline.get(handlerName);
        if (handler != null && pipeline.get(target) != null) {
            pipeline.remove(handlerName);
            pipeline.addBefore(target, handlerName, handler);
        }
    }
}
