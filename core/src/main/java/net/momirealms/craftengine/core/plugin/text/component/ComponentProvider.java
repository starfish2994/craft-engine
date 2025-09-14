package net.momirealms.craftengine.core.plugin.text.component;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.AdventureHelper;

import java.util.function.Function;

import static net.momirealms.craftengine.core.plugin.text.minimessage.FormattedLine.CUSTOM_RESOLVERS;

public sealed interface ComponentProvider extends Function<Context, Component>
        permits ComponentProvider.Constant, ComponentProvider.MiniMessage {

    static ComponentProvider constant(Component component) {
        return new Constant(component);
    }

    static ComponentProvider miniMessageOrConstant(String line) {
        if (line.equals(AdventureHelper.customMiniMessage().stripTags(line, CUSTOM_RESOLVERS))) {
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
            return AdventureHelper.miniMessage().deserialize(this.value, context.tagResolvers());
        }
    }
}