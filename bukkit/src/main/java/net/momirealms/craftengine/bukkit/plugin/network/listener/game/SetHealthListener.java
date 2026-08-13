package net.momirealms.craftengine.bukkit.plugin.network.listener.game;

import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.event.ByteBufPacketEvent;
import net.momirealms.craftengine.core.plugin.network.listener.ByteBufferPacketListener;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.VersionHelper;

public final class SetHealthListener implements ByteBufferPacketListener {
    public static final SetHealthListener INSTANCE = new SetHealthListener();

    @Override
    public void onPacketSend(NetWorkUser user, ByteBufPacketEvent event) {
        if (!VersionHelper.isOrAbove1_20_5) return;
        if (!Config.enableHealthScaling()) return;
        BukkitServerPlayer player = (BukkitServerPlayer) user;
        double maxHealth = player.clientSideMaxHealth();
        if (maxHealth <= Config.healthScalingThreshold()) return;
        double visualMax = Config.healthScalingVisualMaxHealth();

        FriendlyByteBuf buf = event.getBuffer();
        float health = buf.readFloat();
        int food = buf.readVarInt();
        float saturation = buf.readFloat();

        float scaled = (float) Math.clamp(health * visualMax / maxHealth, 0d, visualMax);
        event.setChanged(true);
        buf.clear();
        buf.writeVarInt(event.packetID());
        buf.writeFloat(scaled);
        buf.writeVarInt(food);
        buf.writeFloat(saturation);
    }
}
