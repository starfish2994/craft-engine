package net.momirealms.craftengine.core.plugin.context.text;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.Key;

public final class TagTextProvider implements TextProvider {
    private final String text;

    public TagTextProvider(String text) {
        this.text = text;
    }

    public static TagTextProvider of(String text) {
        return new TagTextProvider(text);
    }

    @Override
    public String get(Context context) {
        Component resultComponent = AdventureHelper.expressionMiniMessage().deserialize(this.text);
        return AdventureHelper.serializeMiniMessage(resultComponent);
    }

    @Override
    public Key type() {
        return TextProviders.TAG;
    }
}
