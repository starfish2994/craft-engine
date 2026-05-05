package net.momirealms.craftengine.bukkit.plugin.network.listener;

import net.momirealms.craftengine.bukkit.entity.furniture.BukkitFurniture;
import net.momirealms.craftengine.bukkit.entity.furniture.BukkitFurnitureManager;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.event.ByteBufPacketEvent;
import net.momirealms.craftengine.core.plugin.network.listener.ByteBufferPacketListener;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.world.Vec3d;

public class InteractEntityListener26_1 implements ByteBufferPacketListener {
    public static final InteractEntityListener26_1 INSTANCE = new InteractEntityListener26_1();

    private InteractEntityListener26_1() {}

    @Override
    public void onPacketReceive(NetWorkUser user, ByteBufPacketEvent event) {
        BukkitServerPlayer serverPlayer = (BukkitServerPlayer) user;
        if (serverPlayer.isSpectatorMode()) return;
        // 交互实体的时候，应该取消挖掘
        serverPlayer.stopMiningBlock();

        FriendlyByteBuf buf = event.getBuffer();
        int rawId = buf.readVarInt();
        int entityId = CraftEngine.instance().compatibilityManager().remapEntityId(rawId);
        BukkitFurniture furniture = BukkitFurnitureManager.instance().loadedFurnitureByInteractableEntityId(entityId);
        if (furniture == null) return;
        // 太远就是挂
        if (!furniture.canInteract(serverPlayer)) return;

        InteractionHand hand = buf.readVarInt() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        Vec3d vec3d = buf.readLpVec3();
        boolean usingSecondaryAction = buf.readBoolean();

        if (entityId != furniture.entityId()) {
            event.setChanged(true);
            buf.clear();
            buf.writeVarInt(event.packetID());
            buf.writeVarInt(furniture.entityId());
            buf.writeVarInt(hand == InteractionHand.MAIN_HAND ? 0 : 1);
            buf.writeLpVec3(vec3d);
            buf.writeBoolean(usingSecondaryAction);
        }

        InteractEntityListener1_20.performInteractFurniture(furniture, entityId, serverPlayer, hand, usingSecondaryAction);
    }
}
