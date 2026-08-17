package net.momirealms.craftengine.core.item.equipment;

import net.momirealms.craftengine.core.plugin.config.ConfigKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;

public final class SetPotionEffect {
    private static final String[] SHOW_ICON = ConfigKeys.of("show_icon");
    private final Key type;
    private final int duration;
    private final int amplifier;
    private final boolean ambient;
    private final boolean particles;
    private final boolean icon;

    public SetPotionEffect(Key type, int duration, int amplifier, boolean ambient, boolean particles, boolean icon) {
        this.type = type;
        this.duration = duration;
        this.amplifier = amplifier;
        this.ambient = ambient;
        this.particles = particles;
        this.icon = icon;
    }

    public static SetPotionEffect fromConfig(ConfigSection section) {
        return new SetPotionEffect(
                section.getNonNullIdentifier("type"),
                section.getInt("duration", -1), // -1 常驻,档位失效时移除
                section.getInt("amplifier", 0),
                section.getBoolean("ambient"),
                section.getBoolean("particles", true),
                section.getBoolean(SHOW_ICON, true)
        );
    }

    public Key type() {
        return this.type;
    }

    public int duration() {
        return this.duration;
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
}
