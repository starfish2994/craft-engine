package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.special.TriggerOnceBlockBehavior;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.Vec3d;

import java.util.Map;
import java.util.concurrent.Callable;

public class BouncingBlockBehavior extends BukkitBlockBehavior implements TriggerOnceBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final double bounceHeight;
    private final boolean fallDamage;

    public BouncingBlockBehavior(CustomBlock customBlock, double bounceHeight, boolean fallDamage) {
        super(customBlock);
        this.bounceHeight = bounceHeight;
        this.fallDamage = fallDamage;
    }

    @Override
    public void fallOn(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        if (!this.fallDamage) {
            return;
        }
        Object entity = args[3];
        Object finalFallDistance = VersionHelper.isOrAbove1_21_5() ? (double) args[4] * 0.5 : (float) args[4] * 0.5F;
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
        Vec3d deltaMovement = LocationUtils.fromVec(FastNMS.INSTANCE.method$Entity$getDeltaMovement(entity));
        if (deltaMovement.y < 0.0) {
            double d = CoreReflections.clazz$LivingEntity.isInstance(entity) ? 1.0 : 0.8;
            double y = -deltaMovement.y * this.bounceHeight * d;
            FastNMS.INSTANCE.method$Entity$setDeltaMovement(entity, deltaMovement.x, y, deltaMovement.z);
            if (CoreReflections.clazz$Player.isInstance(entity) && y > 0.032) {
                // 防抖
                FastNMS.INSTANCE.field$Entity$hurtMarked(entity, true);
            }
        }
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            double bounceHeight = ResourceConfigUtils.getAsDouble(arguments.getOrDefault("bounce-height", 0.66), "bounce-height");
            boolean fallDamage = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("fall-damage", false), "fall-damage");
            return new BouncingBlockBehavior(block, bounceHeight, fallDamage);
        }
    }
}
