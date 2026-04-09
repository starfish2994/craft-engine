package net.momirealms.craftengine.bukkit.entity.furniture;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.momirealms.craftengine.core.entity.furniture.Collider;
import net.momirealms.craftengine.core.entity.furniture.FurnitureSnapshotState;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElement;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBox;
import net.momirealms.craftengine.core.plugin.context.ContextKey;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.proxy.minecraft.world.entity.EntityProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelWriterProxy;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;

public final class BukkitVariantSnapshot extends FurnitureSnapshotState {

    public BukkitVariantSnapshot(List<FurnitureElement> elements, List<FurnitureHitBox> hitboxes, Int2ObjectMap<FurnitureHitBox> hitboxMap, List<Collider> colliders, Map<ContextKey<?>, Object> customData) {
        super(elements, hitboxes, hitboxMap, colliders, customData);
    }

    @Override
    public void addCollidersToWorld(World cWorld) {
        Object world = cWorld.minecraftWorld();
        for (Collider entity : super.colliders) {
            Entity bukkitEntity = EntityProxy.INSTANCE.getBukkitEntity(entity.handle());
            bukkitEntity.getPersistentDataContainer().set(BukkitFurnitureManager.FURNITURE_COLLISION, PersistentDataType.BYTE, (byte) 1);
            bukkitEntity.setPersistent(false);
            if (!bukkitEntity.isValid()) {
                LevelWriterProxy.INSTANCE.addFreshEntity(world, entity.handle(), CreatureSpawnEvent.SpawnReason.CUSTOM);
            }
        }
    }
}
