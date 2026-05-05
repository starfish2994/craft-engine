package net.momirealms.craftengine.bukkit.plugin.network.listener;

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

public class TabListListener1_20_3 implements ByteBufferPacketListener {
    public static final TabListListener1_20_3 INSTANCE = new TabListListener1_20_3();

    private TabListListener1_20_3() {}

    @Override
    public void onPacketSend(NetWorkUser user, ByteBufPacketEvent event) {
        if (!Config.interceptTabList()) return;
        FriendlyByteBuf buf = event.getBuffer();
        Tag nbt1 = buf.readNbt(false);
        if (nbt1 == null) return;
        Tag nbt2 = buf.readNbt(false);
        if (nbt2 == null) return;
        BukkitNetworkManager networkManager = BukkitNetworkManager.instance();
        Map<String, ComponentProvider> tokens1 = networkManager.matchNetworkTags(nbt1);
        Map<String, ComponentProvider> tokens2 = networkManager.matchNetworkTags(nbt2);
        if (tokens1.isEmpty() && tokens2.isEmpty()) return;
        event.setChanged(true);
        buf.clear();
        buf.writeVarInt(event.packetID());
        NetworkTextReplaceContext context = NetworkTextReplaceContext.of((BukkitServerPlayer) user);
        buf.writeNbt(tokens1.isEmpty() ? nbt1 : AdventureHelper.componentToTag(AdventureHelper.replaceText(AdventureHelper.tagToComponent(nbt1), tokens1, context)), false);
        buf.writeNbt(tokens2.isEmpty() ? nbt2 : AdventureHelper.componentToTag(AdventureHelper.replaceText(AdventureHelper.tagToComponent(nbt2), tokens2, context)), false);
    }
}
