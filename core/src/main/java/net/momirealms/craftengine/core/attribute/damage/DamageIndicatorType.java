package net.momirealms.craftengine.core.attribute.damage;

import net.momirealms.craftengine.core.util.Key;

public record DamageIndicatorType<T extends DamageIndicator>(Key id, DamageIndicatorFactory<T> factory) {
}
