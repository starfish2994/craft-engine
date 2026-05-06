package net.momirealms.craftengine.bukkit.plugin.network.listener.common;

import net.momirealms.craftengine.core.pack.host.ResourcePackHost;
import net.momirealms.craftengine.core.pack.host.ResourcePackResponseAction;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import net.momirealms.craftengine.core.plugin.logger.Debugger;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.event.ByteBufPacketEvent;
import net.momirealms.craftengine.core.plugin.network.listener.ByteBufferPacketListener;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.proxy.minecraft.network.ConnectionProxy;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.common.ServerCommonPacketListenerProxy;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.common.ServerboundResourcePackPacketProxy;
import net.momirealms.craftengine.proxy.minecraft.server.network.ServerConfigurationPacketListenerImplProxy;
import net.momirealms.craftengine.proxy.minecraft.server.network.config.JoinWorldTaskProxy;
import net.momirealms.craftengine.proxy.minecraft.server.network.config.ServerResourcePackConfigurationTaskProxy;

import java.util.Queue;
import java.util.UUID;

public final class ResourcePackListener implements ByteBufferPacketListener {
    public static final ByteBufferPacketListener INSTANCE = new ResourcePackListener();

    private ResourcePackListener() {}

    @Override
    public void onPacketReceive(NetWorkUser user, ByteBufPacketEvent event) {
        FriendlyByteBuf buf = event.getBuffer();

        UUID uuid;
        if (VersionHelper.isOrAbove1_20_3()) {
            uuid = buf.readUUID();
            if (!user.isResourcePackLoading(uuid)) {
                // 不是CraftEngine发送的资源包,不管
                return;
            }
        } else {
            uuid = null;
        }

        ResourcePackResponseAction action = ResourcePackResponseAction.byOrdinal(buf.readVarInt());

        ResourcePackHost host = CraftEngine.instance().packManager().resourcePackHost();
        host.response(user, action).whenComplete((returnAction, t) -> {
            if (t != null) {
                CraftEngine.instance().logger().warn(TranslationManager.instance().plainTranslation("host.handle_response_failed", user.name()), t);
                return;
            }
            // 响应不是最终的或者没有配置阶段就不处理后续
            if (returnAction.intermediate() || !VersionHelper.isOrAbove1_20_2()) return;
            event.setCancelled(true);
            Object packetListener = ConnectionProxy.INSTANCE.getPacketListener(user.connection());
            if (!ServerConfigurationPacketListenerImplProxy.CLASS.isInstance(packetListener)) return; // 不是配置阶段不处理
            Queue<Object> tasks = ServerConfigurationPacketListenerImplProxy.INSTANCE.getConfigurationTasks(packetListener);
            // 主线程上处理这个包
            CraftEngine.instance().scheduler().executeSync(() -> {
                try {
                    // 当客户端发出多次成功包的时候，finish会报错，我们忽略他
                    Object packet;
                    if (VersionHelper.isOrAbove1_20_3()) {
                        packet = ServerboundResourcePackPacketProxy.INSTANCE.newInstance(uuid, action);
                    } else {
                        packet = ServerboundResourcePackPacketProxy.INSTANCE.newInstance(action);
                    }
                    ServerCommonPacketListenerProxy.INSTANCE.handleResourcePackResponse(packetListener, packet);
                    if (VersionHelper.isPaper() && VersionHelper.isOrAbove1_21_7()) { // paper在1.21.7+增加了判断不会主动结束任务
                        ServerConfigurationPacketListenerImplProxy.INSTANCE.finishCurrentTask(packetListener, ServerResourcePackConfigurationTaskProxy.TYPE);
                    }
                } catch (Throwable e) {
                    Debugger.RESOURCE_PACK.warn(() -> "Cannot finish current task", e);
                } finally { // 无论如何如果全部资源包任务处理完成就加入并执行JoinWorldTask
                    if (tasks.isEmpty()) {
                        returnToWorld(tasks, packetListener);
                    }
                }
            });
        });
    }

    private static void returnToWorld(Queue<Object> tasks, Object packetListener) {
        tasks.add(JoinWorldTaskProxy.INSTANCE.newInstance());
        ServerConfigurationPacketListenerImplProxy.INSTANCE.startNextTask(packetListener);
    }
}
