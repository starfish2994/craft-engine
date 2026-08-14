package net.momirealms.craftengine.bukkit.entity.hologram;

import net.momirealms.craftengine.core.entity.hologram.DamageIndicatorType;
import net.momirealms.craftengine.core.entity.hologram.DamageIndicators;
import net.momirealms.craftengine.core.util.Key;

public final class BukkitDamageIndicators {
    public static final DamageIndicatorType<TextDamageIndicator> TEXT = DamageIndicators.register(Key.ce("text"), TextDamageIndicator.FACTORY);

    private BukkitDamageIndicators() {}

    public static void init() {
    }
}
