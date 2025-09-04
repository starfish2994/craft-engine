package net.momirealms.craftengine.bukkit.world;

import com.google.common.collect.Lists;
import net.momirealms.craftengine.bukkit.entity.BukkitEntity;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.util.*;
import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.sound.SoundSource;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.*;
import net.momirealms.craftengine.core.world.collision.AABB;
import net.momirealms.craftengine.core.world.particle.ParticleData;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.SoundCategory;
import org.bukkit.entity.EnderDragonPart;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class BukkitWorld implements World {
    private final WeakReference<org.bukkit.World> world;
    private WorldHeight worldHeight;

    public BukkitWorld(org.bukkit.World world) {
        this.world = new WeakReference<>(world);
    }

    @Override
    public org.bukkit.World platformWorld() {
        return world.get();
    }

    @Override
    public Object serverWorld() {
        return FastNMS.INSTANCE.field$CraftWorld$ServerLevel(platformWorld());
    }

    @Override
    public WorldHeight worldHeight() {
        if (this.worldHeight == null) {
            this.worldHeight = WorldHeight.create(platformWorld().getMinHeight(), platformWorld().getMaxHeight() - platformWorld().getMinHeight());
        }
        return this.worldHeight;
    }

    @Override
    public ExistingBlock getBlockAt(int x, int y, int z) {
        return new BukkitExistingBlock(platformWorld().getBlockAt(x, y, z));
    }

    @Override
    public List<Entity> getEntities(@Nullable Entity entity, AABB aabb, Predicate<? super Entity> predicate) {
        List<Entity> entities = Lists.newArrayList();
        Object nmsAABB = FastNMS.INSTANCE.constructor$AABB(
                aabb.minX, aabb.minY, aabb.minZ,
                aabb.maxX, aabb.maxY, aabb.maxZ
        );
        BoundingBox bukkitAABB = new BoundingBox(
                aabb.minX, aabb.minY, aabb.minZ,
                aabb.maxX, aabb.maxY, aabb.maxZ
        );
        Object level = serverWorld();
        int entityCount = FastNMS.INSTANCE.method$EntityGetter$getEntitiesOfClass(level, nmsAABB, CoreReflections.clazz$Entity);
        if (entityCount == 0) {
            return entities;
        }
        for (org.bukkit.entity.Entity bukkitEntity : platformWorld().getNearbyEntities(bukkitAABB)) {
            if (bukkitEntity instanceof org.bukkit.entity.EnderDragonPart) {
                continue;
            }
            Entity craftEngineEntity = new BukkitEntity(bukkitEntity);
            if (!craftEngineEntity.equals(entity) && predicate.test(craftEngineEntity)) {
                entities.add(craftEngineEntity);
            }
        }
        for (EnderDragonPart dragonPart : platformWorld().getEntitiesByClass(EnderDragonPart.class)) {
            org.bukkit.util.BoundingBox partBoundingBox = dragonPart.getBoundingBox();
            boolean intersects = aabb.maxX >= partBoundingBox.getMinX() &&
                    aabb.minX <= partBoundingBox.getMaxX() &&
                    aabb.maxY >= partBoundingBox.getMinY() &&
                    aabb.minY <= partBoundingBox.getMaxY() &&
                    aabb.maxZ >= partBoundingBox.getMinZ() &&
                    aabb.minZ <= partBoundingBox.getMaxZ();
            if (!intersects) continue;
            Entity craftEnginePart = new BukkitEntity(dragonPart);
            Entity craftEngineParent = new BukkitEntity(dragonPart.getParent());
            if (!craftEnginePart.equals(entity) && !craftEngineParent.equals(entity) && predicate.test(craftEnginePart)) {
                entities.add(craftEnginePart);
            }
        }
        return entities;
    }

    @Override
    public String name() {
        return platformWorld().getName();
    }

    @Override
    public Path directory() {
        return platformWorld().getWorldFolder().toPath();
    }

    @Override
    public UUID uuid() {
        return platformWorld().getUID();
    }

    @Override
    public void dropItemNaturally(Position location, Item<?> item) {
        ItemStack itemStack = (ItemStack) item.getItem();
        if (ItemStackUtils.isEmpty(itemStack)) return;
        if (VersionHelper.isOrAbove1_21_2()) {
            platformWorld().dropItemNaturally(new Location(null, location.x(), location.y(), location.z()), (ItemStack) item.getItem());
        } else {
            platformWorld().dropItemNaturally(new Location(null, location.x() - 0.5, location.y() - 0.5, location.z() - 0.5), (ItemStack) item.getItem());
        }
    }

    @Override
    public void dropExp(Position location, int amount) {
        if (amount <= 0) return;
        EntityUtils.spawnEntity(platformWorld(), new Location(platformWorld(), location.x(), location.y(), location.z()), EntityType.EXPERIENCE_ORB, (e) -> {
            ExperienceOrb orb = (ExperienceOrb) e;
            orb.setExperience(amount);
        });
    }

    @Override
    public void playSound(Position location, Key sound, float volume, float pitch, SoundSource source) {
        platformWorld().playSound(new Location(null, location.x(), location.y(), location.z()), sound.toString(), SoundUtils.toBukkit(source), volume, pitch);
    }

    @Override
    public void playBlockSound(Position location, Key sound, float volume, float pitch) {
        platformWorld().playSound(new Location(null, location.x(), location.y(), location.z()), sound.toString(), SoundCategory.BLOCKS, volume, pitch);
    }

    @Override
    public void spawnParticle(Position location, Key particle, int count, double xOffset, double yOffset, double zOffset, double speed, @Nullable ParticleData extraData, @NotNull Context context) {
        Particle particleType = ParticleUtils.getParticle(particle);
        if (particleType == null) return;
        org.bukkit.World platformWorld = platformWorld();
        platformWorld.spawnParticle(particleType, location.x(), location.y(), location.z(), count, xOffset, yOffset, zOffset, speed, extraData == null ? null : ParticleUtils.toBukkitParticleData(extraData, context, platformWorld, location.x(), location.y(), location.z()));
    }

    @Override
    public long time() {
        return platformWorld().getTime();
    }

    @Override
    public void setBlockAt(int x, int y, int z, BlockStateWrapper blockState, int flags) {
        Object worldServer = serverWorld();
        Object blockPos = FastNMS.INSTANCE.constructor$BlockPos(x, y, z);
        FastNMS.INSTANCE.method$LevelWriter$setBlock(worldServer, blockPos, blockState.literalObject(), flags);
    }

    @Override
    public void levelEvent(int id, BlockPos pos, int data) {
        FastNMS.INSTANCE.method$LevelAccessor$levelEvent(serverWorld(), id, LocationUtils.toBlockPos(pos), data);
    }
}
