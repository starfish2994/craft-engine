package net.momirealms.craftengine.core.item.equipment;

import net.momirealms.craftengine.core.plugin.config.ConfigKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;

import java.util.Objects;

public final class SetPotionEffect {
    private static final String[] SHOW_ICON = ConfigKeys.of("show_icon");
    private final Key type;
    private final int amplifier;
    private final boolean ambient;
    private final boolean particles;
    private final boolean icon;

    public SetPotionEffect(Key type, int amplifier, boolean ambient, boolean particles, boolean icon) {
        this.type = type;
        this.amplifier = amplifier;
        this.ambient = ambient;
        this.particles = particles;
        this.icon = icon;
    }

    public static SetPotionEffect fromConfig(ConfigSection section) {
        return new SetPotionEffect(
                section.getNonNullIdentifier("type"),
                section.getInt("amplifier", 0),
                section.getBoolean("ambient"),
                section.getBoolean("particles", true),
                section.getBoolean(SHOW_ICON, true)
        );
    }

    public Key type() {
        return this.type;
    }

    public boolean sameEffect(SetPotionEffect other) {
        return other != null
                && this.type.equals(other.type)
                && this.amplifier == other.amplifier
                && this.ambient == other.ambient
                && this.particles == other.particles
                && this.icon == other.icon;
    }

    public int amplifier() {
        return this.amplifier;
    }

    public boolean ambient() {
        return this.ambient;
    }

    public boolean particles() {
        return this.particles;
    }

    public boolean icon() {
        return this.icon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SetPotionEffect that)) return false;
        return this.sameEffect(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type, this.amplifier, this.ambient, this.particles, this.icon);
    }
}
