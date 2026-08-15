package net.momirealms.craftengine.core.plugin.text.component;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.PlayerContext;
import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import net.momirealms.craftengine.core.util.AdventureHelper;

import java.util.function.Function;

public sealed interface ComponentProvider extends Function<Context, Component>
        permits ComponentProvider.Constant, ComponentProvider.L10N, ComponentProvider.MiniMessage {

    static ComponentProvider constant(Component component) {
        return new Constant(component);
    }

    static ComponentProvider miniMessageOrConstant(String line) {
        if (line.equals(AdventureHelper.customMiniMessage().stripTags(line))) {
            return constant(AdventureHelper.miniMessage().deserialize(line));
        } else {
            return new MiniMessage(line);
        }
    }

    static ComponentProvider miniMessage(String line) {
        return new MiniMessage(line);
    }

    static ComponentProvider l10n(String translationKey) {
        return new L10N(translationKey);
    }

    non-sealed class Constant implements ComponentProvider {
        private final Component value;

        public Constant(final Component value) {
            this.value = value;
        }

        @Override
        public Component apply(Context context) {
            return this.value;
        }
    }

    non-sealed class MiniMessage implements ComponentProvider {
        private final String value;

        public MiniMessage(final String value) {
            this.value = value;
        }

        @Override
        public Component apply(Context context) {
            return AdventureHelper.deserialize(this.value, context);
        }
    }

    non-sealed class L10N implements ComponentProvider {
        private final String key;

        public L10N(String key) {
            this.key = key;
        }

        @Override
        public Component apply(Context context) {
            if (context instanceof PlayerContext playerContext) {
                Player player = playerContext.player();
                if (player != null) {
                    String content = TranslationManager.instance().miniMessageTranslation(this.key, player.selectedLocale());
                    if (content == null) {
                        return Component.text(this.key);
                    }
                    return AdventureHelper.deserialize(content, context);
                }
            }
            return Component.text(this.key);
        }
    }
}