package net.momirealms.craftengine.bukkit.plugin.network.listener;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.event.NMSPacketEvent;
import net.momirealms.craftengine.core.plugin.network.listener.NMSPacketListener;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.game.ServerboundContainerClickPacketProxy;

public class ContainerClickListener1_21_5 implements NMSPacketListener {
    public static final ContainerClickListener1_21_5 INSTANCE = new ContainerClickListener1_21_5();

    private ContainerClickListener1_21_5() {}

    @Override
    public void onPacketReceive(NetWorkUser user, NMSPacketEvent event, Object packet) {
        if (Config.disableItemOperations()) return;
        if (!VersionHelper.PREMIUM && !Config.interceptItem()) return;
        BukkitServerPlayer player = (BukkitServerPlayer) user;
        Int2ObjectMap<Object> changedSlots = ServerboundContainerClickPacketProxy.INSTANCE.getChangedSlots(packet);
        Int2ObjectMap<Object> newChangedSlots = new Int2ObjectOpenHashMap<>(changedSlots.size());
        for (Int2ObjectMap.Entry<Object> entry : changedSlots.int2ObjectEntrySet()) {
            newChangedSlots.put(entry.getIntKey(), FastNMS.INSTANCE.createInjectedHashedStack(entry.getValue(), player));
        }
        Object carriedItem = FastNMS.INSTANCE.createInjectedHashedStack(ServerboundContainerClickPacketProxy.INSTANCE.getCarriedItem(packet), player);
        ServerboundContainerClickPacketProxy.INSTANCE.setCarriedItem(packet, carriedItem);
        ServerboundContainerClickPacketProxy.INSTANCE.setChangedSlots(packet, Int2ObjectMaps.unmodifiable(newChangedSlots));
    }
}
