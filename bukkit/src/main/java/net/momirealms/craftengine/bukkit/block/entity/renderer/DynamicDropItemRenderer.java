package net.momirealms.craftengine.bukkit.block.entity.renderer;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.momirealms.craftengine.bukkit.block.entity.DisplayItemEntity;
import net.momirealms.craftengine.bukkit.entity.data.ItemEntityData;
import net.momirealms.craftengine.bukkit.util.EntityUtils;
import net.momirealms.craftengine.bukkit.util.PacketUtils;
import net.momirealms.craftengine.core.block.entity.render.DynamicBlockEntityRenderer;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.game.ClientboundAddEntityPacketProxy;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacketProxy;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.game.ClientboundSetEntityDataPacketProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.EntityProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.EntityTypeProxy;
import net.momirealms.craftengine.proxy.minecraft.world.phys.Vec3Proxy;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DynamicDropItemRenderer implements DynamicBlockEntityRenderer {
    public final DisplayItemEntity blockEntity;
    private WorldPosition displayItemPosition;
    @NotNull private Object lastUpdateMinecraftItem; // 最后一次发送更新掉落物品
    private boolean positionDirty; // 坐标脏位
    public final int vehicleId;
    public final int passengerId;
    public final UUID vehicleUUID = UUID.randomUUID();
    public final UUID passengeUUID = UUID.randomUUID();
    public final Object ridePacket;
    public final Object despawnVehiclePacket;
    public final Object despawnPassengerPacket;
    public final Object despawnAllPacket;
    @NotNull private Object spawnVehiclePacket;
    @NotNull private Object spawnPassengerPacket;
    @NotNull private Object changeDisplayItemPacket;
    @NotNull private Object updatePosPacket;

    public DynamicDropItemRenderer(@NotNull DisplayItemEntity blockEntity, @NotNull WorldPosition displayItemPosition) {
        this.blockEntity = blockEntity;
        this.displayItemPosition = displayItemPosition;
        this.vehicleId = EntityProxy.ENTITY_COUNTER.incrementAndGet();
        this.passengerId = EntityProxy.ENTITY_COUNTER.incrementAndGet();
        // 包缓存
        this.ridePacket = PacketUtils.createClientboundSetPassengersPacket(vehicleId, passengerId);
        this.despawnVehiclePacket = ClientboundRemoveEntitiesPacketProxy.INSTANCE.newInstance(MiscUtils.init(new IntArrayList(), a -> a.add(vehicleId)));
        this.despawnPassengerPacket = ClientboundRemoveEntitiesPacketProxy.INSTANCE.newInstance(MiscUtils.init(new IntArrayList(), a -> a.add(passengerId)));
        this.despawnAllPacket = ClientboundRemoveEntitiesPacketProxy.INSTANCE.newInstance(MiscUtils.init(new IntArrayList(),
                a -> {
                    a.add(vehicleId);
                    a.add(passengerId);
                }
        ));
        this.refreshChangeDisplayItemPacket(blockEntity.displayItem().getMinecraftItem());
        this.refreshSpawnVehicleAndPassengerPacket(displayItemPosition, false);
    }

    // 更新展示的物品
    public void refreshChangeDisplayItemPacket(Object minecraftItem) {
        this.lastUpdateMinecraftItem = minecraftItem; // 更新缓存
        this.changeDisplayItemPacket = ClientboundSetEntityDataPacketProxy.INSTANCE.newInstance(this.passengerId, new ArrayList<>() {{
            ItemEntityData.Item.addEntityData(minecraftItem, this);
        }});
    }

    // 更新展示物品的位置, 这里的 lastUpdateDisplayItemPosition 由 DisplayItemEntity#setBlockState 刷新.
    public void refreshSpawnVehicleAndPassengerPacket(WorldPosition displayItemPosition, boolean dirtyFlag) {
        this.positionDirty = dirtyFlag;
        this.displayItemPosition = displayItemPosition;
        this.spawnVehiclePacket = ClientboundAddEntityPacketProxy.INSTANCE.newInstance(
                vehicleId, vehicleUUID, displayItemPosition.x, displayItemPosition.y, displayItemPosition.z,
                0, 0, EntityTypeProxy.ITEM_DISPLAY, 0, Vec3Proxy.ZERO, 0
        );
        this.spawnPassengerPacket = ClientboundAddEntityPacketProxy.INSTANCE.newInstance(
                passengerId, passengeUUID, displayItemPosition.x, displayItemPosition.y, displayItemPosition.z,
                0, 0, EntityTypeProxy.ITEM, 0, Vec3Proxy.ZERO, 0
        );
        this.updatePosPacket = EntityUtils.createUpdatePosPacket(vehicleId, displayItemPosition.x, displayItemPosition.y, displayItemPosition.z,
                0.0f, 0.0f, true
        );
    }

    public void positionDirty(boolean dirtyFlag) {
        this.positionDirty = dirtyFlag;
    }

    @Override
    public void show(Player player) {
        player.sendPackets(List.of(
                this.spawnVehiclePacket,
                this.spawnPassengerPacket,
                this.ridePacket,
                this.changeDisplayItemPacket
        ), false);
    }

    @Override
    public void hide(Player player) {
        player.sendPacket(this.despawnAllPacket, false);
    }

    @Override
    public void update(Player player) {
        // 检查最新的物品和当前刷新的是否一样, 不一样则刷新缓存的包.
        Object minecraftItem = blockEntity.displayItem().getMinecraftItem();
        if (lastUpdateMinecraftItem != minecraftItem) {
            this.refreshChangeDisplayItemPacket(minecraftItem);
        }
        // 如果缓存的显示位置和最新的不一样, 额外发送一个同步位置包.
        if (this.positionDirty) {
            player.sendPacket(updatePosPacket, false);
        }
        // 重发物品刷新包.
        player.sendPackets(List.of(
                despawnPassengerPacket, spawnPassengerPacket, ridePacket, changeDisplayItemPacket
        ), false);
    }
}
