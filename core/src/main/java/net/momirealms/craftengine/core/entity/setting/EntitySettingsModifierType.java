package net.momirealms.craftengine.core.entity.setting;

import net.momirealms.craftengine.core.util.Key;

public record EntitySettingsModifierType<M extends EntitySettingsModifier>(Key id, EntitySettingsModifierFactory<M> factory) {
}
