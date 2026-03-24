package net.momirealms.craftengine.bukkit.block.entity.renderer;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.momirealms.craftengine.bukkit.entity.data.ItemEntityData;
import net.momirealms.craftengine.bukkit.util.PacketUtils;
import net.momirealms.craftengine.core.block.entity.render.DynamicBlockEntityRenderer;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
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

public class DynamicDropItemRenderer implements DynamicBlockEntityRenderer {
    @NotNull
    public Item displayItem;
    public final WorldPosition position;
    public final int vehicleId;
    public final int passengerId;
    public final Object despawnAllPacket;
    public final Object despawnVehiclePacket;
    public final Object despawnPassengerPacket;
    public final Object spawnVehiclePacket;
    public final Object spawnPassengerPacket;
    public final Object ridePacket;

    public DynamicDropItemRenderer(@NotNull Item displayItem, @NotNull WorldPosition position) {
        this.displayItem = displayItem;
        this.position = position;
        this.vehicleId = EntityProxy.ENTITY_COUNTER.incrementAndGet();
        this.passengerId = EntityProxy.ENTITY_COUNTER.incrementAndGet();
        this.spawnVehiclePacket = ClientboundAddEntityPacketProxy.INSTANCE.newInstance(
                vehicleId, UUID.randomUUID(), position.x, position.y, position.z,
                0, 0, EntityTypeProxy.ITEM_DISPLAY, 0, Vec3Proxy.ZERO, 0
        );
        this.spawnPassengerPacket = ClientboundAddEntityPacketProxy.INSTANCE.newInstance(
                passengerId, UUID.randomUUID(), position.x, position.y, position.z,
                0, 0, EntityTypeProxy.ITEM, 0, Vec3Proxy.ZERO, 0
        );
        this.ridePacket = PacketUtils.createClientboundSetPassengersPacket(vehicleId, passengerId);
        this.despawnAllPacket = ClientboundRemoveEntitiesPacketProxy.INSTANCE.newInstance(MiscUtils.init(new IntArrayList(),
                a -> {
                    a.add(vehicleId);
                    a.add(passengerId);
                }
        ));
        this.despawnVehiclePacket = ClientboundRemoveEntitiesPacketProxy.INSTANCE.newInstance(MiscUtils.init(new IntArrayList(), a -> a.add(vehicleId)));
        this.despawnPassengerPacket = ClientboundRemoveEntitiesPacketProxy.INSTANCE.newInstance(MiscUtils.init(new IntArrayList(), a -> a.add(passengerId)));
    }

    // 更新展示的物品
    public void displayItem(@NotNull Item displayItem) {
        this.displayItem = displayItem;
    }

    @Override
    public void show(Player player) {
        player.sendPackets(List.of(
                this.spawnVehiclePacket,
                this.spawnPassengerPacket,
                this.ridePacket,
                ClientboundSetEntityDataPacketProxy.INSTANCE.newInstance(this.passengerId, new ArrayList<>() {{
                    ItemEntityData.Item.addEntityData(displayItem.getMinecraftItem(), this);
                }})
        ), false);
    }

    @Override
    public void hide(Player player) {
        player.sendPacket(this.despawnAllPacket, false);
    }

    @Override
    public void update(Player player) {
        Object changeDisplayItemPacket = ClientboundSetEntityDataPacketProxy.INSTANCE.newInstance(this.passengerId, new ArrayList<>() {{
            ItemEntityData.Item.addEntityData(displayItem.getMinecraftItem(), this);
        }});
        player.sendPackets(List.of(
                despawnPassengerPacket, spawnPassengerPacket, ridePacket, changeDisplayItemPacket
        ), false);
    }

}
