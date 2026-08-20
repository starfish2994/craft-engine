package net.momirealms.craftengine.core.attribute.damage;

import org.jetbrains.annotations.Nullable;

public enum DamageVisibility {
    NONE,
    SELF,
    ALL;

    private static final DamageVisibility[] VALUES = values();

    public static DamageVisibility byName(@Nullable String name, DamageVisibility fallback) {
        if (name != null) {
            for (DamageVisibility value : VALUES) {
                if (value.name().equalsIgnoreCase(name)) return value;
            }
        }
        return fallback;
    }
}
