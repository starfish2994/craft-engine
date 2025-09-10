package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.VersionHelper;

import java.util.Map;
import java.util.concurrent.Callable;

public class BouncingBlockBehavior extends BukkitBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final double bounceHeight;
    private final boolean syncPlayerSelf;

    public BouncingBlockBehavior(CustomBlock customBlock, double bounceHeight, boolean syncPlayerSelf) {
        super(customBlock);
        this.bounceHeight = bounceHeight;
        this.syncPlayerSelf = syncPlayerSelf;
    }

    @Override
    public void fallOn(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        Object entity = args[3];
        Object finalFallDistance;
        if (VersionHelper.isOrAbove1_21_5()) {
            double fallDistance = (double) args[4];
            finalFallDistance = fallDistance * 0.5;
        } else {
            finalFallDistance = (float) args[4] * 0.5F;
        }
        FastNMS.INSTANCE.method$Entity$causeFallDamage(
                entity, finalFallDistance, 1.0F,
                FastNMS.INSTANCE.method$DamageSources$fall(FastNMS.INSTANCE.method$Entity$damageSources(entity))
        );
    }

    @Override
    public void updateEntityMovementAfterFallOn(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object entity = args[1];
        if (FastNMS.INSTANCE.method$Entity$getSharedFlag(entity, 1)) {
            superMethod.call();
        } else {
            bounceUp(entity);
        }
    }

    private void bounceUp(Object entity) {
        Object deltaMovement = FastNMS.INSTANCE.method$Entity$getDeltaMovement(entity);
        if (FastNMS.INSTANCE.field$Vec3$y(deltaMovement) < 0.0) {
            double d = CoreReflections.clazz$LivingEntity.isInstance(entity) ? 1.0 : 0.8;
            FastNMS.INSTANCE.method$Entity$setDeltaMovement(
                    entity,
                    FastNMS.INSTANCE.field$Vec3$x(deltaMovement),
                    -FastNMS.INSTANCE.field$Vec3$y(deltaMovement) * this.bounceHeight * d,
                    FastNMS.INSTANCE.field$Vec3$z(deltaMovement)
            );
            if (this.syncPlayerSelf) {
                FastNMS.INSTANCE.field$Entity$hurtMarked(entity, true);
            }
        }
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            double bounceHeight = ResourceConfigUtils.getAsDouble(arguments.getOrDefault("bounce-height", 0.66), "bounce-height");
            boolean syncPlayerSelf = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("sync-player-self", true), "sync-player-self");
            return new BouncingBlockBehavior(block, bounceHeight, syncPlayerSelf);
        }
    }
}
