package net.momirealms.craftengine.bukkit.attribute.damage;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.entity.data.DisplayData;
import net.momirealms.craftengine.bukkit.util.ComponentUtils;
import net.momirealms.craftengine.bukkit.util.EntityUtils;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.scheduler.SchedulerAdapter;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.game.ClientboundAddEntityPacketProxy;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacketProxy;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.game.ClientboundSetEntityDataPacketProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.EntityTypesProxy;
import net.momirealms.craftengine.proxy.minecraft.world.phys.Vec3Proxy;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class DamageHologram {
    private static final byte BILLBOARD_CENTER = 3;
    private static final int POP_INTERPOLATION_TICKS = 4;
    private static final int SETTLE_INTERPOLATION_TICKS = 1;
    private static final int SHRINK_INTERPOLATION_TICKS = 3;
    private static final float SHRINK_SCALE = 0.01f;

    private final List<Player> viewers;
    private final Component text;
    private final Animation animation;
    private final int entityId;
    private final double x;
    private final double y;
    private final double z;

    public DamageHologram(List<Player> viewers, Component text, Animation animation, double x, double y, double z) {
        this.viewers = viewers;
        this.text = text;
        this.animation = animation;
        this.entityId = EntityUtils.ENTITY_COUNTER.incrementAndGet();
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void start() {
        List<Object> dataValues = new ArrayList<>(4);
        DisplayData.TextDisplayData.Text.addEntityData(ComponentUtils.adventureToMinecraft(this.text), dataValues);
        DisplayData.TextDisplayData.BackgroundColor.addEntityData(0, dataValues);
        DisplayData.BillboardConstraints.addEntityData(BILLBOARD_CENTER, dataValues);
        DisplayData.Scale.addEntityData(new Vector3f(this.animation.spawnScale()), dataValues);
        Object spawnPacket = ClientboundAddEntityPacketProxy.INSTANCE.newInstance(
                this.entityId, UUID.randomUUID(), this.x, this.y, this.z, 0, 0,
                EntityTypesProxy.TEXT_DISPLAY, 0, Vec3Proxy.ZERO, 0
        );
        Object dataPacket = ClientboundSetEntityDataPacketProxy.INSTANCE.newInstance(this.entityId, dataValues);
        for (Player viewer : this.viewers) {
            viewer.sendPackets(List.of(spawnPacket, dataPacket), false);
        }

        SchedulerAdapter scheduler = CraftEngine.instance().scheduler();
        scheduler.asyncLater(() -> sendScaleUpdate(this.animation.popScale(), POP_INTERPOLATION_TICKS),
                this.animation.popDelay() * 50, TimeUnit.MILLISECONDS);
        scheduler.asyncLater(() -> sendScaleUpdate(this.animation.settleScale(), SETTLE_INTERPOLATION_TICKS),
                this.animation.settleDelay() * 50, TimeUnit.MILLISECONDS);
        scheduler.asyncLater(() -> sendScaleUpdate(SHRINK_SCALE, SHRINK_INTERPOLATION_TICKS),
                this.animation.shrinkDelay() * 50, TimeUnit.MILLISECONDS);
        scheduler.asyncLater(this::remove, this.animation.removeDelay() * 50, TimeUnit.MILLISECONDS);
    }

    private void sendScaleUpdate(float scale, int interpolationTicks) {
        List<Object> dataValues = new ArrayList<>(4);
        DisplayData.TransformationInterpolationDelay.addEntityData(0, dataValues);
        if (VersionHelper.isOrAbove1_20_2) {
            DisplayData.TransformationInterpolationDuration.addEntityData(interpolationTicks, dataValues);
            DisplayData.PosRotInterpolationDuration.addEntityData(interpolationTicks, dataValues);
        } else {
            DisplayData.InterpolationDuration.addEntityData(interpolationTicks, dataValues);
        }
        DisplayData.Scale.addEntityData(new Vector3f(scale), dataValues);
        Object packet = ClientboundSetEntityDataPacketProxy.INSTANCE.newInstance(this.entityId, dataValues);
        for (Player viewer : this.viewers) {
            viewer.sendPacket(packet, false);
        }
    }

    private void remove() {
        Object packet = ClientboundRemoveEntitiesPacketProxy.INSTANCE.newInstance(new IntArrayList(new int[]{this.entityId}));
        for (Player viewer : this.viewers) {
            viewer.sendPacket(packet, false);
        }
    }

    public record Animation(float spawnScale, float popScale, float settleScale,
                            long popDelay, long settleDelay, long shrinkDelay, long removeDelay) {
    }
}
