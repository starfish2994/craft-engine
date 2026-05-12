package net.momirealms.craftengine.proxy.common.text.component;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.proxy.common.context.Context;
import net.momirealms.craftengine.proxy.common.context.NetworkTextReplaceContext;
import net.momirealms.craftengine.proxy.common.tag.NetworkTagData;
import net.momirealms.craftengine.proxy.common.text.minimessage.FormattedLine;
import net.momirealms.craftengine.proxy.common.util.AdventureHelper;

import java.util.function.Function;

public sealed interface ComponentProvider extends Function<Context, Component>
        permits ComponentProvider.Constant, ComponentProvider.L10N, ComponentProvider.MiniMessage {

    static ComponentProvider constant(Component component) {
        return new Constant(component);
    }

    static ComponentProvider miniMessage(String line) {
        return new MiniMessage(line);
    }

    static ComponentProvider l10n(String translationKey, NetworkTagData networkTagData) {
        return new L10N(translationKey, networkTagData);
    }

    static ComponentProvider miniMessageOrConstant(String line) {
        if (line.equals(AdventureHelper.customMiniMessage().stripTags(line, FormattedLine.CUSTOM_RESOLVERS))) {
            return constant(AdventureHelper.miniMessage().deserialize(line));
        } else {
            return new MiniMessage(line);
        }
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
            return net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(this.value, context.tagResolvers());
        }
    }

    non-sealed class L10N implements ComponentProvider {
        private final String key;
        private final NetworkTagData networkTagData;

        public L10N(String key, NetworkTagData networkTagData) {
            this.key = key;
            this.networkTagData = networkTagData;
        }

        @Override
        public Component apply(Context context) {
            if (context instanceof NetworkTextReplaceContext networkContext) {
                String content = networkTagData.miniMessageTranslation(this.key, networkContext.player().locale());
                return net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(content, context.tagResolvers());
            }
            return Component.text(this.key);
        }
    }
}