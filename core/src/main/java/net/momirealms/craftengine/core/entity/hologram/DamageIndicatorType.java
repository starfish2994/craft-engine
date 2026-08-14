package net.momirealms.craftengine.core.entity.hologram;

import net.momirealms.craftengine.core.util.Key;

public record DamageIndicatorType<T extends DamageIndicator>(Key id, DamageIndicatorFactory<T> factory) {
}
