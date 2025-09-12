package net.momirealms.craftengine.bukkit.block;

import net.momirealms.craftengine.bukkit.plugin.reflection.bukkit.CraftBukkitReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.world.BukkitWorld;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.WorldPosition;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRemoveEvent;

import java.util.Optional;

@SuppressWarnings("DuplicatedCode")
public final class FallingBlockRemoveListener implements Listener {

    @EventHandler
    public void onFallingBlockBreak(EntityRemoveEvent event) {
        if (!(event.getEntity() instanceof FallingBlock fallingBlock)) return;
        try {
            if (event.getCause() == EntityRemoveEvent.Cause.DROP) {
                Object fallingBlockEntity = CraftBukkitReflections.field$CraftEntity$entity.get(fallingBlock);
                Object blockState = CoreReflections.field$FallingBlockEntity$blockState.get(fallingBlockEntity);
                Optional<ImmutableBlockState> optionalCustomState = BlockStateUtils.getOptionalCustomBlockState(blockState);
                if (optionalCustomState.isEmpty()) return;
                ImmutableBlockState customState = optionalCustomState.get();
                World world = new BukkitWorld(fallingBlock.getWorld());
                WorldPosition position = new WorldPosition(world, CoreReflections.field$Entity$xo.getDouble(fallingBlockEntity), CoreReflections.field$Entity$yo.getDouble(fallingBlockEntity), CoreReflections.field$Entity$zo.getDouble(fallingBlockEntity));
                boolean cancelDrop = (boolean) CoreReflections.field$FallingBlockEntity$cancelDrop.get(fallingBlockEntity);
                if (!cancelDrop) {
                    ContextHolder.Builder builder = ContextHolder.builder()
                            .withParameter(DirectContextParameters.FALLING_BLOCK, true)
                            .withParameter(DirectContextParameters.POSITION, position);
                    for (Item<Object> item : customState.getDrops(builder, world, null)) {
                        world.dropItemNaturally(position, item);
                    }
                }
                Object entityData = CoreReflections.field$Entity$entityData.get(fallingBlockEntity);
                boolean isSilent = (boolean) CoreReflections.method$SynchedEntityData$get.invoke(entityData, CoreReflections.instance$Entity$DATA_SILENT);
                if (!isSilent) {
                    world.playBlockSound(position, customState.settings().sounds().destroySound());
                }
            } else if (event.getCause() == EntityRemoveEvent.Cause.DESPAWN) {
                Object fallingBlockEntity = CraftBukkitReflections.field$CraftEntity$entity.get(fallingBlock);
                Object blockState = CoreReflections.field$FallingBlockEntity$blockState.get(fallingBlockEntity);
                Optional<ImmutableBlockState> optionalCustomState = BlockStateUtils.getOptionalCustomBlockState(blockState);
                if (optionalCustomState.isEmpty()) return;
                ImmutableBlockState customState = optionalCustomState.get();
                World world = new BukkitWorld(fallingBlock.getWorld());
                WorldPosition position = new WorldPosition(world, CoreReflections.field$Entity$xo.getDouble(fallingBlockEntity), CoreReflections.field$Entity$yo.getDouble(fallingBlockEntity), CoreReflections.field$Entity$zo.getDouble(fallingBlockEntity));
                Object entityData = CoreReflections.field$Entity$entityData.get(fallingBlockEntity);
                boolean isSilent = (boolean) CoreReflections.method$SynchedEntityData$get.invoke(entityData, CoreReflections.instance$Entity$DATA_SILENT);
                if (!isSilent) {
                    world.playBlockSound(position, customState.settings().sounds().destroySound());
                }
            }
        } catch (ReflectiveOperationException e) {
            CraftEngine.instance().logger().warn("Failed to handle EntityRemoveEvent", e);
        }
    }
}
