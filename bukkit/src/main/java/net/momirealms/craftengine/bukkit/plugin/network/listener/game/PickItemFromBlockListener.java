package net.momirealms.craftengine.bukkit.plugin.network.listener.game;

import net.momirealms.craftengine.bukkit.item.BukkitItem;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.event.ByteBufPacketEvent;
import net.momirealms.craftengine.core.plugin.network.listener.ByteBufferPacketListener;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.proxy.bukkit.craftbukkit.entity.CraftEntityProxy;
import net.momirealms.craftengine.proxy.minecraft.server.level.ServerPlayerProxy;
import net.momirealms.craftengine.proxy.minecraft.server.network.ServerGamePacketListenerImplProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.BlockGetterProxy;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class PickItemFromBlockListener implements ByteBufferPacketListener {
    public static final ByteBufferPacketListener INSTANCE = new PickItemFromBlockListener();

    private PickItemFromBlockListener() {}

    @Override
    public void onPacketReceive(NetWorkUser user, ByteBufPacketEvent event) {
        BukkitServerPlayer player = (BukkitServerPlayer) user;
        if (player == null) return;
        FriendlyByteBuf buf = event.getBuffer();
        BlockPos pos = buf.readBlockPos();
        // 太远了，有挂
        if (!player.canInteractPoint(new Vec3d(pos.x, pos.y, pos.z), 4)) {
            return;
        }
        CraftEngine.instance().scheduler().sync().run(
                () -> handlePickItemFromBlockPacketOnMainThread((BukkitServerPlayer) user, pos),
                player.platformPlayer().getWorld(), pos.x >> 4, pos.z >> 4
        );
    }

    private static void handlePickItemFromBlockPacketOnMainThread(BukkitServerPlayer player, Object pos) {
        Object serverLevel = player.world().minecraftWorld();
        Object blockState = BlockGetterProxy.INSTANCE.getBlockState(serverLevel, pos);
        Optional<ImmutableBlockState> optionalState = BlockStateUtils.getOptionalCustomBlockState(blockState);
        if (optionalState.isEmpty()) return;
        ImmutableBlockState customBlockState = optionalState.get();
        Item item = customBlockState.behavior().itemToPickup(player.world(), LocationUtils.fromBlockPos(pos), customBlockState, player);
        Object itemStack;
        if (item == null) {
            Key itemId = customBlockState.settings().itemId();
            if (itemId == null) return;
            BukkitItem wrappedItem = BukkitItemManager.instance().createWrappedItem(itemId, player);
            if (wrappedItem == null) return;
            itemStack = wrappedItem.minecraftItem();
        } else {
            itemStack = item.minecraftItem();
        }
        tryPickItem(player.platformPlayer(), itemStack, pos, null);
    }

    private static void tryPickItem(Player player, Object itemStack, @Nullable Object blockPos, @Nullable Object entity) {
        if (VersionHelper.isOrAbove1_21_5()) {
            ServerGamePacketListenerImplProxy.INSTANCE.tryPickItem(ServerPlayerProxy.INSTANCE.getConnection(CraftEntityProxy.INSTANCE.getEntity(player)), itemStack, blockPos, entity, true);
        } else if (VersionHelper.isOrAbove1_21_4()) {
            ServerGamePacketListenerImplProxy.INSTANCE.tryPickItem(ServerPlayerProxy.INSTANCE.getConnection(CraftEntityProxy.INSTANCE.getEntity(player)), itemStack);
        }
    }
}
