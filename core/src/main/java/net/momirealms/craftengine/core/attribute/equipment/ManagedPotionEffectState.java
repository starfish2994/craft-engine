package net.momirealms.craftengine.core.attribute.equipment;

import net.momirealms.craftengine.core.entity.effect.PotionEffectSnapshot;
import net.momirealms.craftengine.core.item.equipment.SetPotionEffect;
import org.jetbrains.annotations.Nullable;

public record ManagedPotionEffectState(
        SetPotionEffect managed,
        @Nullable PotionEffectSnapshot shadow
) {
}
