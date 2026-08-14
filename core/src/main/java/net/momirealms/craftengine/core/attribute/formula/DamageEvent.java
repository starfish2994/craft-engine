package net.momirealms.craftengine.core.attribute.formula;

import net.momirealms.craftengine.core.attribute.Attribute;
import net.momirealms.craftengine.core.attribute.AttributeSide;
import net.momirealms.craftengine.core.entity.Entity;

import java.util.Map;

public interface DamageEvent {

    double damage();

    void setDamage(double damage);

    DamageSource source();

    Entity victim();

    boolean isSweepAttack();

    double getAttributeValue(AttributeSide side, Attribute attribute);

    void recordDamagePart(String id, double amount);

    Map<String, Double> damageParts();
}
