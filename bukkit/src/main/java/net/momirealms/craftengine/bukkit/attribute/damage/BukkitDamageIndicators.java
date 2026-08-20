package net.momirealms.craftengine.bukkit.attribute.damage;

import net.momirealms.craftengine.core.attribute.damage.DamageIndicatorType;
import net.momirealms.craftengine.core.attribute.damage.DamageIndicators;
import net.momirealms.craftengine.core.util.Key;

public final class BukkitDamageIndicators {
    public static final DamageIndicatorType<TextDamageIndicator> TEXT = DamageIndicators.register(Key.ce("text"), TextDamageIndicator.FACTORY);

    private BukkitDamageIndicators() {}

    public static void init() {
    }
}
