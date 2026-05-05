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

import java.util.List;
import java.util.Map;

public class TeamListener1_20 implements ByteBufferPacketListener {
    public static final TeamListener1_20 INSTANCE = new TeamListener1_20();

    private TeamListener1_20() {}

    @Override
    public void onPacketSend(NetWorkUser user, ByteBufPacketEvent event) {
        if (!Config.interceptTeam()) return;
        FriendlyByteBuf buf = event.getBuffer();
        String name = buf.readUtf();
        byte method = buf.readByte();
        if (method != 2 && method != 0)
            return;
        String displayName = buf.readUtf();
        byte friendlyFlags = buf.readByte();
        String nameTagVisibility = buf.readUtf(40);
        String collisionRule = buf.readUtf(40);
        int color = buf.readVarInt();
        String prefix = buf.readUtf();
        String suffix = buf.readUtf();

        BukkitNetworkManager networkManager = BukkitNetworkManager.instance();
        Map<String, ComponentProvider> tokens1 = networkManager.matchNetworkTags(displayName);
        Map<String, ComponentProvider> tokens2 = networkManager.matchNetworkTags(prefix);
        Map<String, ComponentProvider> tokens3 = networkManager.matchNetworkTags(suffix);
        if (tokens1.isEmpty() && tokens2.isEmpty() && tokens3.isEmpty()) return;
        event.setChanged(true);
        NetworkTextReplaceContext context = NetworkTextReplaceContext.of((BukkitServerPlayer) user);

        List<String> entities = method == 0 ? buf.readStringList() : null;
        buf.clear();
        buf.writeVarInt(event.packetID());
        buf.writeUtf(name);
        buf.writeByte(method);
        buf.writeUtf(tokens1.isEmpty() ? displayName : AdventureHelper.componentToJson(AdventureHelper.replaceText(AdventureHelper.jsonToComponent(displayName), tokens1, context)));
        buf.writeByte(friendlyFlags);
        buf.writeUtf(nameTagVisibility);
        buf.writeUtf(collisionRule);
        buf.writeVarInt(color);
        buf.writeUtf(tokens2.isEmpty() ? prefix : AdventureHelper.componentToJson(AdventureHelper.replaceText(AdventureHelper.jsonToComponent(prefix), tokens2, context)));
        buf.writeUtf(tokens3.isEmpty() ? suffix : AdventureHelper.componentToJson(AdventureHelper.replaceText(AdventureHelper.jsonToComponent(suffix), tokens3, context)));
        if (entities != null) {
            buf.writeStringList(entities);
        }
    }
}
