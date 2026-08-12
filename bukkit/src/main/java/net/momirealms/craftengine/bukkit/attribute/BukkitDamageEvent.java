package net.momirealms.craftengine.bukkit.attribute;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.core.attribute.*;
import net.momirealms.craftengine.core.attribute.formula.DamageEvent;
import net.momirealms.craftengine.core.attribute.formula.DamageSource;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.proxy.bukkit.craftbukkit.damage.CraftDamageSourceProxy;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

public final class BukkitDamageEvent implements DamageEvent {
    private final EntityDamageEvent event;
    private final BukkitDamageSource source;
    private final BukkitAttributeManager manager;
    private final Entity victim;
    private final AttributeGetter victimAttributes;
    private final AttributeGetter attackerAttributes;

    public BukkitDamageEvent(BukkitAttributeManager manager, EntityDamageEvent event) {
        this.manager = manager;
        this.event = event;
        this.source = new BukkitDamageSource(CraftDamageSourceProxy.INSTANCE.getHandle(event.getDamageSource()));
        org.bukkit.entity.Entity victimEntity = this.event.getEntity();
        this.victim = BukkitAdaptor.adapt(victimEntity);
        AttributeContainer victimContainer = manager.getContainer(victimEntity.getUniqueId());
        this.victimAttributes = victimContainer == null ? EmptyAttributeHolder.INSTANCE : victimContainer;
        this.attackerAttributes = causingEntityAttributes();
    }

    @Override
    public double damage() {
        return this.event.getDamage();
    }

    @Override
    public void setDamage(double damage) {
        this.event.setDamage(damage);
    }

    @Override
    public DamageSource source() {
        return this.source;
    }

    @Override
    public Entity victim() {
        return this.victim;
    }

    @SuppressWarnings("deprecation")
    public AttributeGetter causingEntityAttributes() {
        org.bukkit.entity.Entity entity = this.source.causingBukkitEntity();
        if (entity == null) {
            return EmptyAttributeHolder.INSTANCE;
        }
        AttributeGetter container = this.manager.getContainer(entity.getUniqueId());
        if (container == null) {
            List<MetadataValue> attribute = entity.getMetadata(AttributeManager.META_KEY);
            if (!attribute.isEmpty()) {
                MetadataValue first = attribute.getFirst();
                container = (AttributeGetter) first.value();
            }
        }
        return container == null ? EmptyAttributeHolder.INSTANCE : container;
    }

    @Override
    public double getAttributeValue(AttributeSide side, Attribute attribute) {
        if (side == AttributeSide.ATTACKER) {
            return this.attackerAttributes.getAttributeValue(attribute);
        } else {
            return this.victimAttributes.getAttributeValue(attribute);
        }
    }
}
