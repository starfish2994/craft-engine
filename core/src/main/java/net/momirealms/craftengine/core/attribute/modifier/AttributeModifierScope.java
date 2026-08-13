package net.momirealms.craftengine.core.attribute.modifier;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public enum AttributeModifierScope {
    ENTITY("entity"),
    WEAPON("weapon");

    private static final Map<String, AttributeModifierScope> BY_ID = Map.of(
            "entity", ENTITY,
            "weapon", WEAPON
    );
    private final String id;

    AttributeModifierScope(String id) {
        this.id = id;
    }

    public static AttributeModifierScope byId(String id) {
        return BY_ID.get(id);
    }

    @NotNull
    public String id() {
        return this.id;
    }
}
