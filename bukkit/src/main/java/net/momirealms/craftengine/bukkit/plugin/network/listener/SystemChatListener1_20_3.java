package net.momirealms.craftengine.bukkit.plugin.network.listener;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.plugin.network.BukkitNetworkManager;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.context.NetworkTextReplaceContext;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.event.ByteBufPacketEvent;
import net.momirealms.craftengine.core.plugin.network.listener.ByteBufferPacketListener;
import net.momirealms.craftengine.core.plugin.text.component.ComponentProvider;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.sparrow.nbt.Tag;

import java.util.Map;

public class SystemChatListener1_20_3 implements ByteBufferPacketListener {
    public static final SystemChatListener1_20_3 INSTANCE = new SystemChatListener1_20_3();

    private SystemChatListener1_20_3() {}

    @Override
    public void onPacketSend(NetWorkUser user, ByteBufPacketEvent event) {
        if (!Config.interceptSystemChat() && Config.disableItemOperations()) return;
        FriendlyByteBuf buf = event.getBuffer();
        Tag nbt = buf.readNbt(false);
        if (nbt == null) return;
        boolean overlay = buf.readBoolean();
        event.setChanged(true);
        buf.clear();
        buf.writeVarInt(event.packetID());
        Component component = AdventureHelper.tagToComponent(nbt);
        if (Config.interceptSystemChat()) {
            Map<String, ComponentProvider> tokens = BukkitNetworkManager.instance().matchNetworkTags(nbt);
            if (!tokens.isEmpty()) {
                component = AdventureHelper.replaceText(component, tokens, NetworkTextReplaceContext.of((BukkitServerPlayer) user));
            }
        }
        if (!Config.disableItemOperations()) {
            component = AdventureHelper.replaceShowItem(component, s -> SystemChatListener1_20.replaceShowItem(s, (BukkitServerPlayer) user));
        }
        buf.writeNbt(AdventureHelper.componentToTag(component), false);
        buf.writeBoolean(overlay);
    }
}
