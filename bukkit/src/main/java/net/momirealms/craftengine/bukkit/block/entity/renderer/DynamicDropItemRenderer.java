package net.momirealms.craftengine.bukkit.block.entity.renderer;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.momirealms.craftengine.bukkit.block.entity.DisplayItemEntity;
import net.momirealms.craftengine.bukkit.entity.data.ItemEntityData;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DynamicDropItemRenderer implements DynamicBlockEntityRenderer {
    public final DisplayItemEntity blockEntity;
    public WorldPosition displayItemPosition;
    @Nullable
    public Object lastUpdateMinecraftItem; // 最后一次发送更新掉落物品
    @NotNull
    public WorldPosition lastUpdateDisplayItemPosition; // 最后一次发送包位置时使用的展示物品位置.
    public final int vehicleId;
    public final int passengerId;
    public final UUID vehicleUUID = UUID.randomUUID();
    public final UUID passengeUUID = UUID.randomUUID();
    public final Object ridePacket;
    public final Object despawnVehiclePacket;
    public final Object despawnPassengerPacket;
    public final Object despawnAllPacket;
    public Object spawnVehiclePacket;
    public Object spawnPassengerPacket;
    public Object changeDisplayItemPacket; //

    public DynamicDropItemRenderer(@NotNull DisplayItemEntity blockEntity, @NotNull WorldPosition displayItemPosition) {
        this.blockEntity = blockEntity;
        this.displayItemPosition = displayItemPosition;
        this.lastUpdateDisplayItemPosition = displayItemPosition;
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
        this.refreshSpawnVehicleAndPassengerPacket(displayItemPosition);
    }

    // 更新展示的物品
    public void refreshChangeDisplayItemPacket(Object minecraftItem) {
        this.lastUpdateMinecraftItem = minecraftItem; // 更新缓存
        this.changeDisplayItemPacket = ClientboundSetEntityDataPacketProxy.INSTANCE.newInstance(this.passengerId, new ArrayList<>() {{
            ItemEntityData.Item.addEntityData(minecraftItem, this);
        }});
    }

    // 更新展示物品的位置, 刷新缓存的数据包
    public void refreshSpawnVehicleAndPassengerPacket(WorldPosition displayItemPosition) {
        this.spawnVehiclePacket = ClientboundAddEntityPacketProxy.INSTANCE.newInstance(
                vehicleId, vehicleUUID, displayItemPosition.x, displayItemPosition.y, displayItemPosition.z,
                0, 0, EntityTypeProxy.ITEM_DISPLAY, 0, Vec3Proxy.ZERO, 0
        );
        this.spawnPassengerPacket = ClientboundAddEntityPacketProxy.INSTANCE.newInstance(
                passengerId, passengeUUID, displayItemPosition.x, displayItemPosition.y, displayItemPosition.z,
                0, 0, EntityTypeProxy.ITEM, 0, Vec3Proxy.ZERO, 0
        );
    }

    @Override
    public void show(Player player) {
        // 刷新物品缓存包
        Object minecraftItem = blockEntity.displayItem().getMinecraftItem();
        if (lastUpdateMinecraftItem != minecraftItem) {
            this.refreshChangeDisplayItemPacket(minecraftItem);
        }
        // 发包
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
        // 如果最后发送的数据包位置和目前不一样时, 重发所有数据包;
        if (lastUpdateDisplayItemPosition != displayItemPosition) {
            this.hide(player);
            this.show(player);
        }
        // 位置一样, 只检查物品
        else {
            // 刷新缓存的掉落物的包
            Object minecraftItem = blockEntity.displayItem().getMinecraftItem();
            if (lastUpdateMinecraftItem != minecraftItem) {
                this.refreshChangeDisplayItemPacket(minecraftItem);
            }
            // 发包
            player.sendPackets(List.of(
                    despawnPassengerPacket, spawnPassengerPacket, ridePacket, changeDisplayItemPacket
            ), false);
        }
    }

}
