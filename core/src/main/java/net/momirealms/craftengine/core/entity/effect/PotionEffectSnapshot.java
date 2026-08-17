package net.momirealms.craftengine.core.entity.effect;

import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

public record PotionEffectSnapshot(
        Key type,
        int duration,
        int amplifier,
        boolean ambient,
        boolean particles,
        boolean showIcon
) {

    public boolean isInfinite() {
        return this.duration < 0;
    }

    public boolean isExpired() {
        return !isInfinite() && this.duration <= 0;
    }

    public boolean isSameEffect(PotionEffectSnapshot effect) {
        if (effect == null) return false;
        return this.type.equals(effect.type)
                && this.amplifier == effect.amplifier
                && this.ambient == effect.ambient
                && this.particles == effect.particles
                && this.showIcon == effect.showIcon;
    }

    public PotionEffectSnapshot withDuration(int duration) {
        return new PotionEffectSnapshot(
                this.type,
                duration,
                this.amplifier,
                this.ambient,
                this.particles,
                this.showIcon
        );
    }

    @Nullable
    public static PotionEffectSnapshot getBetterEffect(
            @Nullable PotionEffectSnapshot existing,
            @Nullable PotionEffectSnapshot incoming
    ) {
        if (incoming == null || incoming.isExpired()) return existing;
        if (existing == null || existing.isExpired()) return incoming;
        if (incoming.amplifier != existing.amplifier) {
            return incoming.amplifier > existing.amplifier ? incoming : existing;
        }
        if (incoming.isInfinite()) return incoming;
        if (existing.isInfinite()) return existing;
        return incoming.duration >= existing.duration ? incoming : existing;
    }
}
