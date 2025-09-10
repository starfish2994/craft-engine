package net.momirealms.craftengine.bukkit.block.entity.renderer.element;

import it.unimi.dsi.fastutil.ints.IntList;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MEntityTypes;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElement;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.world.BlockPos;
import org.joml.Vector3f;

import java.util.List;
import java.util.UUID;

public class ItemDisplayBlockEntityElement implements BlockEntityElement {
    private final Object cachedSpawnPacket;
    private final Object cachedDespawnPacket;

    public ItemDisplayBlockEntityElement(ItemDisplayBlockEntityElementConfig config, BlockPos pos) {
        int entityId = CoreReflections.instance$Entity$ENTITY_COUNTER.incrementAndGet();
        Vector3f position = config.position();
        this.cachedSpawnPacket = FastNMS.INSTANCE.constructor$ClientboundBundlePacket(List.of(
                FastNMS.INSTANCE.constructor$ClientboundAddEntityPacket(
                        entityId, UUID.randomUUID(), pos.x() + position.x, pos.y() + position.y, pos.z() + position.z,
                        config.xRot(), config.yRot(), MEntityTypes.ITEM_DISPLAY, 0, CoreReflections.instance$Vec3$Zero, 0
                ),
                FastNMS.INSTANCE.constructor$ClientboundSetEntityDataPacket(
                        entityId, config.metadataValues().get()
                )
        ));
        this.cachedDespawnPacket = FastNMS.INSTANCE.constructor$ClientboundRemoveEntitiesPacket(IntList.of(entityId));
    }

    @Override
    public void despawn(Player player) {
        player.sendPacket(this.cachedDespawnPacket, false);
    }

    @Override
    public void spawn(Player player) {
        player.sendPacket(this.cachedSpawnPacket, true);
    }

    @Override
    public void update(Player player) {
    }
}
