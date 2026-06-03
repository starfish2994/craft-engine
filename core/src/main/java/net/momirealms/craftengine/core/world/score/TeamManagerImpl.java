package net.momirealms.craftengine.core.world.score;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.LegacyChatFormatter;
import net.momirealms.craftengine.core.util.VersionHelper;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class TeamManagerImpl implements TeamManager {
    private static TeamManagerImpl instance;
    private final CraftEngine plugin;
    private final Map<LegacyChatFormatter, String> teamNameByColor = new EnumMap<>(LegacyChatFormatter.class);
    private List<ByteBuf> addTeamsPackets;

    public TeamManagerImpl(CraftEngine plugin) {
        instance = this;
        this.plugin = plugin;
    }

    public static TeamManagerImpl instance() {
        return instance;
    }

    @Override
    public String getTeamNameByColor(LegacyChatFormatter color) {
        return this.teamNameByColor.get(color);
    }

    @Override
    public List<ByteBuf> addTeamsPackets() {
        return this.addTeamsPackets;
    }

    @Override
    public void init() {
        List<ByteBuf> packets = new ObjectArrayList<>();
        int packetId = this.plugin.platform().packetIds().clientboundSetPlayerTeamPacket();
        LegacyChatFormatter[] values = LegacyChatFormatter.values();
        for (int i = 0; i < 16; i++) {
            LegacyChatFormatter color = values[i];
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeVarInt(packetId);
            String teamName = TeamManager.createTeamName(color);
            buf.writeUtf(teamName);
            buf.writeByte(0);
            buf.writeComponent(Component.text(teamName));
            buf.writeByte(3);
            if (VersionHelper.isOrAbove1_21_5) {
                buf.writeVarInt(0);
                buf.writeVarInt(0);
            } else {
                buf.writeUtf("always");
                buf.writeUtf("always");
            }
            buf.writeEnumConstant(color);
            buf.writeComponent(Component.empty());
            buf.writeComponent(Component.empty());
            buf.writeStringList(List.of());
            this.teamNameByColor.put(color, teamName);
            packets.add(buf);
        }
        this.addTeamsPackets = packets;
    }
}
