package net.momirealms.craftengine.core.entity;

import net.momirealms.craftengine.core.entity.setting.EntitySettings;
import net.momirealms.craftengine.core.util.Key;

public final class EntityDefinition {
    private final Key id;
    private final EntitySettings settings;

    public EntityDefinition(Key id, EntitySettings settings) {
        this.id = id;
        this.settings = settings;
    }

    public Key id() {
        return this.id;
    }

    public EntitySettings settings() {
        return this.settings;
    }
}
