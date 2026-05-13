package net.momirealms.craftengine.bukkit.entity.furniture;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.entity.furniture.Collider;
import net.momirealms.craftengine.core.entity.furniture.FurnitureSnapshotState;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElement;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBox;
import net.momirealms.craftengine.core.plugin.logger.Debugger;
import net.momirealms.craftengine.core.util.CustomDataType;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.proxy.minecraft.world.entity.EntityProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelWriterProxy;
import net.momirealms.craftengine.proxy.spottedleaf.moonrise.common.util.TickThreadProxy;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;

public final class BukkitVariantSnapshot extends FurnitureSnapshotState {

    public BukkitVariantSnapshot(List<FurnitureElement> elements,
                                 List<FurnitureHitBox> hitboxes,
                                 Int2ObjectMap<FurnitureHitBox> hitboxMap,
                                 List<Collider> colliders,
                                 Map<CustomDataType<?>, Object> customData) {
        super(elements, hitboxes, hitboxMap, colliders, customData);
    }

    @Override
    public void addCollidersToWorld(World cWorld) {
        Object world = cWorld.minecraftWorld();
        for (Collider entity : super.colliders) {
            Object minecraftEntity = entity.handle();
            Entity bukkitEntity = EntityProxy.INSTANCE.getBukkitEntity(minecraftEntity);
            bukkitEntity.getPersistentDataContainer().set(BukkitFurnitureManager.FURNITURE_COLLISION, PersistentDataType.BYTE, (byte) 1);
            bukkitEntity.setPersistent(false);
            if (!bukkitEntity.isValid()) {
                if (VersionHelper.isFolia) {
                    if (TickThreadProxy.INSTANCE.isTickThreadFor(minecraftEntity)) {
                        try {
                            LevelWriterProxy.INSTANCE.addFreshEntity(world, minecraftEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                            return;
                        } catch (Throwable e) {
                            Debugger.FURNITURE.warn(() -> "Failed to add collider to world", e);
                        }
                    }
                    Debugger.FURNITURE.warnLazy(() -> {
                        BukkitFurniture furniture = BukkitFurnitureManager.instance().loadedFurnitureByColliderEntityId(entity.entityId());
                        Key id = furniture != null ? furniture.config.id() : null;
                        Vec3d furnitureLocation = furniture != null ? furniture.position().toVec3d() : null;
                        Vec3d colliderLocation = LocationUtils.toVec3d(bukkitEntity.getLocation());
                        return "furniture " + id + " at " + furnitureLocation + " and collider at " + colliderLocation + " are not on the same tick thread";
                    }, Throwable::new);
                    bukkitEntity.getScheduler().run(BukkitCraftEngine.instance().javaPlugin(),
                            t -> LevelWriterProxy.INSTANCE.addFreshEntity(world, minecraftEntity, CreatureSpawnEvent.SpawnReason.CUSTOM),
                            null
                    );
                    return;
                }
                LevelWriterProxy.INSTANCE.addFreshEntity(world, minecraftEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
            }
        }
    }
}
