package net.momirealms.craftengine.bukkit.attribute.damage;

import net.momirealms.craftengine.bukkit.entity.BukkitEntity;
import net.momirealms.craftengine.bukkit.util.EntityUtils;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.core.attribute.damage.DamageSource;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.proxy.minecraft.world.damagesource.DamageSourceProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.EntityProxy;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

public final class BukkitDamageSource implements DamageSource {
    private final Object source;

    public BukkitDamageSource(Object source) {
        this.source = source;
    }

    public Key type() {
        return KeyUtils.unwrapHolder(DamageSourceProxy.INSTANCE.getType(this.source));
    }

    @Override
    public boolean isCritical() {
        return DamageSourceProxy.INSTANCE.isCritical(this.source);
    }

    @Nullable
    @Override
    public BukkitEntity causingEntity() {
        Object causingEntity = DamageSourceProxy.INSTANCE.getCausingEntity(this.source);
        if (causingEntity == null) {
            return null;
        }
        return EntityUtils.adaptNMS(causingEntity);
    }

    @Nullable
    public Entity causingBukkitEntity() {
        Object causingEntity = DamageSourceProxy.INSTANCE.getCausingEntity(this.source);
        if (causingEntity == null) {
            return null;
        }
        return EntityProxy.INSTANCE.getBukkitEntity(causingEntity);
    }

    public boolean isDirect() {
        return DamageSourceProxy.INSTANCE.getDirectEntity(this.source) == DamageSourceProxy.INSTANCE.getCausingEntity(this.source);
    }

    @Nullable
    @Override
    public BukkitEntity directEntity() {
        Object directEntity = directNmsEntity();
        if (directEntity == null) {
            return null;
        }
        return EntityUtils.adaptNMS(directEntity);
    }

    @Nullable
    public Object directNmsEntity() {
        return DamageSourceProxy.INSTANCE.getDirectEntity(this.source);
    }
}
