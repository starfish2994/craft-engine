package net.momirealms.craftengine.core.attribute.damage;

import net.momirealms.craftengine.core.attribute.Attribute;
import net.momirealms.craftengine.core.attribute.AttributeSide;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface DamageEvent {

    EntityDamageContext context();

    double damage();

    void setDamage(double damage);

    DamageSource source();

    Entity victim();

    boolean isSweepAttack();

    double getAttributeValue(AttributeSide side, Attribute attribute);

    void recordDamagePart(String id, double amount);

    Map<String, Double> damageParts();

    @Nullable
    Item activeWeapon();
}
