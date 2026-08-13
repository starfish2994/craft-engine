package net.momirealms.craftengine.core.attribute.modifier;

import java.util.Locale;

public enum AttributeModifierScope {
    ENTITY,
    WEAPON;

    public static AttributeModifierScope byName(String name) {
        return valueOf(name.toUpperCase(Locale.ROOT));
    }
}
