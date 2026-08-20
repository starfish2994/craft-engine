package net.momirealms.craftengine.core.item.equipment;

import net.momirealms.craftengine.core.plugin.config.ConfigKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class SetPotionEffect {
    private static final String[] SHOW_ICON = ConfigKeys.of("show_icon");
    private static final String[] CONDITIONS = ConfigKeys.of("condition(s)");
    private static final String[] UPDATE_INTERVAL = ConfigKeys.of("update_interval");
    private final Key type;
    private final int amplifier;
    private final boolean ambient;
    private final boolean particles;
    private final boolean icon;
    private final Predicate<Context> condition;
    private final int updateInterval;

    public SetPotionEffect(Key type,
                           int amplifier,
                           boolean ambient,
                           boolean particles,
                           boolean icon,
                           Predicate<Context> condition,
                           int updateInterval) {
        this.type = type;
        this.amplifier = amplifier;
        this.ambient = ambient;
        this.particles = particles;
        this.icon = icon;
        this.condition = condition;
        this.updateInterval = updateInterval;
    }

    public static SetPotionEffect fromConfig(ConfigSection section) {
        List<Predicate<Context>> conditions = section.getList(CONDITIONS, CommonConditions::fromConfig);
        int updateInterval = conditions.isEmpty() ? 0 : section.getValue(UPDATE_INTERVAL, v -> v.getAsInt(1), 20);
        return new SetPotionEffect(
                section.getNonNullIdentifier("type"),
                section.getInt("amplifier", 0),
                section.getBoolean("ambient"),
                section.getBoolean("particles", true),
                section.getBoolean(SHOW_ICON, true),
                MiscUtils.allOf(conditions),
                updateInterval
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

    public boolean test(Context context) {
        return this.condition.test(context);
    }

    public int updateInterval() {
        return this.updateInterval;
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
